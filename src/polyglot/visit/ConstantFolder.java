package jltools.visit;

import jltools.ast.*;
import jltools.types.TypeSystem;
import jltools.frontend.Job;

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

    public Node enter(Node n) {
	return n.del().foldConstantsEnter(this);
    }

    public Node override(Node n) {
	return n.del().foldConstantsOverride(this);
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	return n.del().foldConstants(this);
    }
}
