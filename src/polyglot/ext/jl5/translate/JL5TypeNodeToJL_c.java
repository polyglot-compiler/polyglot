package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;

public class JL5TypeNodeToJL_c extends ToExt_c implements ToExt {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        TypeNode n = (TypeNode) node();
        return rw.typeToJava(n.type(), n.position());
    }
}
