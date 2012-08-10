package polyglot.translate.ext;

import polyglot.ast.ArrayInit;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ArrayInitToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ArrayInit n = (ArrayInit) node();
        return rw.to_nf().ArrayInit(n.position(), n.elements());

    }
}
