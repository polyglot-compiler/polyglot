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
import polyglot.types.Type;

/**
 * An <code>ArrayInit</code> is an immutable representation of
 * an array initializer, such as { 3, 1, { 4, 1, 5 } }.  Note that
 * the elements of these array may be expressions of any type (e.g.,
 * <code>Call</code>).
 */
public interface ArrayInit extends Expr {
    /**
     * Get the initializer elements.
     * @return A list of {@link polyglot.ast.Expr Expr}.
     */
    List<Expr> elements();

    /**
     * Set the initializer elements.
     * @param elements A list of {@link polyglot.ast.Expr Expr}.
     */
    ArrayInit elements(List<Expr> elements);

    /**
     * Type check the individual elements of the array initializer against the
     * left-hand-side type.  Each element is checked to see if it can be
     * assigned to a variable of type lhsType.
     * @param lhsType Type to compare against.
     * @exception SemanticException if there is a type error.
     */
    void typeCheckElements(Type lhsType) throws SemanticException;
}
