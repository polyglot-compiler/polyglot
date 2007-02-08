/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2007 Polyglot project group, Cornell University
 * Copyright (c) 2006-2007 IBM Corporation
 * 
 */

package polyglot.ast;

import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * An <code>AmbQualifierNode</code> is an ambiguous AST node composed of
 * dot-separated list of identifiers that must resolve to a type qualifier.
 */
public class AmbQualifierNode_c extends Node_c implements AmbQualifierNode
{
    protected Qualifier qualifier;
    protected QualifierNode qual;
    protected Id name;

    public AmbQualifierNode_c(Position pos, QualifierNode qual, Id name) {
	super(pos);
	assert(name != null); // qual may be null

	this.qual = qual;
	this.name = name;
    }
    
    public Qualifier qualifier() {
	return this.qualifier;
    }
    
    public Id id() {
        return this.name;
    }
    
    public AmbQualifierNode id(Id name) {
        AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
        n.name = name;
        return n;
    }

    public String name() {
	return this.name.id();
    }

    public AmbQualifierNode name(String name) {
        return id(this.name.id(name));
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

    protected AmbQualifierNode_c reconstruct(QualifierNode qual, Id name) {
	if (qual != this.qual || name != this.name) {
	    AmbQualifierNode_c n = (AmbQualifierNode_c) copy();
	    n.qual = qual;
	    return n;
	}

	return this;
    }

    public Node visitChildren(NodeVisitor v) {
        Id name = (Id) visitChild(this.name, v);
	QualifierNode qual = (QualifierNode) visitChild(this.qual, v);
	return reconstruct(qual, name);
    }

    public Node buildTypes(TypeBuilder tb) throws SemanticException {
        return qualifier(tb.typeSystem().unknownQualifier(position()));
    }

    public Node disambiguate(AmbiguityRemover sc) throws SemanticException {
        if (qual != null && ! qual.isDisambiguated()) {
            return this;
        }
        
	Node n = sc.nodeFactory().disamb().disambiguate(this, sc, position(), qual, name);

	if (n instanceof QualifierNode) {
	    return n;
	}

	throw new SemanticException("Could not find type or package \"" +
            (qual == null ? name.toString() : qual.toString() + "." + name.toString()) +
	    "\".", position());
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException {
        // Didn't finish disambiguation; just return.
        return this;
    }
    
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
	throw new InternalCompilerError(position(),
	    "Cannot exception check ambiguous node " + this + ".");
    } 

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (qual != null) {
            print(qual, w, tr);
            w.write(".");
	    w.allowBreak(2, 3, "", 0);
        }
             
        tr.print(this, name, w);
    }

    public String toString() {
	return (qual == null
		? name.toString()
		: qual.toString() + "." + name.toString()) + "{amb}";
    }
}
