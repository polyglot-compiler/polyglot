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

import java.util.ArrayList;
import java.util.List;

import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;

/**
 * A <code>ArrayAccessAssign_c</code> represents a Java assignment expression
 * to an array element.  For instance, <code>A[3] = e</code>.
 * 
 * The class of the <code>Expr</code> returned by
 * <code>ArrayAccessAssign_c.left()</code>is guaranteed to be an
 * <code>ArrayAccess</code>.
 */
public class ArrayAccessAssign_c extends Assign_c implements ArrayAccessAssign {
    public ArrayAccessAssign_c(Position pos, ArrayAccess left, Operator op,
            Expr right) {
        super(pos, left, op, right);
    }

    @Override
    public Assign left(Expr left) {
        ArrayAccessAssign_c n = (ArrayAccessAssign_c) super.left(left);
        n.assertLeftType();
        return n;
    }

    private void assertLeftType() {
        if (!(left() instanceof ArrayAccess)) {
            throw new InternalCompilerError("left expression of an ArrayAccessAssign must be an array access");
        }
    }

    @Override
    public Term firstChild() {
        if (operator() == ASSIGN) {
            return ((ArrayAccess) left()).array();
        }
        else {
            return left();
        }
    }

    @Override
    protected void acceptCFGAssign(CFGBuilder<?> v) {
        ArrayAccess a = (ArrayAccess) left();

        //    a[i] = e: visit a -> i -> e -> (a[i] = e)
        v.visitCFG(a.array(), a.index(), ENTRY);
        v.visitCFG(a.index(), right(), ENTRY);
        v.visitCFG(right(), this, EXIT);
    }

    @Override
    protected void acceptCFGOpAssign(CFGBuilder<?> v) {
        /*
        ArrayAccess a = (ArrayAccess)left();
        
        // a[i] OP= e: visit a -> i -> a[i] -> e -> (a[i] OP= e)
        v.visitCFG(a.array(), a.index().entry());
        v.visitCFG(a.index(), a);
        v.visitThrow(a);
        v.edge(a, right().entry());
        v.visitCFG(right(), this);
        */

        v.visitCFG(left(), right(), ENTRY);
        v.visitCFG(right(), this, EXIT);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new ArrayList<Type>(super.throwTypes(ts));

        if (throwsArrayStoreException()) {
            l.add(ts.ArrayStoreException());
        }

        l.add(ts.NullPointerException());
        l.add(ts.OutOfBoundsException());

        return l;
    }

    /** Get the throwsArrayStoreException of the expression. */
    @Override
    public boolean throwsArrayStoreException() {
        return op == ASSIGN && left.type().isReference();
    }
}
