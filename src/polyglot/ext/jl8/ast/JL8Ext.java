package polyglot.ext.jl8.ast;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class JL8Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JL8Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JL8Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No jl8 extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (JL8Ext) e;
    }

    @Override
    public final JL8Lang lang() {
        return JL8Lang_c.instance;
    }

    // TODO:  Override operation methods for overridden AST operations.
}
