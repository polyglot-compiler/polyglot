package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;
import java.util.*;

/**
 * A <code>ConstructorCall</code> is an immutable representation of
 * a direct call to a constructor of a class in the form of
 * <code>super(...)</code>  or <code>this(...)</code>.  It consists of the
 * kind of the call (either <code>super</code> or <code>this</code> and a list
 * of expressions to be parameters of the call.  A constructor call statement
 * may also contain an expression providing the context in which it is 
 * executed.
 */
public class ConstructorCall_c extends Stmt_c implements ConstructorCall
{
    protected Kind kind;
    protected Expr qualifier;
    protected List arguments;
    protected ConstructorInstance ci;

    public ConstructorCall_c(Ext ext, Position pos, Kind kind, Expr qualifier, List arguments) {
	super(ext, pos);
	this.kind = kind;
	this.qualifier = qualifier;
	this.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
    }

    public Expr qualifier() {
	return this.qualifier;
    }

    public ConstructorCall qualifier(Expr qualifier) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.qualifier = qualifier;
	return n;
    }

    public Kind kind() {
	return this.kind;
    }

    public ConstructorCall kind(Kind kind) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.kind = kind;
	return n;
    }

    public List arguments() {
	return Collections.unmodifiableList(this.arguments);
    }

    public ConstructorCall arguments(List arguments) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	return n;
    }

    public ConstructorInstance constructorInstance() {
        return ci;
    }

    public ConstructorCall constructorInstance(ConstructorInstance ci) {
	ConstructorCall_c n = (ConstructorCall_c) copy();
	n.ci = ci;
	return n;
    }

    protected ConstructorCall_c reconstruct(Expr qualifier, List arguments) {
	if (qualifier != this.qualifier || ! CollectionUtil.equals(arguments, this.arguments)) {
	    ConstructorCall_c n = (ConstructorCall_c) copy();
	    n.qualifier = qualifier;
	    n.arguments = TypedList.copyAndCheck(arguments, Expr.class, true);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr qualifier = null;

	if (this.qualifier != null) {
	    qualifier = (Expr) this.qualifier.visit(v);
	}

	List arguments = new ArrayList(this.arguments.size());
	for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
	    Expr n = (Expr) i.next();
	    n = (Expr) n.visit(v);
	    arguments.add(n);
	}

	return reconstruct(qualifier, arguments);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	Context c = tc.context();

	ClassType ct = c.currentClass();

	if (kind == SUPER) {
	    if (! ct.superType().isClass()) {
	        throw new SemanticException("Super type of " + ct +
		    " is not a class.", position());
	    }

	    ct = ct.superType().toClass();
	}

	List argTypes = new LinkedList();

	for (Iterator iter = this.arguments.iterator(); iter.hasNext();) {
	    Expr e = (Expr) iter.next();
	    argTypes.add(e.type());
	}

	ConstructorInstance ci = ts.findConstructor(ct, argTypes, c);

	return constructorInstance(ci);
    }

    public String toString() {
	return (qualifier != null ? qualifier + "." : "") + kind + "(...)";
    }

    public void translate_(CodeWriter w, Translator tr) {
	if (qualifier != null) {
	    qualifier.ext().translate(w, tr);
	    w.write(".");
	} 

	w.write(kind + "(");

	w.begin(0);

	for (Iterator i = arguments.iterator(); i.hasNext(); ) {
	    Expr e = (Expr) i.next();
	    e.ext().translate(w, tr);

	    if (i.hasNext()) {
		w.write(",");
		w.allowBreak(0);
	    }
	}

	w.end();

	w.write(");");
    }
}
