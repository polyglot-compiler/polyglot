package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import polyglot.frontend.*;
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

    public ConstructorDecl_c(Del ext, Position pos, Flags flags, String name, List formals, List exceptionTypes, Block body) {
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
    public CodeDecl body(Block body) {
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
	List formals = visitList(this.formals, v);
	List exceptionTypes = visitList(this.exceptionTypes, v);
	Block body = (Block) visitChild(this.body, v);
	return reconstruct(formals, exceptionTypes, body);
    }

    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        return tb.pushCode();
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
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

    public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
        if (ar.kind() == AmbiguityRemover.SUPER) {
            return ar.bypassChildren(this);
        }
        else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
            if (body != null) {
                return ar.bypass(body);
            }
        }

        return ar;
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
        if (ar.kind() == AmbiguityRemover.SIGNATURES) {
            Context c = ar.context();
            TypeSystem ts = ar.typeSystem();

            ParsedClassType ct = c.currentClassScope();

            ConstructorInstance ci = makeConstructorInstance(ct, ts);

            return constructorInstance(ci);
        }

        return this;
    }

    public NodeVisitor addMembersEnter(AddMemberVisitor am) {
	ParsedClassType ct = am.context().currentClassScope();
        ct.addConstructor(ci);
        return am.bypassChildren(this);
    }

    public Context enterScope(Context c) {
        return c.pushCode(ci);
    }

    /** Type check the constructor. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Context c = tc.context();
        TypeSystem ts = tc.typeSystem();

        ClassType ct = c.currentClass();

	if (ct.flags().isInterface()) {
	    throw new SemanticException(
		"Cannot declare a constructor inside an interface.",
		position());
	}

        if (ct.isAnonymous()) {
	    throw new SemanticException(
		"Cannot declare a constructor inside an anonymous class.",
		position());
        }

        String ctName = ((NamedType) ct).name();

        if (! ctName.equals(name)) {
	    throw new SemanticException("Constructor name \"" + name +
                "\" does not match name of containing class \"" +
                ctName + "\".", position());
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

        for (Iterator i = exceptionTypes().iterator(); i.hasNext(); ) {
            TypeNode tn = (TypeNode) i.next();
            Type t = tn.type();
            if (! t.isThrowable()) {
                throw new SemanticException("Type \"" + t +
                    "\" is not a subclass of \"" + ts.Throwable() + "\".",
                    tn.position());
            }
        }

        return this;
    }

    /** Check exceptions thrown by the constructor. */
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	TypeSystem ts = ec.typeSystem();

	SubtypeSet s = (SubtypeSet) ec.throwsSet();

	for (Iterator i = s.iterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();

	    boolean throwDeclared = false;

	    if (! t.isUncheckedException()) {
		for (Iterator j = exceptionTypes.iterator(); j.hasNext(); ) {
		    TypeNode tn = (TypeNode) j.next();
		    Type tj = tn.type();

		    if (ts.isSubtype(t, tj)) {
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
    public void prettyPrintHeader(CodeWriter w, PrettyPrinter tr) {
	w.begin(0);
	w.write(flags().translate());

	w.write(name);
	w.write("(");

	w.begin(0);

	for (Iterator i = formals.iterator(); i.hasNext(); ) {
	    Formal f = (Formal) i.next();
	    tr.print(f, w);

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
		tr.print(tn, w);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
	}

	w.end();
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        prettyPrintHeader(w, tr);

	if (body != null) {
	    printSubStmt(body, w, tr);
	}
	else {
	    w.write(";");
	}
    }

    public void translate(CodeWriter w, Translator tr) {
        prettyPrintHeader(w, tr);

	if (body != null) {
	    printSubStmt(body, w, tr.context(enterScope(tr.context())));
	}
	else {
	    w.write(";");
	}
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
