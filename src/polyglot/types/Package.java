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

package polyglot.types;

import polyglot.util.CodeWriter;

/**
 * An <code>Package</code> represents a Java package.
 */
public interface Package extends Qualifier, Named, Declaration {
    /**
     * The package's outer package.
     */
    Package prefix();

    /**
     * Return a string that is the translation of this package.
     * @param c A resolver in which to look up the package.
     */
    String translate(Resolver c);

    /** Return true if this package is equivalent to <code>p</code>. */
    boolean packageEquals(Package p);

    /** Return true if this package is equivalent to <code>p</code>. */
    boolean packageEqualsImpl(Package p);

    /** A resolver to access member packages and classes of the package. */
    Resolver resolver();

    /** Pretty-print this package name to w. */
    void print(CodeWriter w);
}
