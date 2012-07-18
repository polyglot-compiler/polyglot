package polyglot.translate.ext;

import polyglot.ast.Catch;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class CatchToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Catch b = (Catch) node();
        return rw.to_nf().Catch(b.position(), b.formal(), b.body());
    }
}
