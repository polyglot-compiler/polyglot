package jltools.ast;

import jltools.util.CodeWriter;
import jltools.util.Copy;
import jltools.types.SemanticException;
import jltools.types.TypeSystem;
import jltools.types.Context;

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
