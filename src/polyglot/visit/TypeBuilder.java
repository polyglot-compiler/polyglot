package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;

import java.io.IOException;
import java.util.*;

public class TypeBuilder extends BaseVisitor
{
    protected Stack stack;
    protected Package currentPackage;

    public TypeBuilder(Job job) {
	super(job);
	stack = new Stack();
	currentPackage = null;
    }

    public Node override(Node n) {
        try {
	    return n.ext().buildTypesOverride(this);
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

	    return n;
	}
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	try {
	    return n.ext().buildTypes(this);
	}
	catch (SemanticException e) {
	    Position position = e.position();

	    if (position == null) {
		position = n.position();
	    }

	    errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
		                 e.getMessage(), position);

	    return n;
	}
    }

    Object inCode = new Object();

    public void pushScope() {
        stack.push(inCode);
    }

    public void popScope() {
        if (stack.isEmpty()) {
	    throw new InternalCompilerError("Empty class stack.");
	}

	if (! isLocal()) {
	    throw new InternalCompilerError("No method to pop.");
	}

        stack.pop();
    }

    public void pushClass(ParsedClassType type) throws SemanticException {
        stack.push(type);

	// Make sure the import table finds this class.
        if (type.isTopLevel()) {
	    importTable().addClassImport(type.toTopLevel().fullName());
	}
    }

    public void popClass() {
        if (stack.isEmpty()) {
	    throw new InternalCompilerError("Empty class stack.");
	}

	if (isLocal()) {
	    throw new InternalCompilerError("No class to pop.");
	}

        stack.pop();
    }

    private ParsedClassType newClass(Position pos, Flags flags, String name) {
	TypeSystem ts = typeSystem();

	if (isLocal()) {
	    ParsedLocalClassType ct = ts.localClassType(job);
	    ct.outer(currentClass());
	    ct.flags(flags);
	    ct.name(name);
	    ct.position(pos);

	    if (currentPackage != null) {
	      	ct.package_(currentPackage);
	    }

	    return ct;
	}
	else if (currentClass() != null) {
	    ParsedMemberClassType ct = ts.memberClassType(job);
	    ct.outer(currentClass());
	    ct.flags(flags);
	    ct.name(name);
	    ct.position(pos);

	    currentClass().addMemberClass(ct);

	    if (currentPackage != null) {
	      	ct.package_(currentPackage);
	    }

	    return ct;
	}
	else {
	    ParsedTopLevelClassType ct = ts.topLevelClassType(job);
	    ct.flags(flags);
	    ct.name(name);
	    ct.position(pos);

	    if (currentPackage != null) {
	      	ct.package_(currentPackage);
	    }

	    job.compiler().parsedResolver().addType(ct.fullName(), ct);

	    return ct;
	}
    }

    public ParsedClassType pushClass(Position pos, Flags flags, String name)
    	throws SemanticException {

        ParsedClassType t = newClass(pos, flags, name);
        pushClass(t);
	return t;
    }

    public boolean isLocal() {
        return ! stack.isEmpty() && stack.peek() == inCode;
    }

    /** Is this a top-level class or one of its members? */
    public boolean isGlobal() {
        return ! stack.contains(inCode);
    }

    public ParsedClassType currentClass() {
	ListIterator iter = stack.listIterator(stack.size());

	while (iter.hasPrevious()) {
	    Object o = iter.previous();

	    if (o != inCode) {
	        return (ParsedClassType) o;
	    }
	}

	return null;
    }

    public Package currentPackage() {
	return currentPackage;
    }

    public void setPackage(Package p) throws SemanticException {
	this.currentPackage = p;

	// The order of setPackage and addDefaultImports is important.

	if (p != null) {
	    importTable().setPackage(p.fullName());
	}

	importTable().addDefaultImports();
    }
}
