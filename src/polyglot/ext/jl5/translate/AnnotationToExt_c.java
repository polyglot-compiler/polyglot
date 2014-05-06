package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl5.ast.AnnotationElem;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.SerialVersionUID;

public class AnnotationToExt_c extends ToExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Term n = (Term) node();
        AnnotationElem ae = (AnnotationElem) n;
        JL5NodeFactory to_nf = (JL5NodeFactory) rw.to_nf();
        return to_nf.NormalAnnotationElem(n.position(),
                                          ae.typeName(),
                                          ae.elements());
    }

}
