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

import polyglot.translate.ExtensionRewriter;
import polyglot.types.SemanticException;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.util.SubtypeSet;
import polyglot.visit.CFGBuilder;
import polyglot.visit.ExceptionChecker;

/**
 * A {@code Term} represents any Java expression or statement on which
 * dataflow can be performed.
 */
public abstract class Term_c extends Node_c implements Term, TermOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean reachable;
    protected SubtypeSet exceptions;

    // TODO
    // @Deprecated
    public Term_c(Position pos) {
        this(pos, null);
    }

    public Term_c(Position pos, Ext ext) {
        super(pos, ext);
    }

    @Override
    public boolean reachable() {
        return reachable;
    }

    @Override
    public Term reachable(boolean reachable) {
        return reachable(this, reachable);
    }

    protected <N extends Term_c> N reachable(N n, boolean reachable) {
        if (n.reachable == reachable) return n;
        n = copyIfNeeded(n);
        n.reachable = reachable;
        return n;
    }

    @Override
    public SubtypeSet exceptions() {
        return exceptions;
    }

    @Override
    public Term exceptions(SubtypeSet exceptions) {
        return exceptions(this, exceptions);
    }

    protected <N extends Term_c> N exceptions(N n, SubtypeSet exceptions) {
        if (n.exceptions == exceptions) return n;
        n = copyIfNeeded(n);
        n.exceptions = new SubtypeSet(exceptions);
        return n;
    }

    /** Utility function to get the first entry of a list, or else alt. */
    public static <T extends Term, U extends T, V extends T> T listChild(
            List<U> l, V alt) {
        return CollectionUtil.<T, U, V> firstOrElse(l, alt);
    }

    @Override
    public Node exceptionCheck(ExceptionChecker ec) throws SemanticException {
        Term_c t = (Term_c) super.exceptionCheck(ec);
        t = exceptions(t, ec.throwsSet());
        //System.out.println("exceptions for " + t + " = " + ec.throwsSet());
        return t;
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        Term_c t = (Term_c) super.extRewrite(rw);
        t = reachable(t, false);
        return t;
    }

    @Override
    public abstract Term firstChild();

    @Override
    public abstract <T> List<T> acceptCFG(CFGBuilder<?> v, List<T> succs);
}
