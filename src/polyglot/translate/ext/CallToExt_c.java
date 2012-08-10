package polyglot.translate.ext;

import polyglot.ast.Call;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class CallToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Call n = (Call) node();
        n = rw.to_nf().Call(n.position(), n.target(), n.id(), n.arguments());
        return n;
    }
}
