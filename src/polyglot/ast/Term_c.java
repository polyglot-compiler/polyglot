package polyglot.ext.jl.ast;

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
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry() {
        return this;
    }

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
        if (this.reachable = reachability) {
            return this;
        }
        
        Term_c t = (Term_c) copy();
        t.reachable = reachability;
        return t;
    }

    /** Utility function to get the first entry of a list, or else alt. */
    public static Term listEntry(List l, Term alt) {
        Term c = (Term) CollectionUtil.firstOrElse(l, alt);
        if (c != alt) return c.entry();
        return alt;
    }
}
