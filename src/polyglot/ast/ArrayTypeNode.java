package jltools.ast;

import jltools.types.Type;

/**
 * An <code>ArrayTypeNode</code> is a type node for a non-canonical
 * array type.
 */
public interface ArrayTypeNode extends TypeNode
{
    TypeNode base();
    ArrayTypeNode base(TypeNode base);
}
