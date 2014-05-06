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
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A {@code Case} is a representation of a Java {@code case}
 * statement.  It can only be contained in a {@code Switch}.
 */
public class Case_c extends Stmt_c implements Case {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr expr;
    protected long value;

//    @Deprecated
    public Case_c(Position pos, Expr expr) {
        this(pos, expr, null);
    }

    public Case_c(Position pos, Expr expr, Ext ext) {
        super(pos, ext);
        assert (true); // expr may be null for default case
        this.expr = expr;
    }

    @Override
    public boolean isDefault() {
        return this.expr == null;
    }

    @Override
    public Expr expr() {
        return this.expr;
    }

    @Override
    public Case expr(Expr expr) {
        return expr(this, expr);
    }

    protected <N extends Case_c> N expr(N n, Expr expr) {
        if (n.expr == expr) return n;
        n = copyIfNeeded(n);
        n.expr = expr;
        return n;
    }

    @Override
    public long value() {
        return this.value;
    }

    @Override
    public Case value(long value) {
        return value(this, value);
    }

    protected <N extends Case_c> N value(N n, long value) {
        if (n.value == value) return n;
        n = copyIfNeeded(n);
        n.value = value;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Case_c> N reconstruct(N n, Expr expr) {
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
        if (expr == null) {
            return this;
        }

        TypeSystem ts = tc.typeSystem();

        if (!ts.isImplicitCastValid(expr.type(), ts.Int())) {
            throw new SemanticException("Case label must be an byte, char, short, or int.",
                                        position());
        }

        return this;
    }

    @Override
    public Node checkConstants(ConstantChecker cc) throws SemanticException {
        if (expr == null) {
            return this;
        }

        if (!cc.lang().constantValueSet(expr, cc.lang())) {
            // Not ready yet; pass will get rerun.
            return this;
        }

        if (cc.lang().isConstant(expr, cc.lang())) {
            Object o = cc.lang().constantValue(expr, cc.lang());

            if (o instanceof Number && !(o instanceof Long)
                    && !(o instanceof Float) && !(o instanceof Double)) {

                return value(((Number) o).longValue());
            }
            else if (o instanceof Character) {
                return value(((Character) o).charValue());
            }
        }

        throw new SemanticException("Case label must be an integral constant.",
                                    position());
    }

    @Override
    public Type childExpectedType(Expr child, AscriptionVisitor av) {
        TypeSystem ts = av.typeSystem();

        if (child == expr) {
            return ts.Int();
        }

        return child.type();
    }

    @Override
    public String toString() {
        if (expr == null) {
            return "default:";
        }
        else {
            return "case " + expr + ":";
        }
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        if (expr == null) {
            w.write("default:");
        }
        else {
            w.write("case ");
            print(expr, w, tr);
            w.write(":");
        }
    }

    @Override
    public Term firstChild() {
        if (expr != null) return expr;
        return null;
    }

    @Override
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs) {
        if (expr != null) {
            v.visitCFG(expr, this, EXIT);
        }

        return succs;
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.Case(this.position, this.expr).value(value);
    }

}
