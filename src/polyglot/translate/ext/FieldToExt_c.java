package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Field;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class FieldToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Field n = (Field) node();
        return rw.to_nf().Field(n.position(), n.target(), n.id());
    }
}
