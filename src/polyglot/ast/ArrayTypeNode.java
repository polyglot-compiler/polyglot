package jltools.ast;

import jltools.types.Type;

public interface ArrayTypeNode extends TypeNode
{
    TypeNode base();
    ArrayTypeNode base(TypeNode base);
}
