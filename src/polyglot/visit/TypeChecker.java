package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;
import polyglot.types.Package;

/** Visitor which performs type checking on the AST. */
public class TypeChecker extends SemanticVisitor
{
    public TypeChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
    }

    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
	return n.del().typeCheckEnter(this);
    }

    protected Node overrideCall(Node n) throws SemanticException {
	Node m = n.del().typeCheckOverride(this);

	if (m instanceof Expr && ((Expr) m).type() == null) {
	    throw new InternalCompilerError("Null type for " + m, m.position());
	}

	return m;
    }

    protected Node leaveCall(Node n) throws SemanticException {
	Node m = n.del().typeCheck(this);

	if (m instanceof Expr && ((Expr) m).type() == null) {
	    throw new InternalCompilerError("Null type for " + m, m.position());
	}

	return m;
    }
}
