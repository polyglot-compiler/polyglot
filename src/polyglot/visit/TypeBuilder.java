package jltools.visit;

import jltools.ast.*;
import jltools.frontend.*;
import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;
import jltools.visit.NodeVisitor;

import java.io.IOException;
import java.util.*;

/** Visitor which traverses the AST constructing type objects. */
public class TypeBuilder extends NodeVisitor
{
    protected Stack stack;
    protected ImportTable importTable;
    protected Job job;
    protected TypeSystem ts;
    protected NodeFactory nf;

    public TypeBuilder(Job job, TypeSystem ts, NodeFactory nf) {
        this.job = job;
        this.ts = ts;
        this.nf = nf;
	stack = new Stack();
    }

    public Job job() {
        return job;
    }

    public ErrorQueue errorQueue() {
        return job.compiler().errorQueue();
    }

    public NodeFactory nodeFactory() {
        return nf;
    }

    public TypeSystem typeSystem() {
        return ts;
    }

    public boolean begin() {
        // Initialize the stack from the context.
        Context context = job.context();

        Stack s = new Stack();

        for (ParsedClassType ct = context.currentClass(); ct != null; ) {
            s.push(ct);

            if (ct.isInner()) {
                ct = (ParsedClassType) ct.toInner().outer();
            }
            else {
                ct = null;
            }
        }

        if (context.importTable() != null) {
            setImportTable(context.importTable());
        }

        while (! s.isEmpty()) {
            ParsedClassType ct = (ParsedClassType) s.pop();

            try {
                pushClass(ct);
            }
            catch (SemanticException e) {
                errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                     e.getMessage(), ct.position());
                return false;
            }

            if (ct.isLocal() || ct.isAnonymous()) {
                pushScope();
            }
        }

        return true;
    }

    public Node enter(Node n) {
        try {
	    return n.del().buildTypesEnter(this);
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

    public Node override(Node n) {
        try {
	    return n.del().buildTypesOverride(this);
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
	    return n.del().buildTypes(this);
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
        Types.report(4, "TB pushing code");
        stack.push(inCode);
    }

    public void popScope() {
        if (stack.isEmpty()) {
	    throw new InternalCompilerError("Empty class stack.");
	}

	if (! isLocal()) {
	    throw new InternalCompilerError("No method to pop.");
	}

        Types.report(4, "TB popping code");

        stack.pop();
    }

    public void pushClass(ParsedClassType type) throws SemanticException {
        Types.report(4, "TB pushing class " + type);

        stack.push(type);

	// Make sure the import table finds this class.
        if (importTable() != null && type.isTopLevel()) {
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

        Types.report(4, "TB popping " + stack.peek());

        stack.pop();
    }

    private ParsedClassType newClass(Position pos, Flags flags, String name) {
	TypeSystem ts = typeSystem();

	if (isLocal()) {
	    ParsedLocalClassType ct = ts.localClassType();
	    ct.outer(currentClass());
	    ct.flags(flags);
	    ct.name(name);
	    ct.position(pos);

	    if (currentPackage() != null) {
	      	ct.package_(currentPackage());
	    }

	    return ct;
	}
	else if (currentClass() != null) {
	    ParsedMemberClassType ct = ts.memberClassType();
	    ct.outer(currentClass());
	    ct.flags(flags);
	    ct.name(name);
	    ct.position(pos);

	    currentClass().addMemberClass(ct);

	    if (currentPackage() != null) {
	      	ct.package_(currentPackage());
	    }

	    return ct;
	}
	else {
	    ParsedTopLevelClassType ct = ts.topLevelClassType();
	    ct.flags(flags);
	    ct.name(name);
	    ct.position(pos);

	    if (currentPackage() != null) {
	      	ct.package_(currentPackage());
	    }

	    typeSystem().parsedResolver().addType(ct.fullName(), ct);

	    return ct;
	}
    }

    public ParsedAnonClassType pushAnonClass(Position pos)
        throws SemanticException {

        if (! isLocal()) {
            throw new InternalCompilerError(
                "Cannot push anonymous class outside method scope.");
        }

	TypeSystem ts = typeSystem();

        ParsedAnonClassType ct = ts.anonClassType();
        ct.outer(currentClass());
        ct.position(pos);

        if (currentPackage() != null) {
            ct.package_(currentPackage());
        }

        pushClass(ct);

        return ct;
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
	return importTable.package_();
    }

    public ImportTable importTable() {
        return importTable;
    }

    public void setImportTable(ImportTable it) {
        this.importTable = it;
    }
}
