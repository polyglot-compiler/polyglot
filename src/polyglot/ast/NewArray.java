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

/**
 * A {@code NewArray} represents a new array expression such as
 * {@code new File[8][] { null }}.  It consists of an element type (e.g.,
 * {@code File}), a list of dimension expressions (e.g., 8), 0 or more
 * additional dimensions (e.g., 1 for {@code []}), and an array initializer.
 * The dimensions of the array initializer must equal the number of additional
 * {@code []} dimensions.
 */
public interface NewArray extends Expr {
    /** The array's base type. */
    TypeNode baseType();

    /** Set the array's base type. */
    NewArray baseType(TypeNode baseType);

    /**
     * The number of array dimensions.
     * Same as dims().size() + additionalDims().
     */
    int numDims();

    /** List of dimension expressions.
     * @return A list of {@link polyglot.ast.Expr Expr}.
     */
    List<Expr> dims();

    /** Set the list of dimension expressions.
     * @param dims A list of {@link polyglot.ast.Expr Expr}.
     */
    NewArray dims(List<Expr> dims);

    /** The number of additional dimensions. */
    int additionalDims();

    /** Set the number of additional dimensions. */
    NewArray additionalDims(int addDims);

    /** The array initializer, or null. */
    ArrayInit init();

    /** Set the array initializer. */
    NewArray init(ArrayInit init);
}
