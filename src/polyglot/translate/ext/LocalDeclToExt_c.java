package polyglot.translate.ext;

import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;

public class LocalDeclToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        LocalDecl n = (LocalDecl) node();
        Type type = rw.to_ts().unknownType(n.position());
        LocalInstance li =
                rw.to_ts().localInstance(n.position(),
                                         n.flags(),
                                         type,
                                         n.name());
        LocalDecl to =
                rw.to_nf().LocalDecl(n.position(),
                                     n.flags(),
                                     n.type(),
                                     n.id(),
                                     n.init());
        return to.localInstance(li);
    }
}
