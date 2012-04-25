package polyglot.visit;

import polyglot.ast.Node;
import polyglot.visit.HaltingVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Returns a deep copy of the AST.
 */
public class DeepCopy extends HaltingVisitor {
    public DeepCopy() {
        super();
    }

	public Node leave(Node old, Node n, NodeVisitor v) {
        return (Node)n.copy();
    }    
}
