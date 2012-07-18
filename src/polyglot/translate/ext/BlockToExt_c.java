package polyglot.translate.ext;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class BlockToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Block b = (Block) node();
        return rw.to_nf().Block(b.position(), b.statements());
    }
}
