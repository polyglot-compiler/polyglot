package polyglot.translate.ext;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ConstructorCallToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ConstructorCall n = (ConstructorCall) node();
        return rw.to_nf().ConstructorCall(n.position(),
                                          n.kind(),
                                          n.qualifier(),
                                          n.arguments());
    }
}
