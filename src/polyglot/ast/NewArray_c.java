package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>NewArray</code> represents a new array expression such as <code>new
 * File[8][] { null }</code>.  It consists of an element type (e.g.,
 * <code>File</code>), a list of dimension expressions (e.g., 8), 0 or more
 * additional dimensions (e.g., 1 for []), and an array initializer.  The
 * dimensions of the array initializer must equal the number of additional "[]"
 * dimensions.
 */
public class NewArray_c extends Expr_c implements NewArray
{
    protected TypeNode baseType;
    protected List dims;
    protected int addDims;
    protected ArrayInit init;

    public NewArray_c(Ext ext, Position pos, TypeNode baseType, List dims, int addDims, ArrayInit init) {
	super(ext, pos);
	this.baseType = baseType;
	this.dims = TypedList.copyAndCheck(dims, Expr.class, true);
	this.addDims = addDims;
	this.init = init;
    }

    /** Get the base type node of the expression. */
    public TypeNode baseType() {
	return this.baseType;
    }

    /** Set the base type node of the expression. */
    public NewArray baseType(TypeNode baseType) {
	NewArray_c n = (NewArray_c) copy();
	n.baseType = baseType;
	return n;
    }

    /** Get the dimension expressions of the expression. */
    public List dims() {
	return Collections.unmodifiableList(this.dims);
    }

    /** Set the dimension expressions of the expression. */
    public NewArray dims(List dims) {
	NewArray_c n = (NewArray_c) copy();
	n.dims = TypedList.copyAndCheck(dims, Expr.class, true);
	return n;
    }

    /** Get the number of dimensions of the expression. */
    public int numDims() {
        return dims.size() + addDims;
    }

    /** Get the number of additional dimensions of the expression. */
    public int additionalDims() {
	return this.addDims;
    }

    /** Set the number of additional dimensions of the expression. */
    public NewArray additionalDims(int addDims) {
	NewArray_c n = (NewArray_c) copy();
	n.addDims = addDims;
	return n;
    }

    /** Get the initializer of the expression. */
    public ArrayInit init() {
	return this.init;
    }

    /** Set the initializer of the expression. */
    public NewArray init(ArrayInit init) {
	NewArray_c n = (NewArray_c) copy();
	n.init = init;
	return n;
    }

    /** Reconstruct the expression. */
    protected NewArray_c reconstruct(TypeNode baseType, List dims, ArrayInit init) {
	if (baseType != this.baseType || ! CollectionUtil.equals(dims, this.dims) || init != this.init) {
	    NewArray_c n = (NewArray_c) copy();
	    n.baseType = baseType;
	    n.dims = TypedList.copyAndCheck(dims, Expr.class, true);
	    n.init = init;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	TypeNode baseType = (TypeNode) this.baseType.visit(v);

	List dims = new ArrayList(this.dims.size());
	for (Iterator i = this.dims.iterator(); i.hasNext(); ) {
	    Expr n = (Expr) i.next();
	    n = (Expr) n.visit(v);
	    dims.add(n);
	}

	ArrayInit init = null;

	if (this.init != null) {
	    init = (ArrayInit) this.init.visit(v);
	}

	return reconstruct(baseType, dims, init);
    }

    /** Type check the expression. */
    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	ArrayType type = ts.arrayOf(baseType.type(), dims.size() + addDims);

	if (init != null) {
            init.typeCheckElements(type);
	}

	return type(type);
    }

    public String toString() {
	return "new " + baseType + "[...]";
    }

    /** Write the expression to an output file. */
    public void translate_(CodeWriter w, Translator tr) {
	w.write("new ");
	baseType.ext().translate(w, tr);

	for (Iterator i = dims.iterator(); i.hasNext();) {
	  Expr e = (Expr) i.next();
	  w.write("[");
	  translateBlock(e, w, tr);
	  w.write("]");
	}

	for (int i = 0; i < addDims; i++) {
	    w.write("[]");
	}

	if (init != null) {
	    w.write(" ");
	    init.ext().translate(w, tr);
	}
    }
}
