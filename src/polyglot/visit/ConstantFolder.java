package jltools.visit;

import jltools.ast.*;
import jltools.types.TypeSystem;
import jltools.frontend.Pass;
import jltools.frontend.Job;

/** Visitor which performs constant folding. */
public class ConstantFolder extends BaseVisitor
{
    public ConstantFolder(Job job) {
        super(job);
    }

    public Node override(Node n) {
	return n.ext().foldConstantsOverride(this);
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	return n.ext().foldConstants(this);
    }
}
