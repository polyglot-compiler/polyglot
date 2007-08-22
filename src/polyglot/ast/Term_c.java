/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

/**
 * A <code>Term</code> represents any Java expression or statement on which
 * dataflow can be performed.
 */
public abstract class Term_c extends Node_c implements Term
{
    public Term_c(Position pos) {
	super(pos);
    }
    
    protected boolean reachable;

    /**
     * Visit this term in evaluation order.
     */
    public abstract List acceptCFG(CFGBuilder v, List succs);

    /**
     * Return true if this term is eachable.  This attribute is not
     * guaranteed correct until after the reachability pass
     *
     * @see polyglot.visit.ReachChecker
     */
    public boolean reachable() {
        return reachable;
    }

    /**
     * Set the reachability of this term.
     */
    public Term reachable(boolean reachability) {
        if (this.reachable == reachability) {
            return this;
        }
        
        Term_c t = (Term_c) copy();
        t.reachable = reachability;
        return t;
    }

    /** Utility function to get the first entry of a list, or else alt. */
    public static Term listChild(List l, Term alt) {
        Term c = (Term) CollectionUtil.firstOrElse(l, alt);
        return c;
    }
    
    protected SubtypeSet exceptions;
    
    public SubtypeSet exceptions() {
        return exceptions;
    }
    
    public Term exceptions(SubtypeSet exceptions) {
        Term_c n = (Term_c) copy();
        n.exceptions = new SubtypeSet(exceptions);
        return n;
    }
    
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        Term t = (Term) super.exceptionCheck(ec);
        //System.out.println("exceptions for " + t + " = " + ec.throwsSet());
        return t.exceptions(ec.throwsSet());
    }
}
