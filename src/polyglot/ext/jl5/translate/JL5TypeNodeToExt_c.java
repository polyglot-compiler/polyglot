package polyglot.ext.jl5.translate;

import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.CanonicalTypeNodeToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;

public class JL5TypeNodeToExt_c extends ToExt_c
		implements ToExt {
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        TypeNode n = (TypeNode) node();
        JL5TypeSystem jl5ts = (JL5TypeSystem) rw.from_ts();
        return rw.typeToJava(jl5ts.erasureType(n.type()), n.position());
    }
}
