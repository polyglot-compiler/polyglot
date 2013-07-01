package polyglot.ext.jl5.translate;

import polyglot.ast.Call;
import polyglot.ast.Node;
import polyglot.ast.Receiver;
import polyglot.ext.jl5.ast.JL5Call;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.CallToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class JL5CallToExt_c extends CallToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        JL5Call n = (JL5Call) node();
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();
        Receiver target = n.target();
        if (!translateTarget(n)) {
            target = null;
        }
        Call m =
                to_nf.Call(n.position(),
                           target,
                           n.typeArgs(),
                           n.id(),
                           n.arguments());
        m = m.targetImplicit(n.isTargetImplicit());
        return m;
    }
}
