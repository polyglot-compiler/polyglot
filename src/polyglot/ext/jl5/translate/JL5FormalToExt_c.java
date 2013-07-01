package polyglot.ext.jl5.translate;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.JL5Formal;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.FormalToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate formals from Java 5 to Java 4
 */
public class JL5FormalToExt_c extends FormalToExt_c implements ToExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        JL5Formal cd = (JL5Formal) node();
        return rw.bypass(cd.annotationElems());
    }

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        JL5Formal n = (JL5Formal) node();
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();

        Formal to =
                to_nf.Formal(n.position(),
                             n.flags(),
                             n.annotationElems(),
                             n.type(),
                             n.id(),
                             n.isVarArg());
        Type type = rw.to_ts().unknownType(n.position());
        LocalInstance li =
                rw.to_ts().localInstance(n.position(),
                                         n.flags(),
                                         type,
                                         n.name());
        return to.localInstance(li);
    }
}
