package polyglot.ast;

import polyglot.util.Copy;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 */
public interface Ext extends Copy
{
    /**
     * The node which we are extending, or this.
     */
    Node node();

    /**
     * Initialize the Ext with a Node.
     */
    void init(Node node);
}
