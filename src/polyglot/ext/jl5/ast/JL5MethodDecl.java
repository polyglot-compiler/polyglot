package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.MethodDecl;

public interface JL5MethodDecl extends MethodDecl, AnnotatedElement {

    public boolean isCompilerGenerated();

    public JL5MethodDecl setCompilerGenerated(boolean val);

    public List<ParamTypeNode> typeParams();

    public JL5MethodDecl typeParams(List<ParamTypeNode> typeParams);

}
