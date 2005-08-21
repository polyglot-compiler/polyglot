package polyglot.visit;

import polyglot.ast.Node;
import polyglot.util.*;
import java.util.*;

/**
 * A PruningVisitor is used to prune the traversal of the AST at a
 * particular node.  Returning a PruningVisitor from the
 * NodeVisitor.enter method ensures no children will be visited.
 */
public class PruningVisitor extends NodeVisitor
{
    public Node override(Node parent, Node n) {
        return n;
    }
}
