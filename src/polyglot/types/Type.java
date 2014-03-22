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

/**
 * A {@code Type} is the base type of all classes which represent
 * types.
 */
public interface Type extends Qualifier {
    /**
     * Return a string into which to translate the type.
     * @param c A resolver in which to lookup this type to determine if
     * the type is unique in the given resolver.
     */
    String translate(Resolver c);

    /**
     * Return an array of this type.
     */
    ArrayType arrayOf();

    /**
     * Return a {@code dims}-array of this type.
     */
    ArrayType arrayOf(int dims);

    /**
     * Cast the type to a class type, or null.
     * @return a non-null iff {@code isClass()} returns true.
     */
    ClassType toClass();

    /**
     * Cast the type to a null type, or null.
     * @return a non-null iff {@code isNull()} returns true.
     */
    NullType toNull();

    /**
     * Cast the type to a reference type, or null.
     * @return a non-null iff {@code isReference()} returns true.
     */
    ReferenceType toReference();

    /**
     * Cast the type to a primitive type, or null.
     * @return a non-null iff {@code isPrimitive()} returns true.
     */
    PrimitiveType toPrimitive();

    /**
     * Cast the type to an array type, or null.
     * @return a non-null iff {@code isArray()} returns true.
     */
    ArrayType toArray();

    /**
     * Return true if this type is equivalent to t.
     * Usually this is the same as equalsImpl(TypeObject), but that
     * method should return true only if the types are
     * <i>structurally equal</i>.
     * @param t Type to compare to
     * @return True if this type is equivalent to t.
     */
    boolean typeEquals(Type t);

    /**
     * Return true if this type is equivalent to t.
     * Usually this is the same as equalsImpl(TypeObject), but that
     * method should return true only if the types are
     * <i>structurally equal</i>.
     * @param t Type to compare to
     * @return True if this type is equivalent to t.
     */
    boolean typeEqualsImpl(Type t);

    /**
     * Return true if this type is a subtype of {@code ancestor}.
     */
    boolean isSubtype(Type ancestor);

    /**
     * Return true if this type descends from {@code ancestor}.
     */
    boolean descendsFrom(Type ancestor);

    /**
     * Return true if this type can be cast to {@code toType}.
     */
    boolean isCastValid(Type toType);

    /**
     * Return true if a value of this type can be assigned to a variable of
     * type {@code toType}.
     */
    boolean isImplicitCastValid(Type toType);

    /**
     * Return true a literal {@code value} can be converted to this type.
     */
    boolean numericConversionValid(Object value);

    /**
     * Return true a literal {@code value} can be converted to this type.
     * @deprecated Use {@link #numericConversionValid(Object)} instead.
     */
    @Deprecated
    boolean numericConversionValid(long value);

    /**
     * Return true if this type is a subtype of {@code ancestor}.
     */
    boolean isSubtypeImpl(Type ancestor);

    /**
     * Return true if this type descends from {@code ancestor}.
     */
    boolean descendsFromImpl(Type ancestor);

    /**
     * Return true if this type can be cast to {@code toType}.
     */
    boolean isCastValidImpl(Type toType);

    /**
     * Return true if a value of this type can be assigned to a variable of
     * type {@code toType}.
     */
    boolean isImplicitCastValidImpl(Type toType);

    /**
     * Return true a literal {@code value} can be converted to this type.
     */
    boolean numericConversionValidImpl(Object value);

    /**
     * Return true a literal {@code value} can be converted to this type.
     * @deprecated Use {@link #numericConversionValidImpl(Object)} instead.
     */
    @Deprecated
    boolean numericConversionValidImpl(long value);

    /**
     * Return true if a primitive type.
     */
    boolean isPrimitive();

    /**
     * Return true if void.
     */
    boolean isVoid();

    /**
     * Return true if boolean.
     */
    boolean isBoolean();

    /**
     * Return true if char.
     */
    boolean isChar();

    /**
     * Return true if byte.
     */
    boolean isByte();

    /**
     * Return true if short.
     */
    boolean isShort();

    /**
     * Return true if int.
     */
    boolean isInt();

    /**
     * Return true if long.
     */
    boolean isLong();

    /**
     * Return true if float.
     */
    boolean isFloat();

    /**
     * Return true if double.
     */
    boolean isDouble();

    /**
     * Return true if int, short, byte, or char.
     */
    boolean isIntOrLess();

    /**
     * Return true if long, int, short, byte, or char.
     */
    boolean isLongOrLess();

    /**
     * Return true if double, float, long, int, short, byte, or char.
     */
    boolean isNumeric();

    /**
     * Return true if a reference type.
     */
    boolean isReference();

    /**
     * Return true if a null type.
     */
    boolean isNull();

    /**
     * Return true if an array type.
     */
    boolean isArray();

    /**
     * Return true if a class type.
     */
    boolean isClass();

    /**
     * Return true if a subclass of {@code Throwable}.
     */
    boolean isThrowable();

    /**
     * Return true if an unchecked exception.
     */
    boolean isUncheckedException();

    /**
     * Return true if the types can be compared; that is, if they have
     * the same type system.
     */
    boolean isComparable(Type t);

    /**
     * Yields a string representing this type.  The string
     * should be consistent with equality.  That is,
     * if this.equals(anotherType), then it should be
     * that this.toString().equals(anotherType.toString()).
     *
     * The string is not necessarily a legal type expression
     * in the language, such as a Java identifier. It is
     * suggested, but not required, that it be an easily
     * human-readable representation that can be used
     * both in error messages and in generated output.
     */
    @Override
    String toString();

    /**
     * Output a compilable representation of this type to {@code w}.
     * For generated output, this method should be used
     * rather than toString().  It is suggested, but not
     * required, that it be an easily human-readable
     * representation that can be used both in error
     * messages and in generated output.
     */
    void print(CodeWriter w);
}
