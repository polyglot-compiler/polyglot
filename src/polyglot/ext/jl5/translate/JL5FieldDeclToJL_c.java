package polyglot.ext.jl5.translate;

import polyglot.ast.FieldDecl;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5FieldDeclExt;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.FieldDeclToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate field declarations from Java 5 to Java 4
 */
public class JL5FieldDeclToJL_c extends FieldDeclToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        FieldDecl n = (FieldDecl) this.node();
        JL5FieldDeclExt ext = (JL5FieldDeclExt) JL5Ext.ext(n);
        return rw.bypass(ext.annotationElems());
    }
}
