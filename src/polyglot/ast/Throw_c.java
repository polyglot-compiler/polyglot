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

import java.util.Collections;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Throw} is an immutable representation of a {@code throw}
 * statement. Such a statement contains a single {@code Expr} which
 * evaluates to the object being thrown.
 */
public class Throw_c extends Stmt_c implements Throw {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;

//    @Deprecated
    public Throw_c(Position pos, Expr expr) {
        this(pos, expr, null);
    }

    public Throw_c(Position pos, Expr expr, Ext ext) {
        super(pos, ext);
        assert expr != null;
        this.expr = expr;
    }

    @Override
    public Expr expr() {
        return expr;
    }

    @Override
    public Throw expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Throw_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Throw_c> N reconstruct(N n, Expr expr) {
        n = expr(n, expr);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        return reconstruct(this, expr);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        if (!expr.type().isSubtype(tc.typeSystem().Throwable())) {
            throw new SemanticException("Can only throw subclasses of \""
                    + tc.typeSystem().Throwable() + "\".", expr.position());
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Throwable();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "throw " + expr + ";";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("throw ");
        print(expr, w, tr);
        w.write(";");
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, this, EXIT);

        // Throw edges will be handled by visitor.
        return Collections.<T> emptyList();
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        // if the exception that a throw statement is given to throw is null,
        // then a NullPointerException will be thrown.
        if (expr.type().typeEquals(ts.Null()))
            return Collections.<Type> singletonList(ts.NullPointerException());
        return CollectionUtil.list(expr.type(), ts.NullPointerException());
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Throw(position, expr);
    }

}
