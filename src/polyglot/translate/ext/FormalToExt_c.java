package polyglot.translate.ext;

import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;

public class FormalToExt_c extends ToExt_c {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Formal n = (Formal) node();

        Formal to =
                rw.nodeFactory().Formal(n.position(),
                                        n.flags(),
                                        n.type(),
                                        n.id());
        Type type = rw.to_ts().unknownType(n.position());
        LocalInstance li =
                rw.to_ts().localInstance(n.position(),
                                         n.flags(),
                                         type,
                                         n.name());
        return to.localInstance(li);
    }
}
