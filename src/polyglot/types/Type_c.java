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

package polyglot.types;

import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * Abstract implementation of a {@code Type}.  This implements most of
 * the "isa" and "cast" methods of the type and methods which just dispatch to
 * the type system.
 */
public abstract class Type_c extends TypeObject_c implements Type {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /** Used for deserializing types. */
    protected Type_c() {
    }

    /** Creates a new type in the given a TypeSystem. */
    public Type_c(TypeSystem ts) {
        this(ts, Position.COMPILER_GENERATED);
    }

    /** Creates a new type in the given a TypeSystem at a given position. */
    public Type_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    @Override
    public abstract String translate(Resolver c);

    @Override
    public boolean isType() {
        return true;
    }

    @Override
    public boolean isPackage() {
        return false;
    }

    @Override
    public Type toType() {
        return this;
    }

    @Override
    public Package toPackage() {
        return null;
    }

    /* To be filled in by subtypes. */
    @Override
    public boolean isCanonical() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isIntOrLess() {
        return false;
    }

    @Override
    public boolean isLongOrLess() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isChar() {
        return false;
    }

    @Override
    public boolean isByte() {
        return false;
    }

    @Override
    public boolean isShort() {
        return false;
    }

    @Override
    public boolean isInt() {
        return false;
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isThrowable() {
        return false;
    }

    @Override
    public boolean isUncheckedException() {
        return false;
    }

    @Override
    public ClassType toClass() {
        return null;
    }

    @Override
    public NullType toNull() {
        return null;
    }

    @Override
    public ReferenceType toReference() {
        return null;
    }

    @Override
    public PrimitiveType toPrimitive() {
        return null;
    }

    @Override
    public ArrayType toArray() {
        return null;
    }

    @Override
    public ArrayType arrayOf(int dims) {
        return ts.arrayOf(this, dims);
    }

    @Override
    public ArrayType arrayOf() {
        return ts.arrayOf(this);
    }

    @Override
    public final boolean typeEquals(Type t) {
        return ts.typeEquals(this, t);
    }

    @Override
    public boolean typeEqualsImpl(Type t) {
        return this == t;
    }

    @Override
    public final boolean isSubtype(Type ancestor) {
        return ts.isSubtype(this, ancestor);
    }

    @Override
    public boolean isSubtypeImpl(Type ancestor) {
        return ts.typeEquals(this, ancestor) || ts.descendsFrom(this, ancestor);
    }

    @Override
    public final boolean descendsFrom(Type ancestor) {
        return ts.descendsFrom(this, ancestor);
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        return false;
    }

    @Override
    public final boolean isCastValid(Type toType) {
        return ts.isCastValid(this, toType);
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        return false;
    }

    @Override
    public final boolean isImplicitCastValid(Type toType) {
        return ts.isImplicitCastValid(this, toType);
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        return false;
    }

    @Deprecated
    @Override
    public final boolean numericConversionValid(long value) {
        return ts.numericConversionValid(this, value);
    }

    @Deprecated
    @Override
    public boolean numericConversionValidImpl(long value) {
        return numericConversionValidImpl(new Long(value));
    }

    @Override
    public final boolean numericConversionValid(Object value) {
        return ts.numericConversionValid(this, value);
    }

    @Override
    public boolean numericConversionValidImpl(Object value) {
        return false;
    }

    @Override
    public boolean isComparable(Type t) {
        return t.typeSystem() == ts;
    }

    @Override
    public abstract String toString();

    /**
     * Output a compilable representation of this type to {@code w}.
     * This implementation generates whatever representation is produced
     * by {@code toString()}. To satisfy the specification of
     * {@code Type.toString()}, this implementation needs to be overridden
     * if {@code toString} does not produce a compilable representation.
     */
    @Override
    public void print(CodeWriter w) {
        w.write(toString());
    }
}
