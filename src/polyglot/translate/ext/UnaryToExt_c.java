package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Unary;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class UnaryToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Unary n = (Unary) node();
        return rw.to_nf().Unary(n.position(), n.expr(), n.operator());
    }
}
