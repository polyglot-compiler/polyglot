package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Return;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ReturnToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Return n = (Return) node();
        return rw.to_nf().Return(n.position(), n.expr());
    }
}
