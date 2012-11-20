package polyglot.translate.ext;

import polyglot.ast.LocalClassDecl;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class LocalClassDeclToExt_c extends ToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        LocalClassDecl cd = (LocalClassDecl) node();
        return rw.to_nf().LocalClassDecl(cd.position(), cd.decl());
    }
}
