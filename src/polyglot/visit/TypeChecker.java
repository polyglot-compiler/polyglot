package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Job;
import jltools.types.Package;

/** Visitor which performs type checking on the AST. */
public class TypeChecker extends SemanticVisitor
{
    public TypeChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
    }

    protected Node enterCall(Node parent, Node n) throws SemanticException {
	return n.ext().typeCheckEnter(this);
    }

    protected Node overrideCall(Node n) throws SemanticException {
	Node m = n.ext().typeCheckOverride(this);

	if (m instanceof Expr && ((Expr) m).type() == null) {
	    throw new InternalCompilerError("Null type for " + m, m.position());
	}

	return m;
    }

    protected Node leaveCall(Node n) throws SemanticException {
	Node m = n.ext().typeCheck(this);

	if (m instanceof Expr && ((Expr) m).type() == null) {
	    throw new InternalCompilerError("Null type for " + m, m.position());
	}

	return m;
    }
}
