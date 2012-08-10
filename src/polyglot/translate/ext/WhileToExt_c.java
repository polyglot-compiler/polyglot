package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.While;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class WhileToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        While n = (While) node();
        return rw.to_nf().While(n.position(), n.cond(), n.body());
    }
}
