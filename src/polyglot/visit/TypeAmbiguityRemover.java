package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Pass;
import jltools.frontend.Job;
import java.util.*;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself. 
 */
public class TypeAmbiguityRemover extends SemanticVisitor
{
    public TypeAmbiguityRemover(Job job) {
	super(job);
    }

    protected Node overrideCall(Node n) throws SemanticException {
	return n.ext().disambiguateTypesOverride(this);
    }

    protected Node leaveCall(Node n) throws SemanticException {
	return n.ext().disambiguateTypes(this);
    }
}
