package polyglot.ext.jl5.translate;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.AnnotatedElement;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5FormalExt;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.FormalToExt_c;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate formals from Java 5 to Java 4
 */
public class JL5FormalToExt_c extends FormalToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        return rw.bypass(((AnnotatedElement) JL5Ext.ext(node())).annotationElems());
    }

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Formal f = (Formal) node();
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();

        Formal to =
                to_nf.Formal(f.position(),
                             f.flags(),
                             ((AnnotatedElement) JL5Ext.ext(f)).annotationElems(),
                             f.type(),
                             f.id(),
                             ((JL5FormalExt) JL5Ext.ext(f)).isVarArg());
        Type type = rw.to_ts().unknownType(f.position());
        LocalInstance li =
                rw.to_ts().localInstance(f.position(),
                                         f.flags(),
                                         type,
                                         f.name());
        return to.localInstance(li);
    }
}
