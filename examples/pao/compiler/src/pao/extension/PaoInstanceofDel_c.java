/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.extension;

import polyglot.ast.Instanceof;
import polyglot.ast.JLDel_c;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.visit.TypeChecker;

/**
 * The implementation of the delegate for the 
 * <code>InstanceOf</code> AST node. Overrides the 
 * {@link #typeCheck(TypeChecker) typeCheck(TypeChecker)} method.
 */
public class PaoInstanceofDel_c extends JLDel_c {
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
}
