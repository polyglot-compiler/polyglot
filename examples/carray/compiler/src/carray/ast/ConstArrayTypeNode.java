package jltools.ext.carray.ast;

import jltools.ast.*;

/**
 * A <code>ConstArrayTypeNode</code> is a type node for a non-canonical
 * const array type.
 */
public interface ConstArrayTypeNode extends ArrayTypeNode
{
    TypeNode base();
    ArrayTypeNode base(TypeNode base);
}
