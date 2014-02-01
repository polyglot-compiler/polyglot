package carray.ast;

import polyglot.ast.Ext;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class CarrayExt extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static CarrayExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof CarrayExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No Carray extension object for node "
                                                    + n
                                                    + " ("
                                                    + n.getClass()
                                                    + ")",
                                            n.position());
        }
        return (CarrayExt) e;
    }

    @Override
    public final CarrayLang_c lang() {
        return CarrayLang_c.instance;
    }
}
