package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>Field</code> is an immutable representation of a Java field
 * access.  It consists of field name and may also have either a 
 * <code>Type</code> or an <code>Expr</code> containing the field being 
 * accessed.
 */
public class Field_c extends Expr_c implements Field
{
    protected Receiver target;
    protected String name;
    protected FieldInstance fi;

    public Field_c(Ext ext, Position pos, Receiver target, String name) {
	super(ext, pos);
	this.target = target;
	this.name = name;

	if (target == null) {
	    throw new InternalCompilerError("Cannot create a field with a " +
		"null target.  Use AmbExpr or prefix with the appropriate " +
		"type node or this.");
	}
    }

    /** Get the precedence of the field. */
    public Precedence precedence() { 
	return Precedence.LITERAL;
    }

    /** Get the target of the field. */
    public Receiver target() {
	return this.target;
    }

    /** Set the target of the field. */
    public Field target(Receiver target) {
	Field_c n = (Field_c) copy();
	n.target = target;
	return n;
    }

    /** Get the name of the field. */
    public String name() {
	return this.name;
    }

    /** Set the name of the field. */
    public Field name(String name) {
	Field_c n = (Field_c) copy();
	n.name = name;
	return n;
    }

    /** Get the field instance of the field. */
    public FieldInstance fieldInstance() {
	return fi;
    }

    /** Set the field instance of the field. */
    public Field fieldInstance(FieldInstance fi) {
	if (! fi.type().isCanonical()) {
	    throw new InternalCompilerError("Type of " + fi + " in " +
		fi.container() + " is not canonical.");
	}

        Field_c n = (Field_c) copy();
	n.fi = fi;
	return n;
    }

    /** Reconstruct the field. */
    protected Field_c reconstruct(Receiver target) {
	if (target != this.target) {
	    Field_c n = (Field_c) copy();
	    n.target = target;
	    return n;
	}

	return this;
    }

    /** Visit the children of the field. */
    public Node visitChildren(NodeVisitor v) {
	Receiver target = (Receiver) this.target.visit(v);
	return reconstruct(target);
    }

    /** Type check the field. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        TypeSystem ts = tc.typeSystem();

	if (target instanceof TypeNode) {
	    Type type = ((TypeNode) target).type();
	    FieldInstance fi = ts.findField(type.toReference(), name, c);

	    // Check that we don't access a non-static field from a static
	    // context.
	    if (! fi.flags().isStatic()) {
		throw new SemanticException(
		    "Cannot access non-static field " + name +
		    " of " + type + " is static context.");
	    }

	    return fieldInstance(fi).type(fi.type());
	}

	if (target instanceof Expr) {
	    Type type = ((Expr) target).type();
	    FieldInstance fi = ts.findField(type.toReference(), name, c);
	    return fieldInstance(fi).type(fi.type());
	}

	throw new InternalCompilerError(
	    "Cannot access field on node of type " +
	    target.getClass().getName() + ".");
    }

    /** Check exceptions thrown by the field. */
    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
	TypeSystem ts = ec.typeSystem();

	if (target instanceof Expr && ! (target instanceof Special)) {
	    ec.throwsException(ts.NullPointerException());
	}

	return this;
    }

    public String toString() {
	return (target != null ? target + "." : "") + name;
    }

    /** Write the field to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	if (target instanceof Expr) {
	    translateSubexpr((Expr) target, w, tr);
	}
	else if (target instanceof TypeNode) {
	    target.ext().translate(w, tr);
	}

	w.write(".");
	w.write(name);
    }
}
