package polyglot.ast;

import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.Context;

/**
 * <code>Del</code> is the super type of all node delegate objects.
 * It declares the methods which implement compiler passes.
 */
public interface Del extends NodeOps, Copy
{
    /**
     * The node which we are ultimately extending, or this.
     */
    Node node();

    /**
     * Initialize the Del with a Node.
     */
    void init(Node node);
}
