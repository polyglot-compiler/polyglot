package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * An <code>ArrayInit</code> is an immutable representation of
 * an array initializer, such as { 3, 1, { 4, 1, 5 } }.  Note that
 * the elements of these array may be expressions of any type (e.g.,
 * <code>Call</code>).
 */
public class ArrayInit_c extends Expr_c implements ArrayInit
{
    protected List elements;

    public ArrayInit_c(Ext ext, Position pos, List elements) {
	super(ext, pos);
	this.elements = TypedList.copyAndCheck(elements, Expr.class, true);
    }

    /** Get the elements of the initializer. */
    public List elements() {
	return this.elements;
    }

    /** Set the elements of the initializer. */
    public ArrayInit elements(List elements) {
	ArrayInit_c n = (ArrayInit_c) copy();
	n.elements = TypedList.copyAndCheck(elements, Expr.class, true);
	return n;
    }

    /** Reconstruct the initializer. */
    protected ArrayInit_c reconstruct(List elements) {
	if (! CollectionUtil.equals(elements, this.elements)) {
	    ArrayInit_c n = (ArrayInit_c) copy();
	    n.elements = TypedList.copyAndCheck(elements, Expr.class, true);
	    return n;
	}

	return this;
    }

    /** Visit the children of the initializer. */
    public Node visitChildren(NodeVisitor v) {
	List elements = new ArrayList(this.elements.size());
	for (Iterator i = this.elements.iterator(); i.hasNext(); ) {
	    Expr n = (Expr) i.next();
	    n = (Expr) n.visit(v);
	    elements.add(n);
	}

	return reconstruct(elements);
    }

    /** Type check the initializer. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	Type type = null;

	for (Iterator i = elements.iterator(); i.hasNext(); ) {
	    Expr e = (Expr) i.next();

	    if (type == null) {
	        type = e.type();
	    }
	    else {
	        type = ts.leastCommonAncestor(type, e.type());
	    }
	}

	if (type == null) {
	    return type(ts.Null());
	}
	else {
	    return type(ts.arrayOf(type));
	}
    }

    public String toString() {
	return "{ ... }";
    }

    /** Write the initializer to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write("{ ");

	for (Iterator i = elements.iterator(); i.hasNext(); ) {
	    Expr e = (Expr) i.next();

	    e.ext().translate(w, tr);

	    if (i.hasNext()) {
		w.write(", ");
	    }
	}

	w.write(" }");
    }
}
