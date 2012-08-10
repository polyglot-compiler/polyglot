package polyglot.translate.ext;

import polyglot.ast.FieldDecl;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.FieldInstance;
import polyglot.types.SemanticException;

public class FieldDeclToExt_c extends ToExt_c {
    protected FieldInstance fi = null;

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        FieldDecl n = (FieldDecl) node();
        return rw.to_nf().FieldDecl(n.position(),
                                    n.flags(),
                                    n.type(),
                                    n.id(),
                                    n.init());
    }
}
