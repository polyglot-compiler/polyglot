package polyglot.ext.jl8.ast;

import polyglot.ast.*;
import polyglot.ext.jl7.ast.J7Lang_c;
import polyglot.util.InternalCompilerError;

public class JL8Lang_c extends J7Lang_c implements JL8Lang {
    public static final JL8Lang_c instance = new JL8Lang_c();

    public static JL8Lang lang(NodeOps n) {
        while (n != null) {
            Lang lang = n.lang();
            if (lang instanceof JL8Lang) return (JL8Lang) lang;
            if (n instanceof Ext)
                n = ((Ext) n).pred();
            else return null;
        }
        throw new InternalCompilerError("Impossible to reach");
    }

    protected JL8Lang_c() {
    }

    protected static JL8Ext jl8Ext(Node n) {
        return JL8Ext.ext(n);
    }

    @Override
    protected NodeOps NodeOps(Node n) {
        return jl8Ext(n);
    }

    // TODO:  Implement dispatch methods for new AST operations.
    // TODO:  Override *Ops methods for AST nodes with new extension nodes.
}
