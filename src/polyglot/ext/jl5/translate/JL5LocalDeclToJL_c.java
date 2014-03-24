package polyglot.ext.jl5.translate;

import polyglot.ast.LocalDecl;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5LocalDeclExt;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.LocalDeclToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate local declarations from Java 5 to Java 4
 */
public class JL5LocalDeclToJL_c extends LocalDeclToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        LocalDecl n = (LocalDecl) node();
        JL5LocalDeclExt ext = (JL5LocalDeclExt) JL5Ext.ext(n);
        return rw.bypass(ext.annotationElems());
    }
}
