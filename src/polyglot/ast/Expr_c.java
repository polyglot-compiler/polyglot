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

import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;

/**
 * An <code>Expr</code> represents any Java expression.  All expressions
 * must be subtypes of Expr.
 */
public abstract class Expr_c extends Term_c implements Expr {
    protected Type type;

    public Expr_c(Position pos) {
        super(pos);
    }

    /**
     * Get the type of the expression.  This may return an
     * <code>UnknownType</code> before type-checking, but should return the
     * correct type after type-checking.
     */
    @Override
    public Type type() {
        return this.type;
    }

    /** Set the type of the expression. */
    @Override
    public Expr type(Type type) {
        if (type == this.type) return this;
        Expr_c n = (Expr_c) copy();
        n.type = type;
        return n;
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);

        if (type != null) {
            w.allowBreak(4, " ");
            w.begin(0);
            w.write("(type " + type + ")");
            w.end();
        }
    }

    /** Get the precedence of the expression. */
    @Override
    public Precedence precedence() {
        return Precedence.UNKNOWN;
    }

    @Override
    public boolean constantValueSet() {
        return true;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Object constantValue() {
        return null;
    }

    public String stringValue() {
        return (String) constantValue();
    }

    public boolean booleanValue() {
        return ((Boolean) constantValue()).booleanValue();
    }

    public byte byteValue() {
        return ((Byte) constantValue()).byteValue();
    }

    public short shortValue() {
        return ((Short) constantValue()).shortValue();
    }

    public char charValue() {
        return ((Character) constantValue()).charValue();
    }

    public int intValue() {
        return ((Integer) constantValue()).intValue();
    }

    public long longValue() {
        return ((Long) constantValue()).longValue();
    }

    public float floatValue() {
        return ((Float) constantValue()).floatValue();
    }

    public double doubleValue() {
        return ((Double) constantValue()).doubleValue();
    }

    @Override
    public boolean isTypeChecked() {
        return super.isTypeChecked() && type != null && type.isCanonical();
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    /**
     * Correctly parenthesize the subexpression <code>expr<code> given
     * the its precendence and the precedence of the current expression.
     *
     * If the sub-expression has the same precedence as this expression
     * we do not parenthesize.
     *
     * @param expr The subexpression.
     * @param w The output writer.
     * @param pp The pretty printer.
     */
    @Override
    public void printSubExpr(Expr expr, CodeWriter w, PrettyPrinter pp) {
        printSubExpr(expr, true, w, pp);
    }

    /**
     * Correctly parenthesize the subexpression <code>expr<code> given
     * the its precendence and the precedence of the current expression.
     *
     * If the sub-expression has the same precedence as this expression
     * we parenthesize if the sub-expression does not associate; e.g.,
     * we parenthesis the right sub-expression of a left-associative
     * operator.
     *
     * @param expr The subexpression.
     * @param associative Whether expr is the left (right) child of a left-
     * (right-) associative operator.
     * @param w The output writer.
     * @param pp The pretty printer.
     */
    @Override
    public void printSubExpr(Expr expr, boolean associative, CodeWriter w,
            PrettyPrinter pp) {
        if (!associative && precedence().equals(expr.precedence())
                || precedence().isTighter(expr.precedence())) {
            w.write("(");
            printBlock(expr, w, pp);
            w.write(")");
        }
        else {
            print(expr, w, pp);
        }
    }
}
