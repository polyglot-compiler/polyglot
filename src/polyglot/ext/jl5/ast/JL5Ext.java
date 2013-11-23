package polyglot.ext.jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.JL;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class JL5Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * The delegate object to invoke "superclass" functionality.
     * If null, this superclass functionality will by default be delegated
     * to the node. However, extensions to JL5 can override this if needed.
     */
    protected JL superDel = null;

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

    public JL superDel() {
        if (this.superDel == null) {
            return this.node();
        }
        return this.superDel;
    }

    public void setSuperDel(JL superDel) {
        this.superDel = superDel;
    }
}
