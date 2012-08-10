package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.ext.jl5.ast.JL5NodeFactory;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;

public class EnumConstantToExt_c extends ToExt_c implements ToExt {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        EnumConstant ec = (EnumConstant) node();
        return ((JL5NodeFactory) rw.to_nf()).EnumConstant(ec.position(),
                                                          ec.target(),
                                                          ec.id());
    }
}
