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

import polyglot.types.SemanticException;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;

/**
 * A <code>Term</code> represents any Java expression or statement on which
 * dataflow can be performed.
 */
public abstract class Term_c extends Node_c implements Term {
    public Term_c(Position pos) {
        super(pos);
    }

    protected boolean reachable;

    /**
     * Visit this term in evaluation order.
     */
    @Override
    public abstract <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs);

    /**
     * Return true if this term is eachable.  This attribute is not
     * guaranteed correct until after the reachability pass
     *
     * @see polyglot.visit.ReachChecker
     */
    @Override
    public boolean reachable() {
        return reachable;
    }

    /**
     * Set the reachability of this term.
     */
    @Override
    public Term reachable(boolean reachability) {
        if (this.reachable == reachability) {
            return this;
        }

        Term_c t = (Term_c) copy();
        t.reachable = reachability;
        return t;
    }

    /** Utility function to get the first entry of a list, or else alt. */
    public static <T extends Term, U extends T, V extends T> T listChild(
            List<U> l, V alt) {
        return CollectionUtil.<T, U, V> firstOrElse(l, alt);
    }

    protected SubtypeSet exceptions;

    @Override
    public SubtypeSet exceptions() {
        return exceptions;
    }

    @Override
    public Term exceptions(SubtypeSet exceptions) {
        Term_c n = (Term_c) copy();
        n.exceptions = new SubtypeSet(exceptions);
        return n;
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        Term t = (Term) super.exceptionCheck(ec);
        //System.out.println("exceptions for " + t + " = " + ec.throwsSet());
        return t.exceptions(ec.throwsSet());
    }
}
