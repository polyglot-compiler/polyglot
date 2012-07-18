package polyglot.translate.ext;

import polyglot.ast.Initializer;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class InitializerToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Initializer n = (Initializer) node();
        return rw.to_nf().Initializer(n.position(), n.flags(), n.body());
    }
}
