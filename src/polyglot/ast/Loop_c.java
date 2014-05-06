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

import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * An immutable representation of a loop
 * statement.  It contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public abstract class Loop_c extends Stmt_c implements Loop, LoopOps {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected Expr cond;
    protected Stmt body;

//    @Deprecated
    public Loop_c(Position pos, Expr cond, Stmt body) {
        this(pos, cond, body, null);
    }

    public Loop_c(Position pos, Expr cond, Stmt body, Ext ext) {
        super(pos, ext);
        assert (body != null);
        this.body = body;
        this.cond = cond;
    }

    @Override
    public Expr cond() {
        return this.cond;
    }

    @Override
    public Loop cond(Expr cond) {
        return cond(this, cond);
    }

    protected <N extends Loop_c> N cond(N n, Expr cond) {
        if (n.cond == cond) return n;
        n = copyIfNeeded(n);
        n.cond = cond;
        return n;
    }

    @Override
    public boolean condIsConstant(JLang lang) {
        return lang.isConstant(cond(), lang);
    }

    @Override
    public boolean condIsConstantTrue(JLang lang) {
        return Boolean.TRUE.equals(lang.constantValue(cond(), lang));
    }

    @Override
    public boolean condIsConstantFalse(JLang lang) {
        return Boolean.FALSE.equals(lang.constantValue(cond(), lang));
    }

    @Override
    public Stmt body() {
        return this.body;
    }

    @Override
    public Loop body(Stmt body) {
        return body(this, body);
    }

    protected <N extends Loop_c> N body(N n, Stmt body) {
        if (n.body == body) return n;
        n = copyIfNeeded(n);
        n.body = body;
        return n;
    }

    /** Reconstruct the statement. */
    protected <N extends Loop_c> N reconstruct(N n, Expr cond, Stmt body) {
        n = cond(n, cond);
        n = body(n, body);
        return n;
    }
}
