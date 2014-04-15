package carray_jl5.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class CarrayJL5Ext extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static CarrayJL5Ext ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof CarrayJL5Ext)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No CarrayJL5 extension object for node "
                                                    + n
                                                    + " ("
                                                    + n.getClass()
                                                    + ")",
                                            n.position());
        }
        return (CarrayJL5Ext) e;
    }

    @Override
    public final CarrayJL5Lang_c lang() {
        return CarrayJL5Lang_c.instance;
    }
}
