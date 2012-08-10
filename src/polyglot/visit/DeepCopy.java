package polyglot.visit;

import polyglot.ast.Node;

/**
 * Returns a deep copy of the AST.
 */
public class DeepCopy extends HaltingVisitor {
    public DeepCopy() {
        super();
    }

    @Override
    public Node leave(Node old, Node n, NodeVisitor v) {
        return (Node) n.copy();
    }
}
