package carray.ast;

import polyglot.ast.ArrayTypeNode;
import polyglot.ast.TypeNode;

/**
 * A <code>ConstArrayTypeNode</code> is a type node for a non-canonical
 * const array type.
 */
public interface ConstArrayTypeNode extends ArrayTypeNode {
    @Override
    TypeNode base();

    @Override
    ArrayTypeNode base(TypeNode base);
}
