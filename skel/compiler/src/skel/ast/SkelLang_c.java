package skelpkg.ast;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;

public class SkelLang_c extends JLang_c implements SkelLang {
    public static final SkelLang_c instance = new SkelLang_c();

    public static SkelLang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof SkelLang) return (SkelLang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected SkelLang_c() {
    }

    protected static SkelExt skelExt(Node n) {
        return SkelExt.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return skelExt(n);
    }

    // TODO:  Implement dispatch methods for new AST operations.
    // TODO:  Override *Ops methods for AST nodes with new extension nodes.
}
