package polyglot.ast;

import polyglot.visit.*;
import java.util.*;

/**
 * A <code>Term</code> represents any Java expression or statement on which
 * dataflow can be performed.
 */
public interface Term extends Node
{
    /**
     * Return the first (sub)term performed when evaluating this
     * term.
     */
    public Term entry();

    /**
     * Visit this node, calling calling v.edge() for each successor in succs,
     * if data flows on that edge.
     */
    public List acceptCFG(CFGBuilder v, List succs);
    
    /**
     * Returns true if the term is reachable.  This attribute is not
     * guaranteed correct until after the reachability pass.
     *
     * @see polyglot.visit.ReachChecker
     */
    public boolean reachable();

    /**
     * Set the reachability of this term.
     */
    public Term reachable(boolean reachability);
}
