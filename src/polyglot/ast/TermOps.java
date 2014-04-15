/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.List;

import polyglot.visit.CFGBuilder;

/**
 * This interface allows extensions both to override and reuse functionality in
 * Term_c.
 */
public interface TermOps {
    /**
     * Return the first direct subterm performed when evaluating this term. If
     * this term has no subterms, this should return null.
     * 
     * This method is similar to the deprecated entry(), but it should *not*
     * recursively drill down to the innermost subterm. The direct child visited
     * first in this term's dataflow should be returned.
     */
    Term firstChild();

    /**
     * Visit this term in evaluation order, calling v.edge() for each successor
     * in succs, if data flows on that edge.
     */
    <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs);
}
