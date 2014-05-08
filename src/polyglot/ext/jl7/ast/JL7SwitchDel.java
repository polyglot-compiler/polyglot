package polyglot.ext.jl7.ast;

import polyglot.ext.jl5.ast.JL5SwitchDel;
import polyglot.ext.jl5.ast.JL5SwitchOps;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;

public class JL7SwitchDel extends JL5SwitchDel implements JL5SwitchOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public boolean isAcceptableSwitchType(Type t) {
        return ((JL7SwitchExt) JL7Ext.ext(this.node())).isAcceptableSwitchType(t);

    }

}
