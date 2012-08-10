package polyglot.translate.ext;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.visit.NodeVisitor;

public class ToExt_c extends Ext_c implements ToExt {
    public static ToExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof ToExt)) {
            e = e.ext();
        }
        return (ToExt) e;
    }

    @Override
    public NodeVisitor toExtEnter(ExtensionRewriter rw)
            throws SemanticException {
        return rw;
    }

    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        throw new InternalCompilerError("Cannot transalate " + node() + ":"
                + node().ext().getClass());
    }
}
