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

import polyglot.types.ConstructorInstance;
import polyglot.types.ParsedClassType;
import java.util.List;

/**
 * A <code>New</code> is an immutable representation of the use of the
 * <code>new</code> operator to create a new instance of a class.  In
 * addition to the type of the class being created, a <code>New</code> has a
 * list of arguments to be passed to the constructor of the object and an
 * optional <code>ClassBody</code> used to support anonymous classes.
 */
public interface New extends Expr, ProcedureCall {
    /** The type object for anonymous classes, or null. */
    ParsedClassType anonType();

    /** Set the type object for anonymous classes. */
    New anonType(ParsedClassType anonType);

    /** The constructor invoked by this expression. */
    ConstructorInstance constructorInstance();

    /** Set the constructor invoked by this expression. */
    New constructorInstance(ConstructorInstance ci);

    /**
     * The qualifier expression for the type, or null. If non-null, this
     * expression creates an inner class of the static type of the qualifier.
     */
    Expr qualifier();

    /** Set the qualifier expression for the type. */
    New qualifier(Expr qualifier);

    /** The type we are creating, possibly qualified by qualifier. */
    TypeNode objectType();

    /** Set the type we are creating. */
    New objectType(TypeNode t);

    /** Actual arguments to pass to the constructor.
     * @return A list of {@link polyglot.ast.Expr Expr}.
     */
    @Override
    List<Expr> arguments();

    /** Set the actual arguments to pass to the constructor.
     * @param arguments A list of {@link polyglot.ast.Expr Expr}.
     */
    @Override
    ProcedureCall arguments(List<Expr> arguments);

    /** The class body for anonymous classes, or null. */
    ClassBody body();

    /** Set the class body for anonymous classes. */
    New body(ClassBody b);
}
