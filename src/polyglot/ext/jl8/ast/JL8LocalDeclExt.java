package polyglot.ext.jl8.ast;

import polyglot.ast.LocalDecl;
import polyglot.util.SerialVersionUID;

public class JL8LocalDeclExt extends JL8Ext {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public LocalDecl node() {
        return (LocalDecl) super.node();
    }
}
