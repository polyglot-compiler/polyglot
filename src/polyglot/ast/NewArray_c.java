package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>NewArray</code> is an immutable representation of the
 * creation of a new array such as <code>new File[8][]</code>.  It consists of
 * an element type, in the above example <code>File</code>, a list of dimension
 * expressions (expressions which evaluate to a length for a
 * dimension in this case <code>8</code>), and a number representing the
 * number optional dimensions (in the example 1).
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

    public TypeNode baseType() {
	return this.baseType;
    }

    public NewArray baseType(TypeNode baseType) {
	NewArray_c n = (NewArray_c) copy();
	n.baseType = baseType;
	return n;
    }

    public List dims() {
	return Collections.unmodifiableList(this.dims);
    }

    public NewArray dims(List dims) {
	NewArray_c n = (NewArray_c) copy();
	n.dims = TypedList.copyAndCheck(dims, Expr.class, true);
	return n;
    }

    public int numDims() {
        return dims.size() + addDims;
    }

    public int additionalDims() {
	return this.addDims;
    }

    public NewArray additionalDims(int addDims) {
	NewArray_c n = (NewArray_c) copy();
	n.addDims = addDims;
	return n;
    }

    public ArrayInit init() {
	return this.init;
    }

    public NewArray init(ArrayInit init) {
	NewArray_c n = (NewArray_c) copy();
	n.init = init;
	return n;
    }

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

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	Type type = ts.arrayOf(baseType.type(), dims.size() + addDims);

	if (init != null) {
	    if (! ts.isAssignableSubtype(init.type(), type)) {
		throw new SemanticException("An array initializer must be " +
					    "the same type as the array " +
					    "declaration.", init.position());
	    }
	}

	return type(type);
    }

    public String toString() {
	return "new " + baseType + "[...]";
    }

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
