package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>AmbTypeNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type.
 */
public class AmbTypeNode_c extends TypeNode_c implements AmbTypeNode
{
    protected QualifierNode qual;
    protected String name;

    public AmbTypeNode_c(Ext ext, Position pos, QualifierNode qual, String name) {
	super(ext, pos);

	this.qual = qual;
	this.name = name;
    }

    public String name() {
	return this.name;
    }

    public AmbTypeNode name(String name) {
	AmbTypeNode_c n = (AmbTypeNode_c) copy();
	n.name = name;
	return n;
    }

    public QualifierNode qual() {
	return this.qual;
    }

    public AmbTypeNode qual(QualifierNode qual) {
	AmbTypeNode_c n = (AmbTypeNode_c) copy();
	n.qual = qual;
	return n;
    }

    protected AmbTypeNode_c reconstruct(QualifierNode qual) {
	if (qual != this.qual) {
	    AmbTypeNode_c n = (AmbTypeNode_c) copy();
	    n.qual = qual;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	QualifierNode qual = null;

	if (this.qual != null) {
	    qual = (QualifierNode) this.qual.visit(v);
	}

	return reconstruct(qual);
    }

    public Node buildTypes_(TypeBuilder tb) throws SemanticException {
	TypeSystem ts = tb.typeSystem();
	return type(ts.unknownType(position()));
    }

    public Node disambiguateTypes_(TypeAmbiguityRemover sc)
	throws SemanticException {

	Node n = sc.nodeFactory().disamb().disambiguate(sc, position(), qual, name);

	if (n instanceof TypeNode) {
	    return n;
	}
	throw new SemanticException("Could not find type \"" + name +
	    "\".", position());
    }

    public Node disambiguate_(AmbiguityRemover ar) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot disambiguate ambiguous node " + this + ".");
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
	return (qual == null
	        ? name
		: qual.toString() + "." + name) + "{amb}";
    }
}
