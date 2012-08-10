package polyglot.translate.ext;

import polyglot.ast.ArrayAccess;
import polyglot.ast.ArrayAccessAssign;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ArrayAccessAssignToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ArrayAccessAssign n = (ArrayAccessAssign) node();
        return rw.to_nf().ArrayAccessAssign(n.position(),
                                            (ArrayAccess) n.left(),
                                            n.operator(),
                                            n.right());
    }
}
