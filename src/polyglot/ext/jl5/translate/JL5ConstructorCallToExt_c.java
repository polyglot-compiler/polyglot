package polyglot.ext.jl5.translate;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.JL5ConstructorCallExt;
import polyglot.ext.jl5.ast.JL5Ext;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ConstructorCallToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class JL5ConstructorCallToExt_c extends ConstructorCallToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ConstructorCall n = (ConstructorCall) node();
        JL5ConstructorCallExt ext = (JL5ConstructorCallExt) JL5Ext.ext(n);
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();
        ConstructorCall m =
                to_nf.ConstructorCall(n.position(),
                                      n.kind(),
                                      ext.typeArgs(),
                                      n.qualifier(),
                                      n.arguments(),
                                      ext.isEnumConstructorCall());
        return m;
    }
}
