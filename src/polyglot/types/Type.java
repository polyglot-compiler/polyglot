package jltools.types;

import jltools.util.Position;

/**
 * A <code>Type</code> is the base type of all classes which represent
 * types.
 */
public interface Type extends Qualifier
{
    String translate(Context c);
    ArrayType arrayOf();
    ArrayType arrayOf(int dims);

    ClassType toClass();
    NullType toNull();
    ReferenceType toReference();
    PrimitiveType toPrimitive();
    ArrayType toArray();

    boolean isSame(Type t);
    boolean isSubtype(Type t);
    boolean descendsFrom(Type t);
    boolean isAssignableSubtype(Type t);
    boolean isCastValid(Type t);
    boolean isImplicitCastValid(Type t);

    boolean isPrimitive();
    boolean isVoid();
    boolean isBoolean();
    boolean isChar();
    boolean isByte();
    boolean isShort();
    boolean isInt();
    boolean isLong();
    boolean isFloat();
    boolean isDouble();
    boolean isIntOrLess();
    boolean isLongOrLess();
    boolean isNumeric();

    boolean isReference();
    boolean isNull();
    boolean isArray();
    boolean isClass();
    boolean isThrowable();
    boolean isUncheckedException();
}
