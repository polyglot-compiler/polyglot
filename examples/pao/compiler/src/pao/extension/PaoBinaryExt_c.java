/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.extension;

import pao.types.PaoTypeSystem;
import polyglot.ast.Binary;
import polyglot.ast.Call;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.TypeNode;
import polyglot.ast.Unary;
import polyglot.types.MethodInstance;
import polyglot.util.SerialVersionUID;

/**
 * The <code>PaoExt</code> implementation for the 
 * <code>Binary</code> AST node.
 */
public class PaoBinaryExt_c extends PaoExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Rewrite the binary operators <code>==</code> and <code>&excl;=</code> to 
     * invoke <code>Primitive.equals(o, p)</code>.
     * 
     * @see PaoExt#rewrite(PaoTypeSystem, NodeFactory)
     * @see pao.runtime.Primitive#equals(Object, Object)
     */
    @Override
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {
        Binary b = (Binary) node();
        Expr l = b.left();
        Expr r = b.right();

        if (b.operator() == Binary.EQ || b.operator() == Binary.NE) {
            MethodInstance mi = ts.primitiveEquals();

            // The container of mi, mi.container(), is the super class of
            // the runtime boxed representations of primitive values.
            if (ts.isSubtype(l.type(), mi.container())
                    || ts.equals(l.type(), ts.Object())) {
                // The left operand is either a subtype of
                // pao.runtime.Primitive, or it is an
                // Object, and thus possibly a subtype of 
                // pao.runtime.Primitive. Either way,
                // it may be a boxed primitive.
                if (r.type().isReference()) {
                    // The right operand is a reference type, so replace the
                    // binary operation with a call to
                    // Primitive.equals(Object, Object).
                    TypeNode x =
                            nf.CanonicalTypeNode(b.position(), mi.container());
                    Call y =
                            nf.Call(b.position(),
                                    x,
                                    nf.Id(mi.position(), mi.name()),
                                    l,
                                    r);
                    y = (Call) y.type(mi.returnType());
                    if (b.operator() == Binary.NE) {
                        return nf.Unary(b.position(), Unary.NOT, y)
                                 .type(mi.returnType());
                    }
                    else {
                        return y;
                    }
                }
            }
        }

        // we do not need to rewrite the binary operator.
        return super.rewrite(ts, nf);
    }
}
