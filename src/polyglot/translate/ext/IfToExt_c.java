package polyglot.translate.ext;

import polyglot.ast.If;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class IfToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        If n = (If) node();
        return rw.to_nf().If(n.position(),
                             n.cond(),
                             n.consequent(),
                             n.alternative());
    }
}
