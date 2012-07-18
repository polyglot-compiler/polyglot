package polyglot.translate.ext;

import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ImportToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Import n = (Import) node();
        return rw.to_nf().Import(n.position(), n.kind(), n.name());
    }
}
