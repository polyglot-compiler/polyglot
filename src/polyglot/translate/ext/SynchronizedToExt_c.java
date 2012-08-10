package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Synchronized;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class SynchronizedToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Synchronized n = (Synchronized) node();
        return rw.to_nf().Synchronized(n.position(), n.expr(), n.body());
    }
}
