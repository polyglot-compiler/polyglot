package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ConstructorDecl;

public interface JL5ConstructorDecl extends ConstructorDecl, AnnotatedElement {
    public List<ParamTypeNode> typeParams();

    public JL5ConstructorDecl typeParams(List<ParamTypeNode> typeParams);
}
