package polyglot.ext.jl5.ast;

import polyglot.ast.DelFactory;
import polyglot.ast.JL;

public interface JL5DelFactory extends DelFactory {

    JL delEnumDecl();

    JL delExtendedFor();

    JL delEnumConstantDecl();

    JL delEnumConstant();

    JL delParamTypeNode();

    JL delAnnotationElemDecl();

    JL delNormalAnnotationElem();

    JL delMarkerAnnotationElem();

    JL delSingleElementAnnotationElem();

    JL delElementValuePair();

}
