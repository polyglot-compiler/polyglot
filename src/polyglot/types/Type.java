package polyglot.types;

import polyglot.util.Position;

/**
 * A <code>Type</code> is the base type of all classes which represent
 * types.
 */
public interface Type extends Qualifier
{
    String translate(Resolver c);
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
    boolean isCastValid(Type t);
    boolean isImplicitCastValid(Type t);
    boolean numericConversionValid(long value);

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

    /**
     * Yields a string representing this type.  The string
     * should be consistent with equality.  That is,
     * if this.isSame(anotherType), then it should be
     * that this.toString().equals(anotherType.toString()).
     *
     * The string does not have to be a legal Java identifier.
     * It is suggested, but not required, that it be an
     * easily human readable representation, and thus useful
     * in error messages and generated output.
     */
    String toString();
}
