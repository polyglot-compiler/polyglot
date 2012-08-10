package polyglot.ext.jl5.translate;

import polyglot.ast.Node;
import polyglot.ext.jl5.ast.EnumConstant;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class EnumConstantToJL_c extends EnumConstantToExt_c {

    /**
     * Rewrite EnumConstants to Fields
     */
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        EnumConstant ec = (EnumConstant) node();
        return rw.to_nf().Field(ec.position(), ec.target(), ec.id());
    }

}
