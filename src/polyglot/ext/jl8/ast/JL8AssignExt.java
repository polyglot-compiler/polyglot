package polyglot.ext.jl8.ast;

import polyglot.ast.Assign;
import polyglot.util.SerialVersionUID;

public abstract class JL8AssignExt extends JL8Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Assign node() {
        return (Assign) super.node();
    }
}
