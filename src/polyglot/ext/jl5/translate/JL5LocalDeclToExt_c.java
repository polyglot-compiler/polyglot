package polyglot.ext.jl5.translate;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.AnnotatedElement;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.LocalDeclToExt_c;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;

/**
 * Class used to translate local declarations from Java 5 to Java 4
 */
public class JL5LocalDeclToExt_c extends LocalDeclToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        LocalDecl n = (LocalDecl) node();
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();

        Type type = rw.to_ts().unknownType(n.position());
        LocalInstance li =
                rw.to_ts().localInstance(n.position(),
                                         n.flags(),
                                         type,
                                         n.name());
        LocalDecl to =
                to_nf.LocalDecl(n.position(),
                                n.flags(),
                                ((AnnotatedElement) JL5Ext.ext(n)).annotationElems(),
                                n.type(),
                                n.id(),
                                n.init());
        return to.localInstance(li);
    }

}
