package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * A method declaration.
 */
public class MethodDecl_c extends Node_c implements MethodDecl
{
    protected Flags flags;
    protected TypeNode returnType;
    protected String name;
    protected List formals;
    protected List exceptionTypes;
    protected Block body;
    protected MethodInstance mi;

    public MethodDecl_c(Ext ext, Position pos, Flags flags, TypeNode returnType, String name, List formals, List exceptionTypes, Block body) {
	super(ext, pos);
	this.flags = flags;
	this.returnType = returnType;
	this.name = name;
	this.formals = TypedList.copyAndCheck(formals, Formal.class, true);
	this.exceptionTypes = TypedList.copyAndCheck(exceptionTypes, TypeNode.class, true);
	this.body = body;
    }

    /** Get the flags of the method. */
    public Flags flags() {
	return this.flags;
    }

    /** Set the flags of the method. */
    public MethodDecl flags(Flags flags) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.flags = flags;
	return n;
    }

    /** Get the return type of the method. */
    public TypeNode returnType() {
	return this.returnType;
    }

    /** Set the return type of the method. */
    public MethodDecl returnType(TypeNode returnType) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.returnType = returnType;
	return n;
    }

    /** Get the name of the method. */
    public String name() {
	return this.name;
    }

    /** Set the name of the method. */
    public MethodDecl name(String name) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.name = name;
	return n;
    }

    /** Get the formals of the method. */
    public List formals() {
	return Collections.unmodifiableList(this.formals);
    }

    /** Set the formals of the method. */
    public MethodDecl formals(List formals) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.formals = TypedList.copyAndCheck(formals, Formal.class, true);
	return n;
    }

    /** Get the exception types of the method. */
    public List exceptionTypes() {
	return Collections.unmodifiableList(this.exceptionTypes);
    }

    /** Set the exception types of the method. */
    public MethodDecl exceptionTypes(List exceptionTypes) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.exceptionTypes = TypedList.copyAndCheck(exceptionTypes, TypeNode.class, true);
	return n;
    }

    /** Get the body of the method. */
    public Block body() {
	return this.body;
    }

    /** Set the body of the method. */
    public MethodDecl body(Block body) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.body = body;
	return n;
    }

    /** Get the method instance of the method. */
    public MethodInstance methodInstance() {
	return mi;
    }

    /** Set the method instance of the method. */
    public MethodDecl methodInstance(MethodInstance mi) {
	MethodDecl_c n = (MethodDecl_c) copy();
	n.mi = mi;
	return n;
    }

    /** Get the procedure instance of the method. */
    public ProcedureInstance procedureInstance() {
	return mi;
    }

    /** Reconstruct the method. */
    protected MethodDecl_c reconstruct(TypeNode returnType, List formals, List exceptionTypes, Block body) {
	if (returnType != this.returnType || ! CollectionUtil.equals(formals, this.formals) || ! CollectionUtil.equals(exceptionTypes, this.exceptionTypes) || body != this.body) {
	    MethodDecl_c n = (MethodDecl_c) copy();
	    n.returnType = returnType;
	    n.formals = TypedList.copyAndCheck(formals, Formal.class, true);
	    n.exceptionTypes = TypedList.copyAndCheck(exceptionTypes, TypeNode.class, true);
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the method. */
    public Node visitChildren(NodeVisitor v) {
	TypeNode returnType = (TypeNode) this.returnType.visit(v);

	List formals = new ArrayList(this.formals.size());
	for (Iterator i = this.formals.iterator(); i.hasNext(); ) {
	    Formal n = (Formal) i.next();
	    n = (Formal) n.visit(v);
	    formals.add(n);
	}

	List exceptionTypes = new ArrayList(this.exceptionTypes.size());
	for (Iterator i = this.exceptionTypes.iterator(); i.hasNext(); ) {
	    TypeNode n = (TypeNode) i.next();
	    n = (TypeNode) n.visit(v);
	    exceptionTypes.add(n);
	}

	Block body = null;

	if (this.body != null) {
	    body = (Block) this.body.visit(v);
	}

	return reconstruct(returnType, formals, exceptionTypes, body);
    }

    /** Build type objects for the method. */
    public Node buildTypesOverride_(TypeBuilder tb) throws SemanticException {
	tb.pushScope();

	MethodDecl_c n = (MethodDecl_c) visitChildren(tb);

	tb.popScope();

	ParsedClassType ct = tb.currentClass();

	MethodInstance mi = n.makeMethodInstance(ct, tb.typeSystem());
	ct.addMethod(mi);

	return n.flags(mi.flags()).methodInstance(mi);
    }

    public void enterScope(Context c) {
        c.pushCode(mi);
    }

    public void leaveScope(Context c) {
        c.popCode();
    }

    /** Type check the method. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();

	try {
	    ts.checkMethodFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	if (body == null && ! (flags().isAbstract() || flags().isNative())) {
	    throw new SemanticException("Missing method body.", position());
	}

	if (body != null && flags().isAbstract()) {
	    throw new SemanticException(
		"An abstract method cannot have a body.", position());
	}

	if (body != null && flags().isNative()) {
	    throw new SemanticException(
		"A native method cannot have a body.", position());
	}

	return this;
    }

    /** Check exceptions thrown by the method. */
    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
	TypeSystem ts = ec.typeSystem();

	SubtypeSet s = (SubtypeSet) ec.throwsSet();

	for (Iterator i = s.iterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();

	    boolean throwDeclared = false;
	    
	    if (! t.isUncheckedException()) {
		for (Iterator j = exceptionTypes.iterator(); j.hasNext(); ) {
		    TypeNode tn = (TypeNode) j.next();
		    Type tj = tn.type();

		    if (ts.isSame(t, tj) || ts.descendsFrom(t, tj)) {
			throwDeclared = true; 
			break;
		    }
		}

		if (! throwDeclared) {
			ec.throwsSet().clear();
			throw new SemanticException("Method \"" + name +
			"\" throws the undeclared exception \"" + t + "\".",
		        position());
		}
	    }
	}

	ec.throwsSet().clear();

	return this;
    }

    public String toString() {
	return flags.translate() + returnType + " " + name + "(...)";
    }

    /** Write the method to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        Context c = tr.context();

	Flags flags = flags();

	if (c.currentClass().flags().isInterface()) {
	    flags = flags.clearPublic();
	    flags = flags.clearAbstract();
	}

	w.begin(0);
	w.write(flags.translate());
	returnType.ext().translate(w, tr);
	w.write(" " + name + "(");

	w.begin(0);

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    f.ext().translate(w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0, " ");
	    }
	}

	w.end();
	w.write(")");

	if (! exceptionTypes.isEmpty()) {
	    w.allowBreak(6);
	    w.write("throws ");

	    for (Iterator i = exceptionTypes.iterator(); i.hasNext(); ) {
	        TypeNode tn = (TypeNode) i.next();
		tn.ext().translate(w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
	}

	w.end();


	if (body != null) {
	    enterScope(c);
	    translateSubstmt(body, w, tr);
	    leaveScope(c);
	}
	else {
	    w.write(";");
	}
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (mi != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + mi + ")");
	    w.end();
	}

        w.allowBreak(4, " ");
        w.begin(0);
        w.write("(name " + name + ")");
        w.end();
    }

    /** Reconstruct the type objects for the method. */
    public Node reconstructTypes_(NodeFactory nf, TypeSystem ts, Context c)
        throws SemanticException {

	ParsedClassType ct = c.currentClass();

	MethodInstance mi = this.mi; 

	if (ct != mi.container()) {
	    mi = mi.container(ct);
	}

	if (! flags.equals(mi.flags())) {
	    mi = mi.flags(flags);
	}

	if (! returnType.type().equals(mi.returnType())) {
	    mi = mi.returnType(returnType.type());
	}

	if (! name.equals(mi.name())) {
	    mi = mi.name(name);
	}

	List l;

	l = new LinkedList();

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    l.add(f.declType());
	}

	if (! l.equals(mi.argumentTypes())) {
	    mi = mi.argumentTypes(l);
	}

	l = new LinkedList();

	for (Iterator i = exceptionTypes.iterator(); i.hasNext(); ) {
	    TypeNode tn = (TypeNode) i.next();
	    l.add(tn.type());
	}

	if (! l.equals(mi.exceptionTypes())) {
	    mi = mi.exceptionTypes(l);
	}

	if (mi != this.mi) {
	    ct.replaceMethod(this.mi, mi);
	    return methodInstance(mi);
	}

	return this;
    }

    protected MethodInstance makeMethodInstance(ClassType ct, TypeSystem ts)
	throws SemanticException {

	List argTypes = new LinkedList();
	List excTypes = new LinkedList(); 

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    argTypes.add(f.declType());
	}

	for (Iterator i = exceptionTypes.iterator(); i.hasNext(); ) {
	    TypeNode tn = (TypeNode) i.next();
	    excTypes.add(tn.type());
	}

	Flags flags = this.flags;

	if (ct.flags().isInterface()) {
	    flags = flags.setPublic();
	    flags = flags.setAbstract();
	}

	return ts.methodInstance(position(),
	    			 ct, flags, returnType.type(), name,
	                         argTypes, excTypes);
    }
}
