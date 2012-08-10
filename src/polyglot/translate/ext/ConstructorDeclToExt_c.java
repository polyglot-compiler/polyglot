package polyglot.translate.ext;

import polyglot.ast.ConstructorDecl;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ConstructorDeclToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ConstructorDecl n = (ConstructorDecl) node();

        return rw.to_nf().ConstructorDecl(n.position(),
                                          n.flags(),
                                          n.id(),
                                          n.formals(),
                                          n.throwTypes(),
                                          n.body());
    }
}
