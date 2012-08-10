package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class SpecialToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Special n = (Special) node();
        return rw.to_nf().Special(n.position(), n.kind(), n.qualifier());
    }
}
