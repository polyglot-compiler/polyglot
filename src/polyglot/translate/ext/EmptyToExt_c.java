package polyglot.translate.ext;

import polyglot.ast.Empty;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class EmptyToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Empty n = (Empty) node();
        return rw.to_nf().Empty(n.position());
    }
}
