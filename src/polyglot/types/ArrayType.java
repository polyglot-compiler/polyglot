package polyglot.types;

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

    /**
     * Return the number of dimensions in this array type.
     * e.g., for A[], return 1; for A[][], return 2, etc.
     */
    int dims();
}
