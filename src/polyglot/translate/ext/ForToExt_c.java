package polyglot.translate.ext;

import polyglot.ast.For;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ForToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        For n = (For) node();
        return rw.to_nf().For(n.position(),
                              n.inits(),
                              n.cond(),
                              n.iters(),
                              n.body());
    }
}
