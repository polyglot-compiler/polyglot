package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * An <code>AmbQualifierNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type qualifier.
 */
public class AmbQualifierNode_c extends Node_c implements AmbQualifierNode
{
    protected Qualifier qualifier;
    protected QualifierNode qual;
    protected String name;

    public AmbQualifierNode_c(Ext ext, Position pos, QualifierNode qual, String name) {
	super(ext, pos);

	this.qual = qual;
	this.name = name;
    }

    public Qualifier qualifier() {
	return this.qualifier;
    }

    public String name() {
	return this.name;
    }

    public AmbQualifierNode name(String name) {
	AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
	n.name = name;
	return n;
    }

    public QualifierNode qual() {
	return this.qual;
    }

    public AmbQualifierNode qual(QualifierNode qual) {
	AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
	n.qual = qual;
	return n;
    }

    public AmbQualifierNode qualifier(Qualifier qualifier) {
	AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
	n.qualifier = qualifier;
	return n;
    }

    protected AmbQualifierNode_c reconstruct(QualifierNode qual) {
	if (qual != this.qual) {
	    AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
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
	return qualifier(ts.unknownQualifier(position()));
    }

    public Node disambiguateTypes_(TypeAmbiguityRemover sc)
	throws SemanticException {

	Node n = sc.nodeFactory().disamb().disambiguate(sc, position(), qual, name);

	if (n instanceof QualifierNode) {
	    return n;
	}

	throw new SemanticException("Could not find type or package \"" + name +
	    "\".", position());
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
