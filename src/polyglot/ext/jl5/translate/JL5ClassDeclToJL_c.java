package polyglot.ext.jl5.translate;

import polyglot.ext.jl5.ast.JL5ClassDecl;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ClassDeclToExt_c;
import polyglot.types.SemanticException;
import polyglot.visit.NodeVisitor;

public class JL5ClassDeclToJL_c extends ClassDeclToExt_c {

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        //Skip annotations and parameter nodes
        JL5ClassDecl cd = (JL5ClassDecl) node();
        rw = (ExtensionRewriter) rw.bypass(cd.annotationElems());
        rw = (ExtensionRewriter) rw.bypass(cd.paramTypes());
        return rw;
    }
}
