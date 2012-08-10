package polyglot.translate.ext;

import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class InstanceOfToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Instanceof io = (Instanceof) this.node();
        return rw.to_nf()
                 .Instanceof(io.position(), io.expr(), io.compareType());
    }
}
