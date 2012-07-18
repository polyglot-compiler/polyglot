package polyglot.translate.ext;

import polyglot.ast.Case;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class CaseToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Case n = (Case) node();
        long value = n.value();
        n = rw.to_nf().Case(n.position(), n.expr());
        n = n.value(value);
        return n;
    }
}
