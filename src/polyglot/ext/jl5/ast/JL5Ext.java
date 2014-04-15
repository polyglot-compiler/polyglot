package polyglot.ext.jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.JLDel;
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
    protected JLDel superDel = null;

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

    @Override
    public void init(Node node) {
        super.init(node);
        if (superDel != null) {
            superDel.init(node);
        }
    }

    @Override
    public Object copy() {
        JL5Ext copy = (JL5Ext) super.copy();
        if (superDel != null) {
            copy.superDel = (JLDel) superDel.copy();
        }
        return copy;
    }

    public JLDel superDel() {
        if (this.superDel == null) {
            return this.node();
        }
        return this.superDel;
    }

    public void setSuperDel(JLDel superDel) {
        this.superDel = superDel;
    }
}
