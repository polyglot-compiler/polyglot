package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;

/** Visitor which performs constant folding. */
public class ConstantFolder extends NodeVisitor
{
    TypeSystem ts;
    NodeFactory nf;

    public ConstantFolder(TypeSystem ts, NodeFactory nf) {
        this.ts = ts;
        this.nf = nf;
    }

    public TypeSystem typeSystem() {
      return ts;
    }

    public NodeFactory nodeFactory() {
      return nf;
    }

    public NodeVisitor enter(Node n) {
	return n.del().foldConstantsEnter(this);
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	return n.del().foldConstants(this);
    }
}
