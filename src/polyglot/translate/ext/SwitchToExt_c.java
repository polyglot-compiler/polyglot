package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Switch;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class SwitchToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Switch n = (Switch) node();
        return rw.to_nf().Switch(n.position(), n.expr(), n.elements());
    }
}
