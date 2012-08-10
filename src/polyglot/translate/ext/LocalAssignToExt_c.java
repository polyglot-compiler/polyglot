package polyglot.translate.ext;

import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class LocalAssignToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        LocalAssign n = (LocalAssign) node();
        return rw.to_nf().LocalAssign(n.position(),
                                      (Local) n.left(),
                                      n.operator(),
                                      n.right());
    }
}
