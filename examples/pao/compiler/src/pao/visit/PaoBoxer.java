package polyglot.ext.pao.visit;

import polyglot.ext.pao.types.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import java.util.*;

public class PaoBoxer extends AscriptionVisitor
{
    public PaoBoxer(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    public Expr ascribe(Expr e, Type toType) {
        Type fromType = e.type();

        if (toType == null) {
            return e;
        }

        Position p = e.position();

        // Insert a cast.  Translation of the cast will insert the
        // correct boxing/unboxing code.
        if (toType.isReference() && fromType.isPrimitive()) {
            return nf.Cast(p, nf.CanonicalTypeNode(p, ts.Object()), e);
        }

        return e;
    }

    public Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        n = super.leaveCall(old, n, v);

        // Insert boxing and unboxing code.
        if (n instanceof Cast) {
            Cast c = (Cast) n;
            Type rtype = c.expr().type();
            Type ltype = c.castType().type();
            TypeSystem ts = typeSystem();

            if (ltype.isPrimitive() && rtype.isReference()) {
                MethodInstance mi = ((PaoTypeSystem) ts).getter(ltype.toPrimitive());

                Cast x = nf.Cast(c.position(),
                                 nf.CanonicalTypeNode(c.position(),
                                                      mi.container()),
                                 c.expr());
                x = (Cast) x.type(mi.container());

                Call y = nf.Call(c.position(), x, mi.name(),
                                 Collections.EMPTY_LIST);
                y = (Call) y.type(mi.returnType());

                return y.methodInstance(mi);
            }
            else if (ltype.isReference() && rtype.isPrimitive()) {
                ConstructorInstance ci = ((PaoTypeSystem) ts).wrapper(rtype.toPrimitive());

                List args = new ArrayList(1);
                args.add(c.expr());

                New x = nf.New(c.position(),
                               nf.CanonicalTypeNode(c.position(),
                                                    ci.container()), args);
                x = (New) x.type(ci.container());
                return x.constructorInstance(ci);
            }
            else {
                return n;
            }
        }

        // Rewrite instanceof for primitives
        if (n instanceof Instanceof) {
            Instanceof o = (Instanceof) n;
            Type rtype = o.compareType().type();

            if (rtype.isPrimitive()) {
                ConstructorInstance ci = ((PaoTypeSystem) ts).wrapper(rtype.toPrimitive());
                return o.compareType(nf.CanonicalTypeNode(o.compareType().position(),
                                                          ci.container()));
            }
        }

        // TODO: Rewrite == and != to invoke Primitive.equals(o, p).
        if (n instanceof Binary) {
            Binary b = (Binary) n;
            Expr l = b.left();
            Expr r = b.right();

            if (b.operator() == Binary.EQ ||
                b.operator() == Binary.NE) {

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
        }

        return n;
    }
}
