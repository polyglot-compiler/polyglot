package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * A <code>Special</code> is an immutable representation of a
 * reference to <code>this</code> or <code>super</code in Java.  This
 * reference can be optionally qualified with a type such as 
 * <code>Foo.this</code>.
 */
public class Special_c extends Expr_c implements Special
{
    protected Special.Kind kind;
    protected TypeNode qualifier;

    public Special_c(Ext ext, Position pos, Special.Kind kind, TypeNode qualifier) {
	super(ext, pos);
	this.kind = kind;
	this.qualifier = qualifier;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public Special.Kind kind() {
	return this.kind;
    }

    public Special kind(Special.Kind kind) {
	Special_c n = (Special_c) copy();
	n.kind = kind;
	return n;
    }

    public TypeNode qualifier() {
	return this.qualifier;
    }

    public Special qualifier(TypeNode qualifier) {
	Special_c n = (Special_c) copy();
	n.qualifier = qualifier;
	return n;
    }

    protected Special_c reconstruct(TypeNode qualifier) {
	if (qualifier != this.qualifier) {
	    Special_c n = (Special_c) copy();
	    n.qualifier = qualifier;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypeNode qualifier = null;

	if (this.qualifier != null) {
	    qualifier = (TypeNode) this.qualifier.visit(v);
	}

	return reconstruct(qualifier);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	ClassType t;

	if (qualifier == null) {
	    // Unqualified this expression
	    t = tc.context().currentClass();
	}
	else {
	    if (! qualifier.type().isClass()) {
		throw new SemanticException("Qualified " + kind +
		    " expression must be of a class type",
		    qualifier.position());
	    }

	    t = qualifier.type().toClass();
	}

	if (kind == THIS) {
	    return type(t);
	}
	else {
	    return type(t.superType());
	}
    }

    public String toString() {
	return (qualifier != null ? qualifier + "." : "") + kind;
    }

    public void translate_(CodeWriter w, Translator tr) {
	if (qualifier != null) {
	    qualifier.ext().translate(w, tr);
	    w.write(".");
	}

	w.write(kind.toString());
    }
}
