package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Id;
import polyglot.ast.TypeNode;

public interface ParamTypeNode extends TypeNode {
    ParamTypeNode id(Id id);

    Id id();

    ParamTypeNode bounds(List<TypeNode> l);

    List<TypeNode> bounds();
}
