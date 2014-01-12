/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.extension;

import pao.types.PaoTypeSystem;
import polyglot.ast.Instanceof;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.Type;

/**
 * The <code>PaoExt</code> implementation for the 
 * <code>InstanceOf</code> AST node.
 */
public class PaoInstanceofExt_c extends PaoExt_c {
    /**
     * Rewrites <code>instanceof</code> checks where the comparison type is
     * a primitive type to use the boxed type instead. For example,
     * "e instanceof int" gets rewritten to 
     * "e instanceof pao.runtime.Integer".
     * 
     * @see PaoExt#rewrite(PaoTypeSystem, NodeFactory)
     */
    @Override
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {
        Instanceof n = (Instanceof) node();
        Type rtype = n.compareType().type();

        if (rtype.isPrimitive()) {
            Type t = ts.boxedType(rtype.toPrimitive());
            return n.compareType(nf.CanonicalTypeNode(n.compareType()
                                                       .position(), t));
        }

        return n;
    }
}
