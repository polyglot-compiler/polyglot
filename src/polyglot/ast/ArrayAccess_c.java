package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * An <code>ArrayAccess</code> is an immutable representation of an
 * access of an array member.  For instance <code>foo[i]</code> accesses the 
 * <code>i</code>'th member of <code>foo</code>.  An 
 * <code>ArrayAccess</code> consists of a base expression which 
 * evaulates to an array, and an index expression which evaluates to an integer
 * indicating the index of the array to be accessed.
 */
public class ArrayAccess_c extends Expr_c implements ArrayAccess
{
    protected Expr array;
    protected Expr index;

    public ArrayAccess_c(Ext ext, Position pos, Expr array, Expr index) {
	super(ext, pos);
	this.array = array;
	this.index = index;
    }

    public Precedence precedence() { 
	return Precedence.LITERAL;
    }

    public Expr array() {
	return this.array;
    }

    public ArrayAccess array(Expr array) {
	ArrayAccess_c n = (ArrayAccess_c) copy();
	n.array = array;
	return n;
    }

    public Expr index() {
	return this.index;
    }

    public ArrayAccess index(Expr index) {
	ArrayAccess_c n = (ArrayAccess_c) copy();
	n.index = index;
	return n;
    }

    protected ArrayAccess_c reconstruct(Expr array, Expr index) {
	if (array != this.array || index != this.index) {
	    ArrayAccess_c n = (ArrayAccess_c) copy();
	    n.array = array;
	    n.index = index;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Expr array = (Expr) this.array.visit(v);
	Expr index = (Expr) this.index.visit(v);
	return reconstruct(array, index);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
        TypeSystem ts = tc.typeSystem();

	if (! array.type().isArray()) {
	    throw new SemanticException(
		"Subscript can only follow an array type.", position());
	}

	if (! index.type().isImplicitCastValid(ts.Int())) {
	    throw new SemanticException(
		"Array subscript must be an integer.", position());
	} 

	return type(array.type().toArray().base());
    }

    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
	TypeSystem ts = ec.typeSystem();

	ec.throwsException(ts.NullPointerException());
	ec.throwsException(ts.OutOfBoundsException());

	return this;
    }

    public String toString() {
	return array + "[" + index + "]";
    }

    public void translate_(CodeWriter w, Translator tr) {
	translateSubexpr(array, w, tr);
	w.write ("[");
	translateBlock(index, w, tr);
	w.write ("]");
    }
}
