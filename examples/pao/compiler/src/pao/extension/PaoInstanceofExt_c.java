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
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.util.SerialVersionUID;
import polyglot.visit.TypeChecker;

/**
 * The <code>PaoExt</code> implementation for the 
 * <code>InstanceOf</code> AST node.
 */
public class PaoInstanceofExt_c extends PaoExt_c {
    private static final long serialVersionUID = SerialVersionUID.generate();

    /**
     * Removes the restriction that the compare type must be a 
     * <code>ReferenceType</code>. 
     * @see polyglot.ast.NodeOps#typeCheck(TypeChecker)
     * @see polyglot.ast.Instanceof_c#typeCheck(TypeChecker)
     */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Instanceof n = (Instanceof) node();
        Type rtype = n.compareType().type();
        Type ltype = n.expr().type();

        if (!tc.typeSystem().isCastValid(ltype, rtype)) {
            throw new SemanticException("Left operand of \"instanceof\" must be castable to "
                    + "the right operand.");
        }

        return n.type(tc.typeSystem().Boolean());
    }

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
