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
package polyglot.ext.jl8.types;

import polyglot.ext.jl5.types.JL5LocalInstance;
import polyglot.util.Position;

/**
 * A local variable or formal parameter in JL8.
 * <p>
 * Java 8 lets an inner class or a lambda expression refer to a local variable of
 * an enclosing method that is merely <i>effectively final</i>, rather than one
 * that is declared final (JLS 4.12.4). A variable is effectively final if it is
 * never assigned when it might already have a value, so the property depends on
 * assignments that may appear anywhere in the enclosing code declaration,
 * including after the expression that refers to the variable. It therefore
 * cannot be decided while type checking that expression.
 * <p>
 * Type checking instead records here that the variable is referred to from a
 * nested scope, and the definite assignment pass decides whether the variable is
 * effectively final once it has seen the whole code declaration.
 */
public interface JL8LocalInstance extends JL5LocalInstance {

    /**
     * The position at which this variable is first referred to from a nested
     * class body or lambda expression, or null if it never is.
     */
    Position capturedAt();

    /**
     * Record that this variable is referred to from a nested class body or
     * lambda expression at {@code pos}. Only the first such position is kept,
     * since it is used only to explain why the variable must be effectively
     * final.
     */
    void setCapturedAt(Position pos);
}
