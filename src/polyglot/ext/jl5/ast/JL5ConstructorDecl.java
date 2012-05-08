package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ConstructorDecl;
import polyglot.ast.TypeNode;

public interface JL5ConstructorDecl extends ConstructorDecl {
	public List typeParams();

	public JL5ConstructorDecl typeParams(List<TypeNode> typeParams);
}
