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

package polyglot.types;

/**
 * An <code>PrimitiveType_c</code> represents a primitive type.
 */
public class PrimitiveType_c extends Type_c implements PrimitiveType {
    protected Kind kind;

    /** Used for deserializing types. */
    protected PrimitiveType_c() {
    }

    public PrimitiveType_c(TypeSystem ts, Kind kind) {
        super(ts);
        this.kind = kind;
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public String toString() {
        return kind.toString();
    }

    @Override
    public String translate(Resolver c) {
        return kind.toString();
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public PrimitiveType toPrimitive() {
        return this;
    }

    @Override
    public boolean isVoid() {
        return kind == VOID;
    }

    @Override
    public boolean isBoolean() {
        return kind == BOOLEAN;
    }

    @Override
    public boolean isChar() {
        return kind == CHAR;
    }

    @Override
    public boolean isByte() {
        return kind == BYTE;
    }

    @Override
    public boolean isShort() {
        return kind == SHORT;
    }

    @Override
    public boolean isInt() {
        return kind == INT;
    }

    @Override
    public boolean isLong() {
        return kind == LONG;
    }

    @Override
    public boolean isFloat() {
        return kind == FLOAT;
    }

    @Override
    public boolean isDouble() {
        return kind == DOUBLE;
    }

    @Override
    public boolean isIntOrLess() {
        return kind == CHAR || kind == BYTE || kind == SHORT || kind == INT;
    }

    @Override
    public boolean isLongOrLess() {
        return isIntOrLess() || kind == LONG;
    }

    @Override
    public boolean isNumeric() {
        return isLongOrLess() || kind == FLOAT || kind == DOUBLE;
    }

    @Override
    public int hashCode() {
        return kind.hashCode();
    }

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (t instanceof PrimitiveType) {
            PrimitiveType p = (PrimitiveType) t;
            return kind() == p.kind();
        }
        return false;
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        return ts.equals(this, t);
    }

    @Override
    public String wrapperTypeString(TypeSystem ts) {
        return ts.wrapperTypeString(this);
    }

    @Override
    public String name() {
        return kind.toString();
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        return false;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        if (!toType.isPrimitive()) return false;

        PrimitiveType t = toType.toPrimitive();
        PrimitiveType f = this;

        if (t.isVoid()) return false;
        if (f.isVoid()) return false;

        if (ts.typeEquals(t, f)) return true;

        if (t.isBoolean()) return f.isBoolean();
        if (f.isBoolean()) return false;

        if (!f.isNumeric() || !t.isNumeric()) return false;

        if (t.isDouble()) return true;
        if (f.isDouble()) return false;

        if (t.isFloat()) return true;
        if (f.isFloat()) return false;

        if (t.isLong()) return true;
        if (f.isLong()) return false;

        if (t.isInt()) return true;
        if (f.isInt()) return false;

        if (t.isShort()) return f.isShort() || f.isByte();
        if (f.isShort()) return false;

        if (t.isChar()) return f.isChar();
        if (f.isChar()) return false;

        if (t.isByte()) return f.isByte();
        if (f.isByte()) return false;

        return false;
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from this to toType is valid; in other
     * words, some non-null members of this are also members of toType.
     **/
    @Override
    public boolean isCastValidImpl(Type toType) {
        if (isVoid() || toType.isVoid()) return false;
        if (ts.typeEquals(this, toType)) return true;
        if (isNumeric() && toType.isNumeric()) return true;
        return false;
    }

    /**
     * Returns true if literal value <code>value</code> can be converted to
     * this primitive type.  This method should be removed.  It is kept
     * for backward compatibility.
     */
    @Override
    public boolean numericConversionValidImpl(long value) {
        return numericConversionValidImpl(new Long(value));
    }

    /**
     * Returns true if literal value <code>value</code> can be converted to
     * this primitive type.
     */
    @Override
    public boolean numericConversionValidImpl(Object value) {
        if (value == null) return false;
        if (value instanceof Float || value instanceof Double) return false;

        long v;

        if (value instanceof Number) {
            v = ((Number) value).longValue();
        }
        else if (value instanceof Character) {
            v = ((Character) value).charValue();
        }
        else {
            return false;
        }

        if (isLong()) return true;
        if (isInt()) return Integer.MIN_VALUE <= v && v <= Integer.MAX_VALUE;
        if (isChar())
            return Character.MIN_VALUE <= v && v <= Character.MAX_VALUE;
        if (isShort()) return Short.MIN_VALUE <= v && v <= Short.MAX_VALUE;
        if (isByte()) return Byte.MIN_VALUE <= v && v <= Byte.MAX_VALUE;

        return false;
    }
}
