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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Cast;
import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.ast.ProcedureCall;
import polyglot.types.Type;

/**
 * <code>RedundantCastRemover</code> removes redundant casts.  It's typically
 * used to clean up inefficient translations from the source language to Java.
 * The AST must be type-checked before using this visitor.
 */
public class RedundantCastRemover extends NodeVisitor {
    public RedundantCastRemover() {
        super();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof Cast) {
            Cast c = (Cast) n;
            Type castType = c.castType().type();
            Type exprType = c.expr().type();
            if (exprType.isImplicitCastValid(castType)
                    && !castType.isPrimitive()) {
                // Redundant cast.
                return c.expr();
            }
        }

        // Do not remove redundant casts from call arguments since the
        // cast may be there to resolve an ambiguity or to force another
        // overloaded method to be called.
        if (n instanceof ProcedureCall) {
            ProcedureCall newCall = (ProcedureCall) n;
            ProcedureCall oldCall = (ProcedureCall) old;
            List<Expr> newArgs =
                    new ArrayList<Expr>(newCall.arguments().size());
            boolean changed = false;
            Iterator<Expr> i = newCall.arguments().iterator();
            Iterator<Expr> j = oldCall.arguments().iterator();
            while (i.hasNext() && j.hasNext()) {
                Expr newE = i.next();
                Expr oldE = j.next();
                if (oldE instanceof Cast) {
                    newArgs.add(oldE);
                    changed = true;
                }
                else {
                    newArgs.add(newE);
                }
            }
            if (changed) {
                n = newCall.arguments(newArgs);
            }
        }
        return n;
    }
}
