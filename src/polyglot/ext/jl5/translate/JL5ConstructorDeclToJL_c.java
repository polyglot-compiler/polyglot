package polyglot.ext.jl5.translate;

import polyglot.ext.jl5.ast.JL5ConstructorDecl;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ConstructorDeclToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.types.SemanticException;
import polyglot.visit.NodeVisitor;

public class JL5ConstructorDeclToJL_c extends ConstructorDeclToExt_c implements
        ToExt {
    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        //Skip annotations and parameter nodes
        JL5ConstructorDecl cd = (JL5ConstructorDecl) node();
        return rw.bypass(cd.typeParams());
    }

}
