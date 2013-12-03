package polyglot.ext.jl7.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.JLDel;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class JL7Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * The delegate object to invoke "superclass" functionality.
     * If null, this superclass functionality will by default be delegated
     * to the node. However, extensions to JL5 can override this if needed.
     */
    protected JLDel superDel = null;

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

    public JLDel superDel() {
        if (this.superDel == null) {
            return this.node();
        }
        return this.superDel;
    }

    @Override
    public void init(Node node) {
        super.init(node);
        if (superDel != null) {
            superDel.init(node);
        }
    }

    @Override
    public Object copy() {
        JL7Ext copy = (JL7Ext) super.copy();
        if (superDel != null) {
            copy.superDel = (JLDel) superDel.copy();
        }
        return copy;
    }

    public void setSuperDel(JLDel superDel) {
        this.superDel = superDel;
    }
}
