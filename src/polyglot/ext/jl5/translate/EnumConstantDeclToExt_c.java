package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.EnumConstantDecl;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;

public class EnumConstantDeclToExt_c extends ToExt_c implements ToExt {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        EnumConstantDecl cd = (EnumConstantDecl) node();
        return ((JL5NodeFactory) rw.to_nf()).EnumConstantDecl(cd.position(),
                                                              cd.flags(),
                                                              cd.annotationElems(),
                                                              cd.name(),
                                                              cd.args(),
                                                              cd.body());
    }
}
