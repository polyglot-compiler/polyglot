package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ClassDecl;

public interface JL5ClassDecl extends ClassDecl, AnnotatedElement {
    List<ParamTypeNode> paramTypes();
}
