package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.Call;
import polyglot.ast.TypeNode;

public interface JL5Call extends Call {

    List<TypeNode> typeArgs();

    JL5Call typeArgs(List<TypeNode> typeArgs);
}
