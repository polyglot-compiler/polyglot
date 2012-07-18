package polyglot.translate.ext;

import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class CanonicalTypeNodeToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        CanonicalTypeNode n = (CanonicalTypeNode) node();
        return rw.typeToJava(n.type(), n.position());
    }
}
