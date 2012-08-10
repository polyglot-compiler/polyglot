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

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.AscriptionVisitor;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * An <code>Eval</code> is a wrapper for an expression in the context of
 * a statement.
 */
public class Eval_c extends Stmt_c implements Eval {
    protected Expr expr;

    public Eval_c(Position pos, Expr expr) {
        super(pos);
        assert (expr != null);
        this.expr = expr;
    }

    /** Get the expression of the statement. */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the expression of the statement. */
    @Override
    public Eval expr(Expr expr) {
        Eval_c n = (Eval_c) copy();
        n.expr = expr;
        return n;
    }

    /** Reconstruct the statement. */
    protected Eval_c reconstruct(Expr expr) {
        if (expr != this.expr) {
            Eval_c n = (Eval_c) copy();
            n.expr = expr;
            return n;
        }

        return this;
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Void();
        }

        return child.type();
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        return reconstruct(expr);
    }

    @Override
    public String toString() {
        return "eval(" + expr.toString() + ");";
    }

    /** Write the statement to an output file. */
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
