package polyglot.ast;

import java.util.List;

/**
 * A <code>NewArray</code> represents a new array expression such as <code>new
 * File[8][] { null }</code>.  It consists of an element type (e.g.,
 * <code>File</code>), a list of dimension expressions (e.g., 8), 0 or more
 * additional dimensions (e.g., 1 for []), and an array initializer.  The
 * dimensions of the array initializer must equal the number of additional "[]"
 * dimensions.
 */
public interface NewArray extends Expr
{
    TypeNode baseType();
    NewArray baseType(TypeNode baseType);

    int numDims();

    List dims();
    NewArray dims(List dims);

    int additionalDims();
    NewArray additionalDims(int addDims);

    ArrayInit init();
    NewArray init(ArrayInit init);
}
