package polyglot.ext.pao.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.types.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import polyglot.util.*;

public class PaoBinaryDel_c extends PaoDel_c {
    // Rewrite == and != to invoke Primitive.equals(o, p).
    public Node rewrite(PaoTypeSystem ts, PaoNodeFactory nf) {
        Binary b = (Binary) node();
        Expr l = b.left();
        Expr r = b.right();

        if (b.operator() == Binary.EQ || b.operator() == Binary.NE) {
            MethodInstance mi = ((PaoTypeSystem) ts).primitiveEquals();

            if (ts.isSubtype(l.type(), mi.container()) ||
                ts.isSame(l.type(), ts.Object())) {

                // left is a boxed primitive
                if (r.type().isReference()) {
                    TypeNode x = nf.CanonicalTypeNode(b.position(),
                                                      mi.container());
                    Call y = nf.Call(b.position(), x, mi.name(), l, r);
                    y = (Call) y.type(mi.returnType());

                    if (b.operator() == Binary.NE) {
                        return nf.Unary(b.position(), Unary.NOT, y).type(mi.returnType());
                    }
                    else {
                        return y;
                    }
                }
            }
        }

        return super.rewrite(ts, nf);
    }
}
