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

package polyglot.ast;

import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An immutable representation of a Java language <code>synchronized</code>
 * block. Contains an expression being tested and a statement to be executed
 * while the expression is <code>true</code>.
 */
public class Synchronized_c extends Stmt_c implements Synchronized {
    protected Expr expr;
    protected Block body;

    public Synchronized_c(Position pos, Expr expr, Block body) {
        super(pos);
        assert (expr != null && body != null);
        this.expr = expr;
        this.body = body;
    }

    /** Get the expression to synchronize. */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the expression to synchronize. */
    @Override
    public Synchronized expr(Expr expr) {
        Synchronized_c n = (Synchronized_c) copy();
        n.expr = expr;
        return n;
    }

    /** Get the body of the statement. */
    @Override
    public Block body() {
        return this.body;
    }

    /** Set the body of the statement. */
    @Override
    public Synchronized body(Block body) {
        Synchronized_c n = (Synchronized_c) copy();
        n.body = body;
        return n;
    }

    /** Reconstruct the statement. */
    protected Synchronized_c reconstruct(Expr expr, Block body) {
        if (expr != this.expr || body != this.body) {
            Synchronized_c n = (Synchronized_c) copy();
            n.expr = expr;
            n.body = body;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        Block body = (Block) visitChild(this.body, v);
        return reconstruct(expr, body);
    }

    /** Type check the statement. */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

        if (!ts.isSubtype(expr.type(), ts.Object())) {
            throw new SemanticException("Cannot synchronize on an expression of type \""
                                                + expr.type() + "\".",
                                        expr.position());
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Object();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "synchronized (" + expr + ") { ... }";
    }

    /** Write the statement to an output file. */
    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("synchronized (");
        printBlock(expr, w, tr);
        w.write(") ");
        printSubStmt(body, w, tr);
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, body, ENTRY);
        v.visitCFG(body, this, EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Synchronized(this.position, this.expr, this.body);
    }

}
