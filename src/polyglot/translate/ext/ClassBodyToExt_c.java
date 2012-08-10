package polyglot.translate.ext;

import polyglot.ast.ClassBody;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class ClassBodyToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        ClassBody cb = (ClassBody) node();
        return rw.to_nf().ClassBody(cb.position(), cb.members());
    }
}
