package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.New;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class NewToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        New n = (New) node();
        return rw.to_nf().New(n.position(),
                              n.qualifier(),
                              n.objectType(),
                              n.arguments(),
                              n.body());

    }
}
