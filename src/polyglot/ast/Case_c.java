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
import polyglot.visit.ConstantChecker;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeChecker;

/**
 * A <code>Case</code> is a representation of a Java <code>case</code>
 * statement.  It can only be contained in a <code>Switch</code>.
 */
public class Case_c extends Stmt_c implements Case {
    protected Expr expr;
    protected long value;

    public Case_c(Position pos, Expr expr) {
        super(pos);
        assert (true); // expr may be null for default case
        this.expr = expr;
    }

    /** Returns true iff this is the default case. */
    @Override
    public boolean isDefault() {
        return this.expr == null;
    }

    /**
     * Get the case label.  This must should a constant expression.
     * The case label is null for the <code>default</code> case.
     */
    @Override
    public Expr expr() {
        return this.expr;
    }

    /** Set the case label.  This must should a constant expression, or null. */
    @Override
    public Case expr(Expr expr) {
        Case_c n = (Case_c) copy();
        n.expr = expr;
        return n;
    }

    /**
     * Returns the value of the case label.  This value is only valid
     * after type-checking.
     */
    @Override
    public long value() {
        return this.value;
    }

    /** Set the value of the case label. */
    @Override
    public Case value(long value) {
        Case_c n = (Case_c) copy();
        n.value = value;
        return n;
    }

    /** Reconstruct the statement. */
    protected Case_c reconstruct(Expr expr) {
        if (expr != this.expr) {
            Case_c n = (Case_c) copy();
            n.expr = expr;
            return n;
        }

        return this;
    }

    /** Visit the children of the statement. */
    @Override
    public Node visitChildren(NodeVisitor v) {
        Expr expr = (Expr) visitChild(this.expr, v);
        return reconstruct(expr);
    }

    /** Type check the statement. */
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

        if (!expr.constantValueSet()) {
            // Not ready yet; pass will get rerun.
            return this;
        }

        if (expr.isConstant()) {
            Object o = expr.constantValue();

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

    /** Write the statement to an output file. */
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
