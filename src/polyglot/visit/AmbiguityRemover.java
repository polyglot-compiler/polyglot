package jltools.visit;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.frontend.Job;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself. 
 */
public class AmbiguityRemover extends SemanticVisitor
{
    public AmbiguityRemover(Job job) {
        super(job);
    }

    protected Node overrideCall(Node n) throws SemanticException {
        return n.ext().disambiguateOverride(this);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        return n.ext().disambiguate(this);
    }
}
