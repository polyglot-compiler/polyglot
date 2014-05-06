package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.ElementValueArrayInit;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class ElementValuePairArrayInitToExt_c extends ToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Node n = node();
        ElementValueArrayInit ext = (ElementValueArrayInit) n;
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();
        return to_nf.ElementValueArrayInit(n.position(), ext.elements());
    }

}
