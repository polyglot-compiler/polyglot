/*
 * RedundantCastRemover.java
 * 
 * Author: nystrom
 * Creation date: Jun 5, 2005
 */
package polyglot.visit;

import polyglot.ast.Cast;
import polyglot.ast.Node;
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
        return n;
    }
}
