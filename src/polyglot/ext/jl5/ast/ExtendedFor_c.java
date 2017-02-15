/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.FloatLit;
import polyglot.ast.IntLit;
import polyglot.ast.Lit;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop_c;
import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.ext.jl5.types.JL5ParsedClassType;
import polyglot.ext.jl5.types.JL5SubstClassType;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.RawClass;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;
import polyglot.visit.FlowGraph;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

public class ExtendedFor_c extends Loop_c implements ExtendedFor {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /** Loop body */
    protected LocalDecl decl;
    protected Expr expr;

    public ExtendedFor_c(Position pos, LocalDecl decl, Expr expr, Stmt body) {
        super(pos, null, body);
        assert decl != null && expr != null;
        this.decl = decl;
        this.expr = expr;
    }

    @Override
    public LocalDecl decl() {
        return decl;
    }

    @Override
    public ExtendedFor decl(LocalDecl decl) {
        return decl(this, decl);
    }

    protected <N extends ExtendedFor_c> N decl(N n, LocalDecl decl) {
        ExtendedFor_c ext = n;
        if (ext.decl.equals(decl)) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.decl = decl;
        return n;
    }

    @Override
    public Expr expr() {
        return expr;
    }

    @Override
    public ExtendedFor expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends ExtendedFor_c> N expr(N n, Expr expr) {
        ExtendedFor_c ext = n;
        if (ext.expr == expr) return n;
        if (n == this) {
            n = Copy.Util.copy(n);
            ext = n;
        }
        ext.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends ExtendedFor_c> N reconstruct(N n, LocalDecl decl,
            Expr expr) {
        n = decl(n, decl);
        n = expr(n, expr);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        ExtendedFor_c n = this;
        LocalDecl decl = visitChild(this.decl, v);
        Expr expr = visitChild(this.expr, v);
        Stmt body = visitChild(n.body(), v);
        return reconstruct(n, decl, expr).body(body);
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushBlock();
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        JL5TypeSystem ts = (JL5TypeSystem) tc.typeSystem();
        NodeFactory nf = tc.nodeFactory();
        Node n = this;
        Position position = n.position();
        // Check that the expr is an array or of type Iterable
        Type t = expr.type();
//        System.err.println(" t is a " + t.getClass());
//        System.err.println("    t is a " + ts.allAncestorsOf((ReferenceType) t));
//        System.err.println("    erasure(t) is " + ts.erasureType(t));
//        System.err.println("    iterable is a " + ts.Iterable().getClass());
        if (!expr.type().isArray()
                && !t.isSubtype(ts.rawClass((JL5ParsedClassType) ts.Iterable()))) {
            throw new SemanticException("Can only iterate over an array or an instance of java.util.Iterable",
                                        expr.position());
        }

        // Check that type is the same as elements in expr
        Type declType = decl().localInstance().type();
        Type elementType;
        if (expr.type().isArray()) {
            elementType = expr.type().toArray().base();
        }
        else if (t instanceof RawClass) {
            // we have a raw class.
            elementType = ts.Object();
        }
        else {
            JL5SubstClassType iterableType =
                    ts.findGenericSupertype((JL5ParsedClassType) ts.Iterable(),
                                            t.toReference());
            if (iterableType == null) {
                throw new InternalCompilerError("Cannot find generic supertype of Iterable for "
                        + t.toReference(), position);
            }
            elementType = iterableType.actuals().get(0);
        }
        if (!elementType.isImplicitCastValid(declType)) {
            throw new SemanticException("Incompatible types: required "
                    + declType + " but found " + elementType, position);
        }

        if (expr instanceof Local
                && decl.localInstance()
                       .equals(((Local) expr).localInstance())) {
            throw new SemanticException("Variable: " + expr
                    + " may not have been initialized", expr.position());
        }
        if (expr instanceof NewArray) {
            if (((NewArray) expr).init() != null) {
                for (Expr next : ((NewArray) expr).init().elements()) {
                    if (next instanceof Local
                            && decl.localInstance()
                                   .equals(((Local) next).localInstance())) {
                        throw new SemanticException("Variable: " + next
                                + " may not have been initialized",
                                                    next.position());
                    }
                }
            }
        }
        // Set the initializer so that the InitChecker doesn't get confused.
        Lit lit;
        Type type = decl.declType();
        Position pos = Position.compilerGenerated();
        if (type.isReference()) {
            lit = (Lit) nf.NullLit(pos).type(type.typeSystem().Null());
        }
        else if (type.isBoolean()) {
            lit = (Lit) nf.BooleanLit(pos, false).type(type);
        }
        else if (type.isInt() || type.isShort() || type.isChar()
                || type.isByte()) {
            lit = (Lit) nf.IntLit(pos, IntLit.INT, 0).type(type);
        }
        else if (type.isLong()) {
            lit = (Lit) nf.IntLit(pos, IntLit.LONG, 0).type(type);
        }
        else if (type.isFloat()) {
            lit = (Lit) nf.FloatLit(pos, FloatLit.FLOAT, 0.0).type(type);
        }
        else if (type.isDouble()) {
            lit = (Lit) nf.FloatLit(pos, FloatLit.DOUBLE, 0.0).type(type);
        }
        else throw new InternalCompilerError("Don't know default value for type "
                + type);
        return decl(decl.init(lit));
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        ExtendedFor n = this;
        v.visitCFG(expr,
                   FlowGraph.EDGE_KEY_TRUE,
                   decl,
                   Term.ENTRY,
                   FlowGraph.EDGE_KEY_FALSE,
                   n,
                   Term.EXIT);
        v.visitCFG(decl, n.body(), Term.ENTRY);
        v.push(n).visitCFG(n.body(), continueTarget(), Term.ENTRY);
        return succs;
    }

    @Override
    public Term continueTarget() {
        ExtendedFor n = this;
        return n.body();
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("for (");
        w.begin(0);

        boolean oldSemiColon = tr.appendSemicolon(false);
        // print the decl without an initializer
        printBlock(decl.init(null), w, tr);
        tr.appendSemicolon(oldSemiColon);

        w.allowBreak(1, " ");
        w.write(":");
        w.allowBreak(1, " ");
        print(expr, w, tr);
        w.end();
        w.write(")");

        ExtendedFor n = this;
        printSubStmt(n.body(), w, tr);
    }

}
