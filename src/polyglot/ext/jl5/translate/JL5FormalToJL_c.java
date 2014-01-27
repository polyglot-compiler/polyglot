package polyglot.ext.jl5.translate;

import polyglot.ext.jl5.ast.JL5AnnotatedElementExt;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.FormalToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;
import polyglot.visit.NodeVisitor;

/**
 * Class used to translate formals from Java 5 to Java 4
 */
public class JL5FormalToJL_c extends FormalToExt_c implements ToExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        // Skip annotations
        return rw.bypass(JL5AnnotatedElementExt.annotationElems(node()));
    }
}
