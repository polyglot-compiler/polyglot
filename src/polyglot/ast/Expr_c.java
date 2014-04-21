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

import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;

/**
 * An {@code Expr} represents any Java expression.  All expressions
 * must be subtypes of Expr.
 */
public abstract class Expr_c extends Term_c implements Expr, ExprOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Type type;

    @Deprecated
    public Expr_c(Position pos) {
        this(pos, null);
    }

    public Expr_c(Position pos, Ext ext) {
        super(pos, ext);
    }

    /**
     * Get the type of the expression.  This may return an
     * {@code UnknownType} before type-checking, but should return the
     * correct type after type-checking.
     */
    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public Expr type(Type type) {
        return type(this, type);
    }

    protected <N extends Expr_c> N type(N n, Type type) {
        if (n.type == type) return n;
        n = copyIfNeeded(n);
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

    @Override
    public Precedence precedence() {
        return Precedence.UNKNOWN;
    }

    @Deprecated
    protected Lang lastLang() {
        Lang lang = lang();
        for (Ext ext = ext(); ext != null; ext = ext.ext()) {
            try {
                lang = ext.lang();
            }
            catch (InternalCompilerError e) {
                return JLangToJLDel.instance;
            }
        }
        return lang;
    }

    @Deprecated
    @Override
    public boolean constantValueSet() {
        return lastLang().constantValueSet(this, lastLang());
    }

    @Deprecated
    @Override
    public boolean isConstant() {
        return lastLang().isConstant(this, lastLang());
    }

    @Deprecated
    @Override
    public Object constantValue() {
        return lastLang().constantValue(this, lastLang());
    }

    @Override
    public boolean constantValueSet(Lang lang) {
        return true;
    }

    @Override
    public boolean isConstant(Lang lang) {
        return false;
    }

    @Override
    public Object constantValue(Lang lang) {
        return null;
    }

    public String stringValue(Lang lang) {
        return (String) lang.constantValue(this, lang);
    }

    public boolean booleanValue(Lang lang) {
        return ((Boolean) lang.constantValue(this, lang)).booleanValue();
    }

    public byte byteValue(Lang lang) {
        return ((Byte) lang.constantValue(this, lang)).byteValue();
    }

    public short shortValue(Lang lang) {
        return ((Short) lang.constantValue(this, lang)).shortValue();
    }

    public char charValue(Lang lang) {
        return ((Character) lang.constantValue(this, lang)).charValue();
    }

    public int intValue(Lang lang) {
        return ((Integer) lang.constantValue(this, lang)).intValue();
    }

    public long longValue(Lang lang) {
        return ((Long) lang.constantValue(this, lang)).longValue();
    }

    public float floatValue(Lang lang) {
        return ((Float) lang.constantValue(this, lang)).floatValue();
    }

    public double doubleValue(Lang lang) {
        return ((Double) lang.constantValue(this, lang)).doubleValue();
    }

    @Override
    public boolean isTypeChecked() {
        return super.isTypeChecked() && type != null && type.isCanonical();
    }

    @Override
    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return type(tb.typeSystem().unknownType(position()));
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        Expr n = (Expr) super.extRewrite(rw);
        return n.type(null);
    }

    @Override
    public void printSubExpr(Expr expr, CodeWriter w, PrettyPrinter pp) {
        printSubExpr(expr, true, w, pp);
    }

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
