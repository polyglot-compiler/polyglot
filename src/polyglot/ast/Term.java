/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.List;

import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;

/**
 * A <code>Term</code> represents any Java expression or statement on which
 * dataflow can be performed.
 */
public interface Term extends Node {
    /**
     * Indicates to dataflow methods that we are looking at the entry of a term.
     */
    public static final int ENTRY = 1;

    /**
     * Indicates to dataflow methods that we are looking at the exit of a term.
     */
    public static final int EXIT = 0;

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
    public <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs);

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
