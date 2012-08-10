package polyglot.translate.ext;

import polyglot.ast.NewArray;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class NewArrayToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        NewArray n = (NewArray) node();
        return rw.to_nf().NewArray(n.position(),
                                   n.baseType(),
                                   n.dims(),
                                   n.additionalDims(),
                                   n.init());
    }
}
