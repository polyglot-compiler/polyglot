package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>FieldDecl</code> is an immutable representation of the declaration 
 * of a field of a class.
 */
public class FieldDecl_c extends Node_c implements FieldDecl
{
    Declarator decl;
    FieldInstance fi;

    public FieldDecl_c(Ext ext, Position pos, Flags flags, TypeNode type, String name, Expr init) {
        super(ext, pos);
	this.decl = new Declarator_c(flags, type, name, init);
    }

    /** Get the type of the declaration. */
    public Type declType() {
        return decl.declType();
    }

    /** Get the flags of the declaration. */
    public Flags flags() {
	return decl.flags();
    }

    /** Set the flags of the declaration. */
    public FieldDecl flags(Flags flags) {
	FieldDecl_c n = (FieldDecl_c) copy();
	n.decl = decl.flags(flags);
	return n;
    }

    /** Get the type node of the declaration. */
    public TypeNode type() {
	return decl.type();
    }

    /** Set the type of the declaration. */
    public FieldDecl type(TypeNode type) {
	FieldDecl_c n = (FieldDecl_c) copy();
	n.decl = decl.type(type);
	return n;
    }

    /** Get the name of the declaration. */
    public String name() {
	return decl.name();
    }

    /** Set the name of the declaration. */
    public FieldDecl name(String name) {
	FieldDecl_c n = (FieldDecl_c) copy();
	n.decl = decl.name(name);
	return n;
    }

    /** Get the initializer of the declaration. */
    public Expr init() {
	return decl.init();
    }

    /** Set the initializer of the declaration. */
    public FieldDecl init(Expr init) {
	FieldDecl_c n = (FieldDecl_c) copy();
	n.decl = decl.init(init);
	return n;
    }

    /** Set the field instance of the declaration. */
    public FieldDecl fieldInstance(FieldInstance fi) {
        FieldDecl_c n = (FieldDecl_c) copy();
	n.fi = fi;
	return n;
    }

    /** Get the field instance of the declaration. */
    public FieldInstance fieldInstance() {
	return fi;
    }

    /** Reconstruct the declaration. */
    protected FieldDecl_c reconstruct(TypeNode type, Expr init) {
	if (type() != type || init() != init) {
	    FieldDecl_c n = (FieldDecl_c) copy();
	    n.decl = (Declarator_c) decl.copy();
	    n.decl = n.decl.type(type);
	    n.decl = n.decl.init(init);
	    return n;
	}

	return this;
    }

    /** Visit the children of the declaration. */
    public Node visitChildren(NodeVisitor v) {
	TypeNode type = (TypeNode) type().visit(v);

	Expr init = null;

	if (init() != null) {
	    init = (Expr) init().visit(v);
	}

	return reconstruct(type, init);
    }

    /** Build type objects for the declaration. */
    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	ParsedClassType ct = tb.currentClass();
	TypeSystem ts = tb.typeSystem();

	FieldInstance fi = ts.fieldInstance(position(),
					    ct, flags(), declType(), name());

	Flags flags = flags();

	if (ct.flags().isInterface()) {
	    flags = flags.setPublic();
	    flags = flags.setStatic();
	    flags = flags.setFinal();
	}

	if (init() instanceof Lit && flags().isFinal()) {
	    Object value = ((Lit) init()).objValue();
	    fi = (FieldInstance) fi.constantValue(value);
	}

	ct.addField(fi);

        return flags(fi.flags()).fieldInstance(fi);
    }

    /** Type check the declaration. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

        try {
	    ts.checkFieldFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	decl.typeCheck(tc);

	return this;
    }

    public String toString() {
	return decl.toString();
    }

    public void translate_(CodeWriter w, Translator tr) {
	decl.translate(w, tr, true);
	w.write(";");
	w.newline(0);
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (fi != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + fi + ")");
	    w.end();
	}
    }

    /** Reconstruct the type objects for the declaration. */
    public Node reconstructTypes_(NodeFactory nf, TypeSystem ts, Context c)
	throws SemanticException {

	ParsedClassType ct = c.currentClass();

	Flags flags = flags();
        Type type = declType();
        String name = name();
	Expr init = init();

	FieldInstance fi = this.fi;

	if (ct != fi.container()) {
	    fi = fi.container(ct);
	}

	if (! flags.equals(fi.flags())) {
	    fi = fi.flags(flags);
	}
	//FIXME: 
	//if (! type.equals(fi.type())) {
	if (type != fi.type()) {
	    fi = fi.type(type);
	}

	if (! name.equals(fi.name())) {
	    fi = fi.name(name);
	}

	if (init instanceof Lit && flags.isFinal()) {
	    Object value = ((Lit) init()).objValue();

	    if (! fi.isConstant() || value != fi.constantValue()) {
		fi = fi.constantValue(value);
	    }
	}
	else if (fi.isConstant()) {
	    fi = fi.constantValue(null);
	}

	if (fi != this.fi) {
	    ct.replaceField(this.fi, fi);
	    return fieldInstance(fi);
	}
	
	return this;
    }
}
