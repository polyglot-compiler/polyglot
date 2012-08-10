package polyglot.translate.ext;

import polyglot.ast.Node;
import polyglot.ast.Local;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;

public class LocalToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Local n = (Local) node();
        Type type = rw.to_ts().unknownType(n.position());
        LocalInstance li =
                rw.to_ts().localInstance(n.position(),
                                         n.flags(),
                                         type,
                                         n.name());
        Local to = rw.to_nf().Local(n.position(), n.id());
        return to.localInstance(li);
    }
}
