package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

public class AmbPrefix_c extends Node_c implements AmbPrefix
{
    protected Prefix prefix;
    protected String name;

    public AmbPrefix_c(Ext ext, Position pos, Prefix prefix, String name) {
	super(ext, pos);
	this.prefix = prefix;
	this.name = name;
    }

    public String name() {
	return this.name;
    }

    public AmbPrefix name(String name) {
	AmbPrefix_c n = (AmbPrefix_c) copy();
	n.name = name;
	return n;
    }

    public Prefix prefix() {
	return this.prefix;
    }

    public AmbPrefix prefix(Prefix prefix) {
	AmbPrefix_c n = (AmbPrefix_c) copy();
	n.prefix = prefix;
	return n;
    }

    protected AmbPrefix_c reconstruct(Prefix prefix) {
	if (prefix != this.prefix) {
	    AmbPrefix_c n = (AmbPrefix_c) copy();
	    n.prefix = prefix;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Prefix prefix = null;

	if (this.prefix != null) {
	    prefix = (Prefix) this.prefix.visit(v);
	}

	return reconstruct(prefix);
    }

    public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
	return new Disamb().disambiguate(ar, position(), prefix, name);
    }

    public Node typeCheck_(TypeChecker tc) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot type check ambiguous node " + this + ".");
    } 

    public Node exceptionCheck_(ExceptionChecker ec) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot exception check ambiguous node " + this + ".");
    } 

    public void translate_(CodeWriter w, Translator tr) {
	throw new InternalCompilerError(position(),
	    "Cannot translate ambiguous node " + this + ".");
    }

    public String toString() {
	return (prefix == null
		? name
		: prefix.toString() + "." + name) + "{amb}";
    }
}
