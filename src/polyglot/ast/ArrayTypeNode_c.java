package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;
import java.io.IOException;

/**
 * A <code>TypeNode</code> represents the syntactic representation of a
 * <code>Type</code> within the abstract syntax tree.
 */
public class ArrayTypeNode_c extends TypeNode_c implements ArrayTypeNode
{
    protected TypeNode base;

    public ArrayTypeNode_c(Del ext, Position pos, TypeNode base) {
	super(ext, pos);
	this.base = base;
    }

    public TypeNode base() {
        return base;
    }

    public ArrayTypeNode base(TypeNode base) {
        ArrayTypeNode_c n = (ArrayTypeNode_c) copy();
	n.base = base;
	return n;
    }

    protected ArrayTypeNode_c reconstruct(TypeNode base) {
        if (base != this.base) {
	    ArrayTypeNode_c n = (ArrayTypeNode_c) copy();
	    n.base = base;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
        TypeNode base = (TypeNode) visitChild(this.base, v);
	return reconstruct(base);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();
        return type(ts.arrayOf(position(), base.type()));
    }

    public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
	TypeSystem ts = ar.typeSystem();
	NodeFactory nf = ar.nodeFactory();

        Type baseType = base.type();

        if (! baseType.isCanonical()) {
	    throw new SemanticException(
		"Base type " + baseType + " of array could not be resolved.",
		base.position());
	}

        return nf.CanonicalTypeNode(position(),
		                    ts.arrayOf(position(), baseType));
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot type check ambiguous node " + this + ".");
    }

    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot exception check ambiguous node " + this + ".");
    }

    public void translate(CodeWriter w, Translator tr) {
	throw new InternalCompilerError(position(),
	    "Cannot translate ambiguous node " + this + ".");
    }

    public String toString() {
        return base.toString() + "[]";
    }
}
