package polyglot.translate.ext;

import polyglot.ast.Labeled;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class LabeledToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Labeled n = (Labeled) node();
        return rw.to_nf().Labeled(n.position(), n.labelNode(), n.statement());
    }
}
