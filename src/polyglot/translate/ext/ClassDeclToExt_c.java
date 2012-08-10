package polyglot.translate.ext;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ClassDeclToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ClassDecl cd = (ClassDecl) node();
        return rw.to_nf().ClassDecl(cd.position(),
                                    cd.flags(),
                                    cd.id(),
                                    cd.superClass(),
                                    cd.interfaces(),
                                    cd.body());
    }
}
