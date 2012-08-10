package polyglot.translate.ext;

import polyglot.ast.Cast;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class CastToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Cast c = (Cast) this.node();
        return rw.to_nf().Cast(c.position(), c.castType(), c.expr());
    }
}
