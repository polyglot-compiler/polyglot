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
 * A <code>FieldAssign_c</code> represents a Java assignment expression to
 * a field.  For instance, <code>this.x = e</code>.
 * 
 * The class of the <code>Expr</code> returned by
 * <code>FieldAssign_c.left()</code>is guaranteed to be a <code>Field</code>.
 */
public class FieldAssign_c extends Assign_c implements FieldAssign {
    public FieldAssign_c(Position pos, Field left, Operator op, Expr right) {
        super(pos, left, op, right);
    }

    @Override
    public Assign left(Expr left) {
        FieldAssign_c n = (FieldAssign_c) super.left(left);
        n.assertLeftType();
        return n;
    }

    private void assertLeftType() {
        if (!(left() instanceof Field)) {
            throw new InternalCompilerError("left expression of an FieldAssign must be a field");
        }
    }

    @Override
    public Term firstChild() {
        Field f = (Field) left();
        if (f.target() instanceof Expr) {
            return ((Expr) f.target());
        }
        else {
            if (operator() != Assign.ASSIGN) {
                return f;
            }
            else {
                return right();
            }
        }
    }

    @Override
    protected void acceptCFGAssign(CFGBuilder<?> v) {
        Field f = (Field) left();
        if (f.target() instanceof Expr) {
            Expr o = (Expr) f.target();

            //     o.f = e: visit o -> e -> (o.f = e)
            v.visitCFG(o, right(), ENTRY);
            v.visitCFG(right(), this, EXIT);
        }
        else {
            //       T.f = e: visit e -> (T.f OP= e)
            v.visitCFG(right(), this, EXIT);
        }

    }

    @Override
    protected void acceptCFGOpAssign(CFGBuilder<?> v) {
        /*
        Field f = (Field)left();
        if (f.target() instanceof Expr) {
            Expr o = (Expr) f.target();

            // o.f OP= e: visit o -> o.f -> e -> (o.f OP= e)
            v.visitCFG(o, f);
            v.visitThrow(f);
            v.edge(f, right().entry());
            v.visitCFG(right(), this);
        }
        else {
            // T.f OP= e: visit T.f -> e -> (T.f OP= e)
            v.visitThrow(f);
            v.edge(f, right().entry());
            v.visitCFG(right(), this);
        }
        */

        v.visitCFG(left(), right(), ENTRY);
        v.visitCFG(right(), this, EXIT);
    }

    @Override
    public List<Type> throwTypes(TypeSystem ts) {
        List<Type> l = new ArrayList<Type>(super.throwTypes(ts));

        Field f = (Field) left();
        if (f.target() instanceof Expr) {
            l.add(ts.NullPointerException());
        }

        return l;
    }

}
