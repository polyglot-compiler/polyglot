package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import java.util.*;

/**
 * An <code>ArrayInit</code> is an immutable representation of
 * the an array initializer, such as { 3, 1, { 4, 1, 5 } }. Note that the 
 * elements of these array may be expressions of any type (e.g. 
 * <code>MethedExpr</code>).
 */
public class ArrayInit_c extends Expr_c implements ArrayInit
{
    protected List elements;

    public ArrayInit_c(Ext ext, Position pos, List elements) {
	super(ext, pos);
	this.elements = TypedList.copyAndCheck(elements, Expr.class, true);
    }

    public List elements() {
	return this.elements;
    }

    public ArrayInit elements(List elements) {
	ArrayInit_c n = (ArrayInit_c) copy();
	n.elements = TypedList.copyAndCheck(elements, Expr.class, true);
	return n;
    }

    protected ArrayInit_c reconstruct(List elements) {
	if (! CollectionUtil.equals(elements, this.elements)) {
	    ArrayInit_c n = (ArrayInit_c) copy();
	    n.elements = TypedList.copyAndCheck(elements, Expr.class, true);
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List elements = new ArrayList(this.elements.size());
	for (Iterator i = this.elements.iterator(); i.hasNext(); ) {
	    Expr n = (Expr) i.next();
	    n = (Expr) n.visit(v);
	    elements.add(n);
	}

	return reconstruct(elements);
    }

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
