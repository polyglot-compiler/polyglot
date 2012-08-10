/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.ast.ArrayInit;
import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.ConstructorCall;
import polyglot.ast.Do;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.FieldDecl;
import polyglot.ast.For;
import polyglot.ast.If;
import polyglot.ast.Lit;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.Stmt;
import polyglot.ast.Switch;
import polyglot.ast.Term;
import polyglot.ast.Unary;
import polyglot.ast.While;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * The <code>FlattenVisitor</code> flattens the AST,
 */
public class FlattenVisitor extends NodeVisitor {
    protected TypeSystem ts;
    protected NodeFactory nf;
    protected LinkedList<List<Stmt>> stack;

    public FlattenVisitor(TypeSystem ts, NodeFactory nf) {
        this.ts = ts;
        this.nf = nf;
        stack = new LinkedList<List<Stmt>>();
    }

    @Override
    public Node override(Node parent, Node n) {
        // Insert Blocks when needed to allow local decls to be inserted.
        if (n instanceof If) {
            If s = (If) n;
            Stmt s1 = s.consequent();
            Stmt s2 = s.alternative();
            if (!(s1 instanceof Block)) {
                s = s.consequent(nf.Block(s1.position(), s1));
            }
            if (s2 != null && !(s2 instanceof Block)) {
                s = s.alternative(nf.Block(s2.position(), s2));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof Do) {
            Do s = (Do) n;
            Stmt s1 = s.body();
            if (!(s1 instanceof Block)) {
                s = s.body(nf.Block(s1.position(), s1));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof While) {
            While s = (While) n;
            Stmt s1 = s.body();
            if (!(s1 instanceof Block)) {
                s = s.body(nf.Block(s1.position(), s1));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof For) {
            For s = (For) n;
            Stmt s1 = s.body();
            if (!(s1 instanceof Block)) {
                s = s.body(nf.Block(s1.position(), s1));
            }
            return visitEdgeNoOverride(parent, s);
        }

        if (n instanceof FieldDecl || n instanceof ConstructorCall) {
            if (!stack.isEmpty()) {
                List<Stmt> l = stack.getFirst();
                l.add((Stmt) n);
            }
            return n;
        }

        // punt on switch statement
        if (n instanceof Switch) {
            return n;
        }

        if (neverFlatten.contains(n)) {
            return n;
        }

        if (n instanceof ArrayInit) {
            return n;
        }

        return null;
    }

    protected static int count = 0;

    protected static String newID() {
        return "flat$$$" + count++;
    }

    protected Set<Term> noFlatten = new HashSet<Term>();
    protected Set<Term> neverFlatten = new HashSet<Term>();

    /** 
     * When entering a BlockStatement, place a new StatementList
     * onto the stack
     */
    @Override
    public NodeVisitor enter(Node parent, Node n) {
        if (n instanceof Block) {
            stack.addFirst(new LinkedList<Stmt>());
        }

        if (n instanceof Eval) {
            // Don't flatten the expression contained in the statement, but
            // flatten its subexpressions.
            Eval s = (Eval) n;
            noFlatten.add(s.expr());
        }

        if (n instanceof LocalDecl) {
            // Don't flatten the expression contained in the statement, but
            // flatten its subexpressions.
            LocalDecl s = (LocalDecl) n;
            noFlatten.add(s.init());
        }

        if (n instanceof For) {
            For s = (For) n;
            noFlatten.addAll(s.inits());
            neverFlatten.addAll(s.iters());
            neverFlatten.add(s.cond());
        }

        if (n instanceof While) {
            While s = (While) n;
            neverFlatten.add(s.cond());
        }

        if (n instanceof Do) {
            Do s = (Do) n;
            neverFlatten.add(s.cond());
        }

        if (n instanceof Assign) {
            Assign s = (Assign) n;
            noFlatten.add(s.left());
            noFlatten.add(s.right());
        }

        if (n instanceof Unary) {
            Unary u = (Unary) n;
            noFlatten.add(u.expr());
        }

        return this;
    }

    /** 
     * Flatten complex expressions within the AST
     */
    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (noFlatten.contains(old)) {
            noFlatten.remove(old);
            return n;
        }

        if (n instanceof Block) {
            List<Stmt> l = stack.removeFirst();
            Block block = ((Block) n).statements(l);
            if (parent instanceof Block && !stack.isEmpty()) {
                l = stack.getFirst();
                l.add(block);
            }
            return block;
        }
        else if (n instanceof Stmt) {
            List<Stmt> l = stack.getFirst();
            l.add((Stmt) n);
            return n;
        }
        else if (n instanceof Expr && !(n instanceof Lit)
                && !(n instanceof Special) && !(n instanceof Local)) {

            Expr e = (Expr) n;

            if (e instanceof Assign) {
                return n;
            }

            /*
            if (e.isTypeChecked() && e.type().isVoid()) {
                return n;
            }
            */

            // create a local temp, initialized to the value of the complex
            // expression

            String name = newID();
            LocalDecl def =
                    nf.LocalDecl(e.position(),
                                 Flags.FINAL,
                                 nf.CanonicalTypeNode(e.position(), e.type()),
                                 nf.Id(Position.compilerGenerated(), name),
                                 e);
            def =
                    def.localInstance(ts.localInstance(e.position(),
                                                       Flags.FINAL,
                                                       e.type(),
                                                       name));

            List<Stmt> l = stack.getFirst();
            l.add(def);

            // return the local temp instead of the complex expression
            Local use =
                    nf.Local(e.position(),
                             nf.Id(Position.compilerGenerated(), name));
            use = (Local) use.type(e.type());
            use =
                    use.localInstance(ts.localInstance(e.position(),
                                                       Flags.FINAL,
                                                       e.type(),
                                                       name));
            return use;
        }

        return n;
    }
}
