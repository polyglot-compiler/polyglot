package jltools.ext.jl.types;

import jltools.util.InternalCompilerError;
import jltools.frontend.Pass;
import jltools.types.*;
import java.util.*;

/**
 * A context represents a stack of scopes used for looking up types, methods,
 * and variables.
 */
public class Context_c implements Context
{
    /**
     * Contains the stack of inner scopes.
     */
    protected Stack /* of Scopes */ scopes;

    /**
     * The import table for the file
     */
    protected ImportTable it;

    /**
     * The type system.
     */
    protected TypeSystem ts;

    public Context_c(TypeSystem ts, ImportTable it) {
	this.ts = ts;
	this.it = it;
	scopes = new Stack();
	scopes.push(new OuterMark());
    }

    public TypeSystem typeSystem() { return ts; }
    public ImportTable importTable() { return it; }

    /**
     * Returns whether the particular symbol is defined locally.  If it isn't
     * in this scope, we ask the parent scope, but don't traverse to enclosing
     * classes.
     */
    public boolean isLocal(String name) {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    if (m instanceof Scope) {
		Scope scope = (Scope) m;

		if (scope.findVariable(name) != null ||
		    scope.findType(name) != null) {
		    return true;
		}
	    }
	    else {
		break;
	    }
	}

	return false;
    }

    /**
     * Looks up a method with name "name" and arguments compatible with
     * "argTypes".
     */
    public MethodInstance findMethod(String name, List argTypes)
	throws SemanticException {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    if (m instanceof Scope) {
		Scope scope = (Scope) m;

		ReferenceType rt = scope.findMethodContainer(name);

		if (rt != null) {
		    // Found a class which has a method of the right name.
		    // Now need to check if the method is of the correct type.
		    return ts.findMethod(rt, name, argTypes, this);
		}
	    }
	}

	throw new SemanticException("Method " + name + " not found.");
    }

    /**
     * Gets a local of a particular name.
     */
    public LocalInstance findLocal(String name) throws SemanticException {
	VarInstance vi = findVariableSilent(name);

	if (vi instanceof LocalInstance) {
	    return (LocalInstance) vi;
	}

	throw new SemanticException("Local variable " + name + " not found.");
    }

    /**
     * Finds the class which added a field to the scope.
     */
    public ParsedClassType findFieldScope(String name)
	throws SemanticException {

	ListIterator i = scopes.listIterator(scopes.size());
	VarInstance var = null;

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    Types.report(3, "find-scope " + name + ": trying " + m);

	    if (var == null) {
		if (m instanceof Scope) {
		    var = ((Scope) m).findVariable(name);
		}
	    }
	    else {
		Types.report(3, "find-scope " + name + " -> " + var);

		if (m instanceof ClassMark && var instanceof FieldInstance) {
		    return ((ClassMark) m).type();
		}
	    }
	}

	throw new SemanticException("Field " + name + " not found.");
    }

    /** Finds the class which added a method to the scope.
     */
    public ParsedClassType findMethodScope(String name) throws
	SemanticException {

	ListIterator i = scopes.listIterator(scopes.size());
	ReferenceType container = null;

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    Types.report(3, "find-scope " + name + ": trying " + m);

	    if (container == null) {
		if (m instanceof Scope) {
		    container = ((Scope) m).findMethodContainer(name);
		}
	    }
	    else {
		Types.report(3, "find-scope " + name + " -> " + container);

		if (m instanceof ClassMark) {
		    return ((ClassMark) m).type();
		}
	    }
	}

	throw new SemanticException("Field " + name + " not found.");
    }

    /**
     * Gets a field of a particular name.
     */
    public FieldInstance findField(String name) throws SemanticException {
	VarInstance vi = findVariableSilent(name);

	if (vi instanceof FieldInstance) {
	    FieldInstance fi = (FieldInstance) vi;
	    if (! ts.isAccessible(fi, this)) {
		throw new SemanticException("Field " + name +
		    " is not accessible from the current context.");
	    }
	    return fi;
	}

	throw new SemanticException("Field " + name + " not found.");
    }

    /**
     * Gets a local or field of a particular name.
     */
    public VarInstance findVariable(String name) throws SemanticException {
	VarInstance vi = findVariableSilent(name);

	if (vi != null) {
	    return vi;
	}

	throw new SemanticException("Field or local " + name + " not found.");
    }

    /**
     * Gets a local or field of a particular name, without raising exceptions.
     */
    private VarInstance findVariableSilent(String name) {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    Types.report(3, "find-var " + name + ": trying " + m);

	    if (m instanceof Scope) {
		Scope scope = (Scope) m;

		VarInstance var = scope.findVariable(name);

		if (var != null) {
		    Types.report(3, "find-var " + name + " -> " + var);
		    return var;
		}
	    }
	}

	return null;
    }

    public String toString() {
        String s = "";

	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();
            s += m.toString() + "; ";
        }

        return s;
    }

    /**
     * Finds the definition of a particular type qualifier (also a type).
     */
    public Qualifier findQualifier(String name) throws SemanticException {
        return findType(name);
    }

    /**
     * Finds the definition of a particular type.
     */
    public Type findType(String name) throws SemanticException {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    if (m instanceof Scope) {
	        Scope scope = (Scope) m;

		Type type = scope.findType(name);

		if (type != null) {
		    return type;
		}
	    }
	}

	return it.findType(name);
    }

    /** Return the mark at the top of the stack. */
    public Mark mark() {
	return (Mark) scopes.peek();
    }

    /** Assert that the mark at the top of the stack is <code>mark</code>. */
    public void assertMark(Mark mark) {
	if (mark() != mark) {
	    throw new InternalCompilerError("Unexpected scope.");
	}
    }

    /** Pop the stack until the top of the stack is <code>mark</code>. */
    public void popToMark(Mark mark) {
	while (mark() != mark) {
	    scopes.pop();
	}
    }

    /** Get a new scope mark. */
    protected Scope getScope() {
	return new JLScope();
    }

    /** Get a new class mark. */
    protected ClassMark getClassMark(ParsedClassType c) {
	return new JLClassMark(c);
    }

    /** Get a new code mark. */
    protected CodeMark getCodeMark(CodeInstance ci) {
	return new JLCodeMark(ci);
    }

    /**
     * Pushes on a class  scoping
     */
    public void pushClass(ParsedClassType c) {
        Types.report(4, "push class " + c + " " + c.position());
	scopes.push(getClassMark(c));
	pushBlock();
    }

    /**
     * Pops the most recently pushed class scoping
     */
    public void popClass() {
	popBlock();

        try {
	    // Force a type check.
	    ClassMark m = (ClassMark) scopes.pop();
            Types.report(4, "pop class " + m.type() + " " + m.type().position());
	}
	catch (EmptyStackException ese ) {
	    throw new InternalCompilerError("No more scopes to pop");
	}
    }

    /**
     * pushes an additional block-scoping level.
     */
    public void pushBlock() {
	Types.report(4, "push block");
	scopes.push(getScope());
    }

    /**
     * Removes a scoping level
     */
    public void popBlock() {
	try {
	    // Force a type check.
	    Scope s = (Scope) scopes.pop();
            Types.report(4, "pop block");
	}
	catch (EmptyStackException ese ) {
	    throw new InternalCompilerError("No more scopes to pop!");
	}
    }

    /**
     * enters a method
     */
    public void pushCode(CodeInstance ci) {
	Types.report(4, "push code " + ci + " " + ci.position());
	scopes.push(getCodeMark(ci));
	pushBlock();
    }

    /**
     * leaves a method
     */
    public void popCode() {
        popBlock();

	try {
	    // Force a type check.
	    CodeMark m = (CodeMark) scopes.pop();
            Types.report(4, "pop code " + m.code() + " " + m.code().position());
	}
	catch (EmptyStackException ese ) {
	    throw new InternalCompilerError("No more method scopes to pop!");
	}
    }

    /**
     * Gets the current method
     */
    public CodeInstance currentCode() {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    if (m instanceof CodeMark) {
		CodeMark s = (CodeMark) m;
		return s.code();
	    }
	}

	return null;
    }

    /**
     * Return true if in a method's scope and not in a local class within the
     * innermost method.
     */
    public boolean inCode() {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    if (m instanceof CodeMark) {
		return true;
	    }

	    if (m instanceof ClassMark) {
		return false;
	    }
	}

	return false;
    }

    /**
     * Gets current class
     */
    public ParsedClassType currentClass() {
	ListIterator i = scopes.listIterator(scopes.size());

	while (i.hasPrevious()) {
	    Mark m = (Mark) i.previous();

	    if (m instanceof ClassMark) {
		ClassMark s = (ClassMark) m;
		return s.type();
	    }
	}

	return null;
    }

    /**
     * Adds a symbol to the current scoping level.
     */
    public void addVariable(VarInstance vi) {
        Types.report(3, "Adding " + vi + " to context.");

	try {
	    Scope scope = (Scope) scopes.peek();
	    scope.addVariable(vi);
	}
	catch (EmptyStackException e) {
	    throw new InternalCompilerError("Scope stack is empty!");
	}
    }

    /**
     * Adds a method to the current scoping level.
     */
    public void addMethod(MethodInstance mi) {
        Types.report(3, "Adding " + mi + " to context.");

	try {
	    Scope scope = (Scope) scopes.peek();
	    scope.addMethodContainer(mi);
	}
	catch (EmptyStackException e) {
	    throw new InternalCompilerError("Scope stack is empty!");
	}
    }

    /**
     * Adds a type to the current scoping level.
     */
    public void addType(NamedType t) {
        Types.report(3, "Adding type " + t + " to context.");

	try {
	    Scope scope = (Scope) scopes.peek();
	    scope.addType(t);
	}
	catch (EmptyStackException e) {
	    throw new InternalCompilerError("Scope stack is empty!");
	}
    }

    /**
     * The outer mark.  An instance of this class is at the bottom of the
     * stack.
     */
    protected static class OuterMark implements Mark {
	public String toString() {
	    return "(outer)";
	}
    }

    /**
     * A class mark.
     */
    protected static interface ClassMark extends Mark {
	ParsedClassType type();
    }

    /**
     * A code (method, procedure, initializer) mark.
     */
    protected static interface CodeMark extends Mark {
	CodeInstance code();
    }

    /**
     * A scope mark.
     */
    protected static interface Scope extends Mark {
        Collection types();

	Type findType(String name);
	void addType(NamedType type);

	ReferenceType findMethodContainer(String name);
	void addMethodContainer(MethodInstance mi);

	VarInstance findVariable(String name);
	void addVariable(VarInstance var);
    }

    /**
     * A concrete implementation of a scope mark.
     */
    protected static class JLScope implements Scope {
	Map types;
	Map methods;
	Map variables;

	public JLScope() {
	    types = new HashMap();
	    methods = new HashMap();
	    variables = new HashMap();
	}

	public Collection types() {
	    return types.values();
	}

	public Type findType(String name) {
	    return (Type) types.get(name);
	}

	public void addType(NamedType type) {
	    types.put(type.name(), type);
	}

	public ReferenceType findMethodContainer(String name) {
	    return (ReferenceType) methods.get(name);
	}

	public void addMethodContainer(MethodInstance mi) {
	    methods.put(mi.name(), mi.container());
	}

	public VarInstance findVariable(String name) {
	    return (VarInstance) variables.get(name);
	}

	public void addVariable(VarInstance var) {
	    variables.put(var.name(), var);
	}

	public String toString() {
	    return "(scope (vars " + variables +
		        ") (methods " + methods +
		        ") (types " + types + "))";
	}
    }

    /**
     * A concrete implementation of a class mark.
     */
    protected static class JLClassMark implements ClassMark {
        ParsedClassType ct;
        JLClassMark(ParsedClassType ct) { this.ct = ct; }
	public ParsedClassType type() { return ct; }

	public String toString() {
	    return "(class " + ct + ")";
	}
    }

    /**
     * A concrete implementation of a code mark.
     */
    protected static class JLCodeMark implements CodeMark {
        CodeInstance ci;
        JLCodeMark(CodeInstance ci) { this.ci = ci; }
	public CodeInstance code() { return ci; }

	public String toString() {
	    return "(code " + ci + ")";
	}
    }
}
