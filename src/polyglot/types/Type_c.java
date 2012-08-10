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

import polyglot.util.CodeWriter;
import polyglot.util.Position;

/**
 * Abstract implementation of a <code>Type</code>.  This implements most of
 * the "isa" and "cast" methods of the type and methods which just dispatch to
 * the type system.
 */
public abstract class Type_c extends TypeObject_c implements Type {
    /** Used for deserializing types. */
    protected Type_c() {
    }

    /** Creates a new type in the given a TypeSystem. */
    public Type_c(TypeSystem ts) {
        this(ts, null);
    }

    /** Creates a new type in the given a TypeSystem at a given position. */
    public Type_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    /**
     * Return a string into which to translate the type.
     * @param c A resolver in which to lookup this type to determine if
     * the type is unique in the given resolver.
     */
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

    /**
     * Return true if a subclass of Throwable.
     */
    @Override
    public boolean isThrowable() {
        return false;
    }

    /**
     * Return true if an unchecked exception.
     */
    @Override
    public boolean isUncheckedException() {
        return false;
    }

    /** Returns a non-null iff isClass() returns true. */
    @Override
    public ClassType toClass() {
        return null;
    }

    /** Returns a non-null iff isNull() returns true. */
    @Override
    public NullType toNull() {
        return null;
    }

    /** Returns a non-null iff isReference() returns true. */
    @Override
    public ReferenceType toReference() {
        return null;
    }

    /** Returns a non-null iff isPrimitive() returns true. */
    @Override
    public PrimitiveType toPrimitive() {
        return null;
    }

    /** Returns a non-null iff isArray() returns true. */
    @Override
    public ArrayType toArray() {
        return null;
    }

    /**
     * Return a <code>dims</code>-array of this type.
     */
    @Override
    public ArrayType arrayOf(int dims) {
        return ts.arrayOf(this, dims);
    }

    /**
     * Return an array of this type.
     */
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

    /**
     * Return true if this type is a subtype of <code>ancestor</code>.
     */
    @Override
    public final boolean isSubtype(Type t) {
        return ts.isSubtype(this, t);
    }

    /**
     * Return true if this type is a subtype of <code>ancestor</code>.
     */
    @Override
    public boolean isSubtypeImpl(Type t) {
        return ts.typeEquals(this, t) || ts.descendsFrom(this, t);
    }

    /**
     * Return true if this type descends from <code>ancestor</code>.
     */
    @Override
    public final boolean descendsFrom(Type t) {
        return ts.descendsFrom(this, t);
    }

    /**
     * Return true if this type descends from <code>ancestor</code>.
     */
    @Override
    public boolean descendsFromImpl(Type t) {
        return false;
    }

    /**
     * Return true if this type can be cast to <code>toType</code>.
     */
    @Override
    public final boolean isCastValid(Type toType) {
        return ts.isCastValid(this, toType);
    }

    /**
     * Return true if this type can be cast to <code>toType</code>.
     */
    @Override
    public boolean isCastValidImpl(Type toType) {
        return false;
    }

    /**
     * Return true if a value of this type can be assigned to a variable of
     * type <code>toType</code>.
     */
    @Override
    public final boolean isImplicitCastValid(Type toType) {
        return ts.isImplicitCastValid(this, toType);
    }

    /**
     * Return true if a value of this type can be assigned to a variable of
     * type <code>toType</code>.
     */
    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        return false;
    }

    /**
     * Return true if a literal <code>value</code> can be converted to this type.
     * This method should be removed.  It is kept for backward compatibility.
     */
    @Override
    public final boolean numericConversionValid(long value) {
        return ts.numericConversionValid(this, value);
    }

    /**
     * Return true if a literal <code>value</code> can be converted to this type.
     * This method should be removed.  It is kept for backward compatibility.
     */
    @Override
    public boolean numericConversionValidImpl(long value) {
        return false;
    }

    /**
     * Return true if a literal <code>value</code> can be converted to this type.
     */
    @Override
    public final boolean numericConversionValid(Object value) {
        return ts.numericConversionValid(this, value);
    }

    /**
     * Return true if a literal <code>value</code> can be converted to this type.
     */
    @Override
    public boolean numericConversionValidImpl(Object value) {
        return false;
    }

    /**
     * Return true if the types can be compared; that is, if they have
     * the same type system.
     */
    @Override
    public boolean isComparable(Type t) {
        return t.typeSystem() == ts;
    }

    @Override
    public abstract String toString();

    /**
     * Output a compilable representation of this type to <code>w</code>.
     * This implementation generates whatever representation is produced
     * by <code>toString()</code>. To satisfy the specification of
     * <code>Type.toString()</code>, this implementation needs to be overridden
     * if <code>toString</code> does not produce a compilable representation.
     */
    @Override
    public void print(CodeWriter w) {
        w.write(toString());
    }
}
