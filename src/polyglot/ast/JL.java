package polyglot.ast;

import polyglot.util.CodeWriter;
import polyglot.util.Copy;
import polyglot.types.SemanticException;
import polyglot.types.Context;
import polyglot.visit.*;

/**
 * A <code>Node</code> represents an AST node.  All AST nodes must implement
 * this interface.  Nodes should be immutable: methods which set fields
 * of the node should copy the node, set the field in the copy, and then
 * return the copy.
 */
public interface JL extends NodeOps, Copy
{
    public void init(Node node);
    public Node node();
}
