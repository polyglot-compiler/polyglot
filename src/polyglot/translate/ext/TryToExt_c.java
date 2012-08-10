package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Try;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class TryToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Try n = (Try) node();
        return rw.to_nf().Try(n.position(),
                              n.tryBlock(),
                              n.catchBlocks(),
                              n.finallyBlock());
    }
}
