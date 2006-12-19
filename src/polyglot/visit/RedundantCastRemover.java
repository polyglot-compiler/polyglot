/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * RedundantCastRemover.java
 * 
 * Author: nystrom
 * Creation date: Jun 5, 2005
 */
package polyglot.visit;

import java.util.*;

import polyglot.ast.*;
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
    
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof Cast) {
            Cast c = (Cast) n;
            Type castType = c.castType().type();
            Type exprType = c.expr().type();
            if (exprType.isImplicitCastValid(castType)) {
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
            List newArgs = new ArrayList(newCall.arguments().size());
            boolean changed = false;
            Iterator i = newCall.arguments().iterator();
            Iterator j = oldCall.arguments().iterator();
            while (i.hasNext() && j.hasNext()) {
                Expr newE = (Expr) i.next();
                Expr oldE = (Expr) j.next();
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
