package polyglot.ext.jl5.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;

public class CastingTypeChecker extends TypeChecker {

	public CastingTypeChecker(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	@Override
	protected Node leaveCall(Node old, Node n, NodeVisitor v)
			throws SemanticException {
		TypeChecker tc = (TypeChecker) v;
		TVCaster tvc = new TVCaster(job, ts, nf);
		tvc = (TVCaster) tvc.begin();
		tvc = (TVCaster) tvc.context(tc.context());
		old = n;
        n = n.visit(tvc);
		return super.leaveCall(old, n, v);
	}	
}
