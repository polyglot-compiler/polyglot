package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import jltools.frontend.*;
import jltools.frontend.Compiler;
import java.util.*;

/**
 * A <code>ConstructorDecl</code> is an immutable representation of a
 * constructor declaration as part of a class body.
 */
public class ConstructorDecl_c extends Node_c implements ConstructorDecl
{
    protected Flags flags;
    protected String name;
    protected List formals;
    protected List exceptionTypes;
    protected Block body;
    protected ConstructorInstance ci;

    public ConstructorDecl_c(Ext ext, Position pos, Flags flags, String name, List formals, List exceptionTypes, Block body) {
	super(ext, pos);
	this.flags = flags;
	this.name = name;
	this.formals = TypedList.copyAndCheck(formals, Formal.class, true);
	this.exceptionTypes = TypedList.copyAndCheck(exceptionTypes, TypeNode.class, true);
	this.body = body;
    }

    /** Get the flags of the constructor. */
    public Flags flags() {
	return this.flags;
    }

    /** Set the flags of the constructor. */
    public ConstructorDecl flags(Flags flags) {
	ConstructorDecl_c n = (ConstructorDecl_c) copy();
	n.flags = flags;
	return n;
    }

    /** Get the name of the constructor. */
    public String name() {
	return this.name;
    }

    /** Set the name of the constructor. */
    public ConstructorDecl name(String name) {
	ConstructorDecl_c n = (ConstructorDecl_c) copy();
	n.name = name;
	return n;
    }

    /** Get the formals of the constructor. */
    public List formals() {
	return Collections.unmodifiableList(this.formals);
    }

    /** Set the formals of the constructor. */
    public ConstructorDecl formals(List formals) {
	ConstructorDecl_c n = (ConstructorDecl_c) copy();
	n.formals = TypedList.copyAndCheck(formals, Formal.class, true);
	return n;
    }

    /** Get the exceptionTypes of the constructor. */
    public List exceptionTypes() {
	return Collections.unmodifiableList(this.exceptionTypes);
    }

    /** Set the exceptionTypes of the constructor. */
    public ConstructorDecl exceptionTypes(List exceptionTypes) {
	ConstructorDecl_c n = (ConstructorDecl_c) copy();
	n.exceptionTypes = TypedList.copyAndCheck(exceptionTypes, TypeNode.class, true);
	return n;
    }

    /** Get the body of the constructor. */
    public Block body() {
	return this.body;
    }

    /** Set the body of the constructor. */
    public ConstructorDecl body(Block body) {
	ConstructorDecl_c n = (ConstructorDecl_c) copy();
	n.body = body;
	return n;
    }

    /** Get the constructorInstance of the constructor. */
    public ConstructorInstance constructorInstance() {
	return ci;
    }

    /** Get the procedureInstance of the constructor. */
    public ProcedureInstance procedureInstance() {
	return ci;
    }

    /** Set the constructorInstance of the constructor. */
    public ConstructorDecl constructorInstance(ConstructorInstance ci) {
	ConstructorDecl_c n = (ConstructorDecl_c) copy();
	n.ci = ci;
	return n;
    }

    /** Reconstruct the constructor. */
    protected ConstructorDecl_c reconstruct(List formals, List exceptionTypes, Block body) {
	if (! CollectionUtil.equals(formals, this.formals) || ! CollectionUtil.equals(exceptionTypes, this.exceptionTypes) || body != this.body) {
	    ConstructorDecl_c n = (ConstructorDecl_c) copy();
	    n.formals = TypedList.copyAndCheck(formals, Formal.class, true);
	    n.exceptionTypes = TypedList.copyAndCheck(exceptionTypes, TypeNode.class, true);
	    n.body = body;
	    return n;
	}

	return this;
    }

    /** Visit the children of the constructor. */
    public Node visitChildren(NodeVisitor v) {
        ConstructorDecl_c n = visitNonBodyChildren(v);

	Block body = null;

	if (n.body != null) {
	    body = (Block) n.body.visit(v);
	}

	return n.reconstruct(n.formals, n.exceptionTypes, body);
    }

    protected ConstructorDecl_c visitNonBodyChildren(NodeVisitor v) {
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

	return reconstruct(formals, exceptionTypes, this.body);
    }

    public Node buildTypesOverride_(TypeBuilder tb) throws SemanticException {
        tb.pushScope();
        return null;
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
        tb.popScope();

        TypeSystem ts = tb.typeSystem();

        List l = new ArrayList(formals.size());
        for (int i = 0; i < formals.size(); i++) {
          l.add(ts.unknownType(position()));
        }

        List m = new ArrayList(exceptionTypes.size());
        for (int i = 0; i < exceptionTypes.size(); i++) {
          l.add(ts.unknownType(position()));
        }

        ConstructorInstance ci = ts.constructorInstance(position(), ts.Object(),
                                                        Flags.NONE, l, m);
        return constructorInstance(ci);
    }

    public Node disambiguateOverride_(AmbiguityRemover ar) throws SemanticException {
        if (ar.kind() == AmbiguityRemover.SUPER) {
            return this;
        }

        if (ar.kind() == AmbiguityRemover.SIGNATURES) {
            ConstructorDecl_c n = visitNonBodyChildren(ar);

            Context c = ar.context();
            TypeSystem ts = ar.typeSystem();

            ParsedClassType ct = c.currentClass();

            ConstructorInstance ci = n.makeConstructorInstance(ct, ts);

            return n.constructorInstance(ci);
        }

        return null;
    }

    public Node addMembersOverride_(AddMemberVisitor tc) {
	ParsedClassType ct = tc.context().currentClass();
        ct.addConstructor(ci);
        return this;
    }

    public void enterScope(Context c) {
        c.pushCode(ci);
    }

    public void leaveScope(Context c) {
        c.popCode();
    }

    /** Type check the constructor. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        TypeSystem ts = tc.typeSystem();

        ClassType ct = c.currentClass();

	if (ct.flags().isInterface()) {
	    throw new SemanticException(
		"Cannot declare a constructor inside an interface.",
		position());
	}

	try {
	    ts.checkConstructorFlags(flags());
	}
	catch (SemanticException e) {
	    throw new SemanticException(e.getMessage(), position());
	}

	if (body == null && ! flags().isNative()) {
	    throw new SemanticException("Missing constructor body.",
		position());
	}

	if (body != null && flags().isNative()) {
	    throw new SemanticException(
		"A native constructor cannot have a body.", position());
	}

	return this;
    }

    /** Check exceptions thrown by the constructor. */
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
		    throw new SemanticException("Constructor \"" + name +
			"\" throws the undeclared exception \"" + t + "\".",
		        position());
		}
	    }
	}

	ec.throwsSet().clear();

	return this;
    }

    public String toString() {
	return flags.translate() + name + "(...)";
    }

    /** Write the constructor to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
        Context c = tr.context();

	w.begin(0);
	w.write(flags().translate());

	w.write(name);
	w.write("(");

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

	w.newline(0);
    }

    public void dump(CodeWriter w) {
	super.dump(w);

	if (ci != null) {
	    w.allowBreak(4, " ");
	    w.begin(0);
	    w.write("(instance " + ci + ")");
	    w.end();
	}
    }

    protected ConstructorInstance makeConstructorInstance(ClassType ct,
	TypeSystem ts) throws SemanticException {

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

	return ts.constructorInstance(position(), ct, flags,
		                      argTypes, excTypes);
    }
}
