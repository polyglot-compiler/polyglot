package polyglot.ast;

import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.Context;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It declares the methods which implement compiler passes.
 */
public interface Ext extends Copy
{
    /**
     * The node which we are ultimately extending, or this.
     */
    Node node();

    /**
     * Initialize the Ext with a Node.
     */
    void init(Node node);
}
