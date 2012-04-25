package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.TypeNode;

public interface AnnotationElem extends Expr {

    TypeNode typeName();
    AnnotationElem typeName(TypeNode typeName);
}
