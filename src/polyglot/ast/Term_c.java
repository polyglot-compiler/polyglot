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
    public List acceptCFG(CFGBuilder v, List succs) {
        return succs;
    }

    /** Utility function to get the first entry of a list, or else alt. */
    public static Term listEntry(List l, Term alt) {
        Term c = (Term) CollectionUtil.firstOrElse(l, alt);
        if (c != alt) return c.entry();
        return alt;
    }
}
