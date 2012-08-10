package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Throw;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ThrowToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Throw n = (Throw) node();
        return rw.to_nf().Throw(n.position(), n.expr());
    }
}
