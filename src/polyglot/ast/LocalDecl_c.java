package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/** 
 * A local variable declaration statement: a type, a name and an optional
 * initializer.
 */
public class LocalDecl_c extends Stmt_c implements LocalDecl
{
    Declarator decl;
    LocalInstance li;

    public LocalDecl_c(Ext ext, Position pos, Flags flags, TypeNode type, String name, Expr init) {
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
    public LocalDecl flags(Flags flags) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.flags(flags);
	return n;
    }

    /** Get the type node of the declaration. */
    public TypeNode type() {
        return decl.type();
    }

    /** Set the type node of the declaration. */
    public LocalDecl type(TypeNode type) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.type(type);
	return n;
    }

    /** Get the name of the declaration. */
    public String name() {
        return decl.name();
    }

    /** Set the name of the declaration. */
    public LocalDecl name(String name) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.name(name);
	return n;
    }

    /** Get the initializer of the declaration. */
    public Expr init() {
        return decl.init();
    }

    /** Set the initializer of the declaration. */
    public LocalDecl init(Expr init) {
        LocalDecl_c n = (LocalDecl_c) copy();
	n.decl = decl.init(init);
	return n;
    }

    /** Get the local instance of the declaration. */
    public LocalInstance localInstance() {
        return li;
    }

    /** Set the local instance of the declaration. */
    public LocalDecl localInstance(LocalInstance li) {
        LocalDecl_c n = (LocalDecl_c) copy();
		n.li = li;
		return n;
    }

	/** 
	 * Get the declarator.
	 */
	protected Declarator decl() {
		return decl;
	}
	
	/**
	 * Set the declarator.
	 */
	protected LocalDecl decl(Declarator decl) {
		LocalDecl_c n = (LocalDecl_c) copy();
		n.decl = decl;
		return n;
	}
	
    /** Reconstruct the declaration. */
    protected LocalDecl_c reconstruct(TypeNode type, Expr init) {
        if (type() != type || init() != init) {
	    LocalDecl_c n = (LocalDecl_c) copy();
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

    /** Add the variable to the scope after the declaration. */
    public void leaveScope(Context c) {
        c.addVariable(li);
    }

    /** Build type objects for the declaration. */
    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();

	LocalInstance li = ts.localInstance(position(),
	    				    flags(), declType(), name());

	if (init() instanceof Lit && flags().isFinal()) {
	    Object value = ((Lit) init()).objValue();
	    li = (LocalInstance) li.constantValue(value);
	}

	return localInstance(li);
    }

    /** Type check the declaration.
     * Override so we can do this test before we enter scope.
     * Return null to let the traversal continue.
     */
    public Node typeCheckOverride_(TypeChecker tc) throws SemanticException {
        Context c = tc.context();

	try {
	    c.findLocal(li.name());

	    throw new SemanticException(
		"Local variable " + li + " multiply-defined in " +
		c.currentCode() + ".");
	}
	catch (SemanticException e) {
	}

	return null;
    }

    /** Type check the declaration. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkLocalFlags(flags());
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

    /** Write the declaration to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        boolean semi = tr.appendSemicolon(true);

	decl.translate(w, tr, false);

	if (semi) {
	    w.write(";");
	}

	tr.appendSemicolon(semi);
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (li != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + li + ")");
	    w.end();
	}
    }

    /** Reconstruct the type objects for the declaration. */
    public Node reconstructTypes_(NodeFactory nf, TypeSystem ts, Context c)
        throws SemanticException {

	Flags flags = flags();
        Type type = declType();
        String name = name();
	Expr init = init();

	LocalInstance li = this.li;

	if (! flags.equals(li.flags())) li = li.flags(flags);
	if (! type.equals(li.type())) li = li.type(type);
	if (! name.equals(li.name())) li = li.name(name);

	if (init instanceof Lit && flags.isFinal()) {
	    Object value = ((Lit) init).objValue();

	    if (value != li.constantValue()) {
		li = (LocalInstance) li.constantValue(value);
	    }
	}
	else if (li.isConstant()) {
	    li = (LocalInstance) li.constantValue(null);
	}

	if (li != this.li) {
	    return localInstance(li);
	}

	return this;
    }
}
