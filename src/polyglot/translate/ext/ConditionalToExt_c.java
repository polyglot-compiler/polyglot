package polyglot.translate.ext;

import polyglot.ast.Conditional;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ConditionalToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Conditional n = (Conditional) node();
        return rw.to_nf().Conditional(n.position(),
                                      n.cond(),
                                      n.consequent(),
                                      n.alternative());
    }
}
