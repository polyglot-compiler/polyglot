package jltools.types;

/**
 * An <code>ArrayType</code> represents an array of other types.
 */
public interface ArrayType extends ReferenceType 
{
    Type base();
    ArrayType base(Type base);

    Type ultimateBase();
    FieldInstance lengthField();
    MethodInstance cloneMethod();
}
