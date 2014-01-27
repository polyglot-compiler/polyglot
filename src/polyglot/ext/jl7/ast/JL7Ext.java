package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.ext.jl5.ast.J5Lang;
import polyglot.ext.jl5.ast.J5Lang_c;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class JL7Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JL7Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JL7Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No JL7 extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (JL7Ext) e;
    }

    @Override
    protected final J5Lang superLang() {
        return J5Lang_c.instance;
    }
}
