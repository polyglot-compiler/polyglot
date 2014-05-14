package skelpkg.ast;

import polyglot.ast.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

public class SkelExt extends Ext_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    public static SkelExt ext(Node n) {
        Ext e = n.ext();
        while (e != null && !(e instanceof SkelExt)) {
            e = e.ext();
        }
        if (e == null) {
            throw new InternalCompilerError("No Skel extension object for node "
                    + n + " (" + n.getClass() + ")", n.position());
        }
        return (SkelExt) e;
    }

    @Override
    public final SkelLang lang() {
        return SkelLang_c.instance;
    }

    // TODO:  Override operation methods for overridden AST operations.
}
