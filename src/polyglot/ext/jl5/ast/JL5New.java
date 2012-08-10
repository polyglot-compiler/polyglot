package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.New;
import polyglot.ast.TypeNode;

public interface JL5New extends New {
    List<TypeNode> typeArgs();

    JL5New typeArgs(List<TypeNode> typeArgs);
}
