package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.SwitchBlock;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class SwitchBlockToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        SwitchBlock n = (SwitchBlock) node();
        return rw.to_nf().SwitchBlock(n.position(), n.statements());
    }
}
