package polyglot.ext.jl5.translate;

import polyglot.ext.jl5.ast.JL5FieldDecl_c;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.FieldDeclToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.types.SemanticException;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate field declarations from Java 5 to Java 4
 */
public class JL5FieldDeclToExt_c extends FieldDeclToExt_c implements ToExt {
    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        JL5FieldDecl_c cd = (JL5FieldDecl_c) node();
        return rw.bypass(cd.annotationElems());
    }
}
