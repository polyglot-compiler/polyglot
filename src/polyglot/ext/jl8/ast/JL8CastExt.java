package polyglot.ext.jl8.ast;

import polyglot.ast.Cast;
import polyglot.util.SerialVersionUID;

public class JL8CastExt extends JL8Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Cast node() {
        return (Cast) super.node();
    }
}
