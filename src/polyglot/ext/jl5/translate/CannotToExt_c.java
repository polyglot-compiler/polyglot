package polyglot.ext.jl5.translate;

import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.translate.ExtensionRewriter;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;

public class CannotToExt_c extends ToExt_c implements Ext {
    @Override
    public Node toExt(ExtensionRewriter rw) throws SemanticException {
        throw new InternalCompilerError("Cannot transalate " + node() + ":"
                + node().getClass());
    }
}
