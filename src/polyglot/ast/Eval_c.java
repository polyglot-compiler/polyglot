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

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * An {@code Eval} wraps an expression in the context of a statement.
 * It evaluates the expression and then discards the result.
 */
public class Eval_c extends Stmt_c implements Eval {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;

//    @Deprecated
    public Eval_c(Position pos, Expr expr) {
        this(pos, expr, null);
    }

    public Eval_c(Position pos, Expr expr, Ext ext) {
        super(pos, ext);
        assert (expr != null);
        this.expr = expr;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Eval expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Eval_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Eval_c> N reconstruct(N n, Expr expr) {
        n = expr(n, expr);
        return n;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = visitChild(this.expr, v);
        return reconstruct(this, expr);
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Void();
        }

        return child.type();
    }

    @Override
    public String toString() {
        return "eval(" + expr.toString() + ");";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        boolean semi = tr.appendSemicolon(true);

        print(expr, w, tr);

        if (semi) {
            w.write(";");
        }

        tr.appendSemicolon(semi);
    }

    @Override
    public Term firstChild() {
        return expr;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        v.visitCFG(expr, this, EXIT);
        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Eval(this.position, this.expr);
    }

}
