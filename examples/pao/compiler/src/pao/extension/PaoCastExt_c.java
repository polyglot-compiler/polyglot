package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

public class PaoCastExt_c extends PaoExt_c {
    // Insert boxing and unboxing code.
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {

        Cast c = (Cast) node();
        Type rtype = c.expr().type();
        Type ltype = c.castType().type();

        if (ltype.isPrimitive() && rtype.isReference()) {
            // Unbox
            MethodInstance mi = ts.getter(ltype.toPrimitive());

            Cast x = nf.Cast(c.position(),
                             nf.CanonicalTypeNode(c.position(), mi.container()),
                             c.expr());
            x = (Cast) x.type(mi.container());

            Call y = nf.Call(c.position(), x, mi.name(),
                             Collections.EMPTY_LIST);
            y = (Call) y.type(mi.returnType());

            return y.methodInstance(mi);
        }
        else if (ltype.isReference() && rtype.isPrimitive()) {
            // Box
            ConstructorInstance ci = ts.wrapper(rtype.toPrimitive());

            List args = new ArrayList(1);
            args.add(c.expr());

            New x = nf.New(c.position(),
                           nf.CanonicalTypeNode(c.position(), ci.container()),
                           args);
            x = (New) x.type(ci.container());
            return x.constructorInstance(ci);
        }

        return super.rewrite(ts, nf);
    }
}
