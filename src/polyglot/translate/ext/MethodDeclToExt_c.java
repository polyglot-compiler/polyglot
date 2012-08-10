package polyglot.translate.ext;

import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class MethodDeclToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        MethodDecl n = (MethodDecl) node();
        return rw.to_nf().MethodDecl(n.position(),
                                     n.flags(),
                                     n.returnType(),
                                     n.id(),
                                     n.formals(),
                                     n.throwTypes(),
                                     n.body());
    }
}
