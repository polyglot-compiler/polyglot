package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
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

    public ArrayInit_c(Del ext, Position pos, List elements) {
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
	List elements = visitList(this.elements, v);
	return reconstruct(elements);
    }

    /** Type check the initializer. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
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

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        if (elements.isEmpty()) {
            return child;
        }

        Type t = this.expectedType();

        if (! t.isArray()) {
            throw new SemanticException("Type of array initializer must be " +
                                        "an array.", position());
        }

        t = t.toArray().base();

	for (Iterator i = elements.iterator(); i.hasNext(); ) {
	    Expr e = (Expr) i.next();

            if (e == child) {
                return child.expectedType(t);
            }
        }

        return child;
    }

    public void typeCheckElements(Type lhsType) throws SemanticException {
        TypeSystem ts = lhsType.typeSystem();

        if (! lhsType.isArray()) {
          throw new SemanticException("Cannot initialize " + lhsType +
                                      " with " + type, position());
        }

        // Check if we can assign each individual element.
        Type t = lhsType.toArray().base();

        for (Iterator i = elements.iterator(); i.hasNext(); ) {
            Expr e = (Expr) i.next();
            Type s = e.type();

            boolean intConversion = false;

            if (e instanceof NumLit) {
                long value = ((NumLit) e).longValue();
                intConversion = ts.numericConversionValid(t, value);
            }

            if (! s.isAssignableSubtype(t) &&
                ! s.isSame(t) &&
                ! intConversion) {
                throw new SemanticException("Cannot assign " + s +
                                            " to " + t + ".", e.position());
            }
        }
    }

    public String toString() {
	return "{ ... }";
    }

    /** Write the initializer to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("{ ");

	for (Iterator i = elements.iterator(); i.hasNext(); ) {
	    Expr e = (Expr) i.next();
	    tr.print(e, w);

	    if (i.hasNext()) {
		w.write(", ");
	    }
	}

	w.write(" }");
    }
}
