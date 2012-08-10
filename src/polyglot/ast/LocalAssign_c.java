/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;

/**
 * A <code>LocalAssign_c</code> represents a Java assignment expression
 * to a local variable.  For instance, <code>x = e</code>.
 * 
 * The class of the <code>Expr</code> returned by
 * <code>LocalAssign_c.left()</code>is guaranteed to be an <code>Local</code>.
 */
public class LocalAssign_c extends Assign_c implements LocalAssign {
    public LocalAssign_c(Position pos, Local left, Operator op, Expr right) {
        super(pos, left, op, right);
    }

    @Override
    public Assign left(Expr left) {
        LocalAssign_c n = (LocalAssign_c) super.left(left);
        n.assertLeftType();
        return n;
    }

    private void assertLeftType() {
        if (!(left() instanceof Local)) {
            throw new InternalCompilerError("left expression of an LocalAssign must be a local");
        }
    }

    @Override
    public Term firstChild() {
        if (operator() != Assign.ASSIGN) {
            return left();
        }

        return right();
    }

    @Override
    protected void acceptCFGAssign(CFGBuilder<?> v) {
        // do not visit left()
        // l = e: visit e -> (l = e)      
        v.visitCFG(right(), this, EXIT);
    }

    @Override
    protected void acceptCFGOpAssign(CFGBuilder<?> v) {
        /*
        Local l = (Local)left();
        
        // l OP= e: visit l -> e -> (l OP= e)
        v.visitThrow(l);
        v.edge(l, right().entry());
        v.visitCFG(right(), this);
        */

        v.visitCFG(left(), right(), ENTRY);
        v.visitCFG(right(), this, EXIT);
    }
}
