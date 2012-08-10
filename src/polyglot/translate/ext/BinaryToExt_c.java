package polyglot.translate.ext;

import polyglot.ast.Binary;
import polyglot.ast.Node;
import polyglot.ast.Precedence;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;

public class BinaryToExt_c extends ToExt_c {

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        Binary b = (Binary) node();
        Precedence precedence = b.precedence();
        b = rw.to_nf().Binary(b.position(), b.left(), b.operator(), b.right());
        b = b.precedence(precedence);
        return b;
    }

}
