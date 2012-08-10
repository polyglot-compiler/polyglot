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

import polyglot.types.ConstructorInstance;
import polyglot.util.Enum;

/**
 * A <code>ConstructorCall</code> represents a direct call to a constructor.
 * For instance, <code>super(...)</code> or <code>this(...)</code>.
 */
public interface ConstructorCall extends Stmt, ProcedureCall {
    /** Constructor call kind: either "super" or "this". */
    public static class Kind extends Enum {
        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind SUPER = new Kind("super");
    public static final Kind THIS = new Kind("this");

    /** The qualifier of the call, possibly null. */
    Expr qualifier();

    /** Set the qualifier of the call, possibly null. */
    ConstructorCall qualifier(Expr qualifier);

    /** The kind of the call: THIS or SUPER. */
    Kind kind();

    /** Set the kind of the call: THIS or SUPER. */
    ConstructorCall kind(Kind kind);

    /**
     * Actual arguments.
     * @return A list of {@link polyglot.ast.Expr Expr}.
     */
    @Override
    List<Expr> arguments();

    /**
     * Set the actual arguments.
     * @param arguments A list of {@link polyglot.ast.Expr Expr}.
     */
    @Override
    ProcedureCall arguments(List<Expr> arguments);

    /**
     * The constructor that is called.  This field may not be valid until
     * after type checking.
     */
    ConstructorInstance constructorInstance();

    /** Set the constructor to call. */
    ConstructorCall constructorInstance(ConstructorInstance ci);
}
