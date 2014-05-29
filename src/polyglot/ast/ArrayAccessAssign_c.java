/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
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
import polyglot.util.SerialVersionUID;
import polyglot.visit.CFGBuilder;

/**
 * A {@code ArrayAccessAssign_c} represents a Java assignment expression
 * to an array element.  For instance, {@code A[3] = e}.
 * 
 * The class of the {@code Expr} returned by
 * {@code ArrayAccessAssign_c.left()}is guaranteed to be an
 * {@code ArrayAccess}.
 */
public class ArrayAccessAssign_c extends Assign_c implements ArrayAccessAssign {
    private static final long serialVersionUID = SerialVersionUID.generate();

//    @Deprecated
    public ArrayAccessAssign_c(Position pos, ArrayAccess left, Operator op,
            Expr right) {
        this(pos, left, op, right, null);
    }

    public ArrayAccessAssign_c(Position pos, ArrayAccess left, Operator op,
            Expr right, Ext ext) {
        super(pos, left, op, right, ext);
    }

    @Override
    public ArrayAccess left() {
        return (ArrayAccess) super.left();
    }

    @Override
    public Assign left(Expr left) {
        assertLeftType(left);
        return super.left(left);
    }

    private void assertLeftType(Expr left) {
        if (!(left instanceof ArrayAccess)) {
            throw new InternalCompilerError("left expression of an ArrayAccessAssign must be an array access");
        }
    }

    @Override
    public Term firstChild() {
        if (operator() == ASSIGN) {
            return left().array();
        }
        else {
            return left();
        }
    }

    @Override
    protected void acceptCFGAssign(CFGBuilder<?> v) {
        ArrayAccess a = left();

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
        List<Type> l = new ArrayList<>(super.throwTypes(ts));

        if (op == ASSIGN && left.type().isReference()) {
            l.add(ts.ArrayStoreException());
        }

        l.add(ts.NullPointerException());
        l.add(ts.OutOfBoundsException());

        return l;
    }

}
