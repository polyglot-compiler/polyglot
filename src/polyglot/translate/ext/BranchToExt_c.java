package polyglot.translate.ext;

import polyglot.ast.Branch;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class BranchToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Branch n = (Branch) node();
        return rw.to_nf().Branch(n.position(), n.kind(), n.labelNode());
    }
}
