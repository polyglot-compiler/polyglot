package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.ext.jl5.ast.ParamTypeNode;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;

public class ParamTypeNodeToExt_c extends ToExt_c implements ToExt {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ParamTypeNode ptn = (ParamTypeNode) node();
        return ((JL5NodeFactory) rw.to_nf()).ParamTypeNode(ptn.position(),
                                                           ptn.bounds(),
                                                           ptn.id());
    }
}
