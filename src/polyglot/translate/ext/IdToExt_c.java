package polyglot.translate.ext;

import polyglot.ast.Id;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class IdToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Id n = (Id) node();
        return rw.to_nf().Id(n.position(), n.id());
    }
}
