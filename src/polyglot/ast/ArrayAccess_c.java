package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * An <code>ArrayAccess</code> is an immutable representation of an
 * access of an array member.
 */
public class ArrayAccess_c extends Expr_c implements ArrayAccess
{
    protected Expr array;
    protected Expr index;

    public ArrayAccess_c(Del ext, Position pos, Expr array, Expr index) {
	super(ext, pos);
	this.array = array;
	this.index = index;
    }

    /** Get the precedence of the expression. */
    public Precedence precedence() { 
	return Precedence.LITERAL;
    }

    /** Get the array of the expression. */
    public Expr array() {
	return this.array;
    }

    /** Set the array of the expression. */
    public ArrayAccess array(Expr array) {
	ArrayAccess_c n = (ArrayAccess_c) copy();
	n.array = array;
	return n;
    }

    /** Get the index of the expression. */
    public Expr index() {
	return this.index;
    }

    /** Set the index of the expression. */
    public ArrayAccess index(Expr index) {
	ArrayAccess_c n = (ArrayAccess_c) copy();
	n.index = index;
	return n;
    }

    /** Reconstruct the expression. */
    protected ArrayAccess_c reconstruct(Expr array, Expr index) {
	if (array != this.array || index != this.index) {
	    ArrayAccess_c n = (ArrayAccess_c) copy();
	    n.array = array;
	    n.index = index;
	    return n;
	}

	return this;
    }

    /** Visit the children of the expression. */
    public Node visitChildren(NodeVisitor v) {
	Expr array = (Expr) visitChild(this.array, v);
	Expr index = (Expr) visitChild(this.index, v);
	return reconstruct(array, index);
    }

    /** Type check the expression. */
    public Node typeCheck(TypeChecker tc) throws SemanticException {
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

    public Expr setExpectedType(Expr child, ExpectedTypeVisitor tc)
      	throws SemanticException
    {
        TypeSystem ts = tc.typeSystem();

        if (child == index) {
            return child.expectedType(ts.Int());
        }

        if (child == array) {
            return child.expectedType(ts.arrayOf(this.type));
        }

        return child;
    }

    /** Check exceptions thrown by the expression. */
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	TypeSystem ts = ec.typeSystem();

	ec.throwsException(ts.NullPointerException());
	ec.throwsException(ts.OutOfBoundsException());

	return this;
    }

    /*
    public String toString() {
	return array + "[" + index + "]";
    }
    */

    /** Write the expression to an output file. */
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	printSubExpr(array, w, tr);
	w.write ("[");
	printBlock(index, w, tr);
	w.write ("]");
    }
}
