package polyglot.ext.jl5.translate;

import polyglot.ast.FieldDecl;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5FieldDeclExt;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.FieldDeclToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

/**
 * Class used to translate field declarations from Java 5 to Java 4
 */
public class JL5FieldDeclToExt_c extends FieldDeclToExt_c implements ToExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        FieldDecl n = (FieldDecl) this.node();
        JL5FieldDeclExt ext = (JL5FieldDeclExt) JL5Ext.ext(n);
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();
        return to_nf.FieldDecl(n.position(),
                               n.flags(),
                               ext.annotationElems(),
                               n.type(),
                               n.id(),
                               n.init());
    }

}
