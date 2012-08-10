package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ConstructorCall;
import polyglot.ast.TypeNode;

public interface JL5ConstructorCall extends ConstructorCall {
    List<TypeNode> typeArgs();

    JL5ConstructorCall typeArgs(List<TypeNode> typeArgs);

}
