package polyglot.translate.ext;

import polyglot.ast.Eval;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class EvalToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Eval n = (Eval) node();
        return rw.to_nf().Eval(n.position(), n.expr());
    }
}
