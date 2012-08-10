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

import polyglot.types.MethodInstance;

/**
 * A <code>Call</code> is an immutable representation of a Java
 * method call.  It consists of a method name and a list of arguments.
 * It may also have either a Type upon which the method is being
 * called or an expression upon which the method is being called.
 */
public interface Call extends Expr, ProcedureCall {
    /**
     * The call's target object.
     */
    Receiver target();

    /**
     * Set the call's target.
     */
    Call target(Receiver target);

    /**
     * The name of the method to call.
     */
    Id id();

    /**
     * Set the name of the method to call.
     */
    Call id(Id name);

    /**
     * The name of the method to call.
     */
    String name();

    /**
     * Set the name of the method to call.
     */
    Call name(String name);

    /**
     * Indicates if the target of this call is implicit, that 
     * is, was not specified explicitly in the syntax.  
     * @return boolean indicating if the target of this call is implicit
     */
    boolean isTargetImplicit();

    /**
     * Set whether the target of this call is implicit.
     */
    Call targetImplicit(boolean targetImplicit);

    /**
     * The call's actual arguments.
     * @return A list of {@link polyglot.ast.Expr Expr}.
     */
    @Override
    List<Expr> arguments();

    /**
     * Set the call's actual arguments.
     * @param arguments A list of {@link polyglot.ast.Expr Expr}.
     */
    @Override
    ProcedureCall arguments(List<Expr> arguments);

    /**
     * The type object of the method we are calling.  This is, generally, only
     * valid after the type-checking pass.
     */
    MethodInstance methodInstance();

    /**
     * Set the type object of the method we are calling.
     */
    Call methodInstance(MethodInstance mi);
}
