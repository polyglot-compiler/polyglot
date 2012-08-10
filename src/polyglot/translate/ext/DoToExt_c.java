package polyglot.translate.ext;

import polyglot.ast.Do;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class DoToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Do n = (Do) node();
        return rw.to_nf().Do(n.position(), n.body(), n.cond());
    }
}
