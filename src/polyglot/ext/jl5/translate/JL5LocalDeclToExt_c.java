package polyglot.ext.jl5.translate;

import polyglot.ext.jl5.ast.JL5LocalDecl_c;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.LocalDeclToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate local declarations from Java 5 to Java 4
 */
public class JL5LocalDeclToExt_c extends LocalDeclToExt_c implements ToExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        JL5LocalDecl_c cd = (JL5LocalDecl_c) node();
        return rw.bypass(cd.annotationElems());
    }
}
