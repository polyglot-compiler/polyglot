package polyglot.translate.ext;

import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class FieldAssignToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        FieldAssign n = (FieldAssign) node();
        return rw.to_nf().FieldAssign(n.position(),
                                      (Field) n.left(),
                                      n.operator(),
                                      n.right());
    }
}
