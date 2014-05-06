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

package polyglot.ast;

import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * An immutable representation of a Java language {@code synchronized}
 * block. Contains an expression being tested and a statement to be executed
 * while the expression is {@code true}.
 */
public class Synchronized_c extends Stmt_c implements Synchronized {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;
    protected Block body;

//    @Deprecated
    public Synchronized_c(Position pos, Expr expr, Block body) {
        this(pos, expr, body, null);
    }

    public Synchronized_c(Position pos, Expr expr, Block body, Ext ext) {
        super(pos, ext);
        assert (expr != null && body != null);
        this.expr = expr;
        this.body = body;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Synchronized expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Synchronized_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    @Override
    public Block body() {
        return this.body;
    }

    @Override
    public Synchronized body(Block body) {
        return body(this, body);
    }

    protected <N extends Synchronized_c> N body(N n, Block body) {
        if (n.body == body) return n;
        n = copyIfNeeded(n);
        n.body = body;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Synchronized_c> N reconstruct(N n, Expr expr,
            Block body) {
        n = expr(n, expr);
        n = body(n, body);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        Block body = visitChild(this.body, v);
        return reconstruct(this, expr, body);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Synchronized expression must be a reference type.
        // See JLS 2nd Ed. | 14.18.
        if (!expr.type().isReference()) {
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
