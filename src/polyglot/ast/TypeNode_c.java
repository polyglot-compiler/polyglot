package jltools.ext.jl.ast;

import jltools.ast.*;

import jltools.frontend.Compiler;
import jltools.util.*;
import jltools.types.*;
import jltools.visit.*;

/**
 * A <code>TypeNode</code> represents the syntactic representation of a 
 * <code>Type</code> within the abstract syntax tree.
 */
public abstract class TypeNode_c extends Node_c implements TypeNode
{
    protected Type type;

    public TypeNode_c(Ext ext, Position pos) {
	super(ext, pos);
    }

    public Qualifier qualifier() {
        return type();
    }

    public Type type() {
	return this.type;
    }

    public TypeNode type(Type type) {
	TypeNode_c n = (TypeNode_c) copy();
	n.type = type;
	return n;
    }

    public String toString() {
	if (type != null) {
	    return type.toString();
	}
	else {
	    return "<unknown type>";
	}
    }

    public abstract void translate_(CodeWriter w, Translator tr);
}
