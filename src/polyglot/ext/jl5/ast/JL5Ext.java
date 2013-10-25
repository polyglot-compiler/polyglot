package polyglot.ext.jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class JL5Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static JL5Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof JL5Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No JL5 extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (JL5Ext) e;
    }

}
