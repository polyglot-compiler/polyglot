package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Job;
import jltools.types.Package;

/** Visitor which performs type checking on the AST. */
public class TypeChecker extends SemanticVisitor
{
    public TypeChecker(Job job) {
	super(job);
    }

    public TypeBuilder typeBuilder(Package p) {
        TypeBuilder tb = new TypeBuilder(job);
	tb.currentPackage = p;
	return tb;
    }

    public TypeAmbiguityRemover typeAmbiguityRemover() {
        TypeAmbiguityRemover sc = new TypeAmbiguityRemover(job);
	sc.context = context;
	return sc;
    }

    public AmbiguityRemover ambiguityRemover() {
        AmbiguityRemover ar = new AmbiguityRemover(job);
	ar.context = context;
	return ar;
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
