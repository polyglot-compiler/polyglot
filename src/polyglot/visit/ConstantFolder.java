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

package polyglot.visit;

import polyglot.ast.Binary;
import polyglot.ast.Expr;
import polyglot.ast.FloatLit;
import polyglot.ast.IntLit;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/** Visitor which performs constant folding. */
public class ConstantFolder extends NodeVisitor {
    protected TypeSystem ts;
    protected NodeFactory nf;

    public ConstantFolder(TypeSystem ts, NodeFactory nf) {
        this.ts = ts;
        this.nf = nf;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public NodeFactory nodeFactory() {
        return nf;
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v_) {
        if (!(n instanceof Expr)) {
            return n;
        }

        Expr e = (Expr) n;

        if (!e.isConstant()) {
            return e;
        }

        // Don't fold String +.  Strings are often broken up for better
        // formatting.
        if (e instanceof Binary) {
            Binary b = (Binary) e;

            if (b.operator() == Binary.ADD
                    && b.left().constantValue() instanceof String
                    && b.right().constantValue() instanceof String) {

                return b;
            }
        }

        Object v = e.constantValue();
        Position pos = e.position();

        if (v == null) {
            return nf.NullLit(pos).type(ts.Null());
        }
        if (v instanceof String) {
            return nf.StringLit(pos, (String) v).type(ts.String());
        }
        if (v instanceof Boolean) {
            return nf.BooleanLit(pos, ((Boolean) v).booleanValue())
                     .type(ts.Boolean());
        }
        if (v instanceof Double) {
            return nf.FloatLit(pos, FloatLit.DOUBLE, ((Double) v).doubleValue())
                     .type(ts.Double());
        }
        if (v instanceof Float) {
            return nf.FloatLit(pos, FloatLit.FLOAT, ((Float) v).floatValue())
                     .type(ts.Float());
        }
        if (v instanceof Long) {
            return nf.IntLit(pos, IntLit.LONG, ((Long) v).longValue())
                     .type(ts.Long());
        }
        if (v instanceof Integer) {
            return nf.IntLit(pos, IntLit.INT, ((Integer) v).intValue())
                     .type(ts.Int());
        }
        if (v instanceof Character) {
            return nf.CharLit(pos, ((Character) v).charValue()).type(ts.Char());
        }

        return e;
    }
}
