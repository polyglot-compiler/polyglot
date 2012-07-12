package polyglot.ext.jl5.translate;

import polyglot.ast.CanonicalTypeNode;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.ext.jl5.types.JL5TypeSystem;
import polyglot.ext.jl5.types.inference.LubType;
import polyglot.ext.param.types.SubstClassType_c;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.CanonicalTypeNodeToExt_c;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;
import polyglot.types.Type;

public class JL5TypeNodeToJL_c extends ToExt_c
		implements ToExt {
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        TypeNode n = (TypeNode) node();
        return rw.typeToJava(n.type(), n.position());
    }
}
