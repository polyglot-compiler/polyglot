/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.ast;

import polyglot.util.SubtypeSet;
import polyglot.visit.*;
import java.util.*;

/**
 * A <code>Term</code> represents any Java expression or statement on which
 * dataflow can be performed.
 */
public interface Term extends Node
{
    /**
     * Return the first direct subterm performed when evaluating this term. If
     * this term has no subterms, this should return null.
     * 
     * This method is similar to the deprecated entry(), but it should *not*
     * recursively drill down to the innermost subterm. The direct child visited
     * first in this term's dataflow should be returned.
     */
    public Term firstChild();

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
    
    /**
     * List of Types with all exceptions possibly thrown by this term.
     * The list is not necessarily correct until after exception-checking.
     * <code>polyglot.ast.NodeOps.throwTypes()</code> is similar, but exceptions
     * are not propagated to the containing node.
     */
    public SubtypeSet exceptions();
    public Term exceptions(SubtypeSet exceptions);
}
