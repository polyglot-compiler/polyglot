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

/**
 * A {@code Case} is a representation of a Java {@code case}
 * statement.  It can only be contained in a {@code Switch}.
 */
public interface Case extends SwitchElement {
    /**
     * Get the case label.  This must should a constant expression.
     * The case label is null for the {@code default} case.
     */
    Expr expr();

    /**
     * Set the case label.  This must should a constant expression,
     * or null.
     */
    Case expr(Expr expr);

    /** Returns true iff this is the default case. */
    boolean isDefault();

    /**
     * Returns the value of the case label.  This value is only valid
     * after type-checking.
     */
    long value();

    /**
     * Set the value of the case label.
     */
    Case value(long value);
}
