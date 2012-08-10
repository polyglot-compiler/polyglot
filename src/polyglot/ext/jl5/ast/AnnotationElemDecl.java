package polyglot.ext.jl5.ast;

import polyglot.ast.Expr;
import polyglot.ast.MethodDecl;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.AnnotationTypeElemInstance;
import polyglot.types.Flags;

public interface AnnotationElemDecl extends MethodDecl {

    AnnotationElemDecl type(TypeNode type);

    TypeNode type();

    @Override
    AnnotationElemDecl flags(Flags flags);

    @Override
    Flags flags();

    AnnotationElemDecl defaultVal(Expr def);

    Expr defaultVal();

    @Override
    AnnotationElemDecl name(String name);

    @Override
    String name();

    AnnotationElemDecl annotationElemInstance(AnnotationTypeElemInstance ai);

    AnnotationTypeElemInstance annotationElemInstance();
}
