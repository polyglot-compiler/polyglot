package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.Job;
import polyglot.types.Package;

/** Visitor which performs type checking on the AST. */
public class ExpectedTypeVisitor extends SemanticVisitor
{
    public ExpectedTypeVisitor(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf);
    }

    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        if (parent != null && n instanceof Expr) {
            n = parent.del().setExpectedType((Expr) n, this);
        }

	return super.enterCall(parent, n);
    }
}
