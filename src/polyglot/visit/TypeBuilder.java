package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.types.Package;
import polyglot.visit.NodeVisitor;

import java.io.IOException;
import java.util.*;

/** Visitor which traverses the AST constructing type objects. */
public class TypeBuilder extends HaltingVisitor
{
    protected ImportTable importTable;
    protected Job job;
    protected TypeSystem ts;
    protected NodeFactory nf;
    protected TypeBuilder outer;

    public TypeBuilder(Job job, TypeSystem ts, NodeFactory nf) {
        this.job = job;
        this.ts = ts;
        this.nf = nf;
        this.outer = null;
    }

    public TypeBuilder push() {
        TypeBuilder tb = (TypeBuilder) this.copy();
        tb.outer = this;
        return tb;
    }

    public TypeBuilder pop() {
        return outer;
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

    public NodeVisitor begin() {
        // Initialize the stack from the context.
        Context context = job.context();

        if (context == null) {
            return this;
        }

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

        TypeBuilder tb = this;

        while (! s.isEmpty()) {
            ParsedClassType ct = (ParsedClassType) s.pop();

            try {
                tb = tb.pushClass(ct);
            }
            catch (SemanticException e) {
                errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                     e.getMessage(), ct.position());
                return null;
            }

            if (ct.isLocal() || ct.isAnonymous()) {
                tb = tb.pushCode();
            }
        }

        return tb;
    }

    public NodeVisitor enter(Node n) {
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

            return this;
	}
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	try {
	    return n.del().buildTypes((TypeBuilder) v);
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

    boolean local; // true if the last scope pushed as not a class.
    boolean global; // true if all scopes pushed have been classes.
    ParsedClassType type; // last class pushed.

    public TypeBuilder pushCode() {
        Types.report(4, "TB pushing code");
        TypeBuilder tb = push();
        tb.local = true;
        tb.global = false;
        return tb;
    }

    public TypeBuilder pushClass(ParsedClassType type) throws SemanticException {
        Types.report(4, "TB pushing class " + type);

        TypeBuilder tb = push();
        tb.type = type;
        tb.local = false;

	// Make sure the import table finds this class.
        if (importTable() != null && type.isTopLevel()) {
	    tb.importTable().addClassImport(type.toTopLevel().fullName());
	}
        
        return tb;
    }

    private ParsedClassType newClass(Position pos, Flags flags, String name) {
	TypeSystem ts = typeSystem();

	if (local) {
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

    public TypeBuilder pushAnonClass(Position pos)
        throws SemanticException {

        if (! local) {
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

        return pushClass(ct);
    }

    public TypeBuilder pushClass(Position pos, Flags flags, String name)
    	throws SemanticException {

        ParsedClassType t = newClass(pos, flags, name);
        return pushClass(t);
    }

    public ParsedClassType currentClass() {
        return this.type;
    }

    public Package currentPackage() {
        if (importTable() == null) return null;
	return importTable.package_();
    }

    public ImportTable importTable() {
        return importTable;
    }

    public void setImportTable(ImportTable it) {
        this.importTable = it;
    }

    public String toString() {
        return "(TB " + type +
                (local ? " local" : "") +
                (global ? " global" : "") +
                (outer == null ? ")" : " " + outer.toString());
    }
}
