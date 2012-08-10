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

import polyglot.util.Enum;

/**
 * An <code>Import</code> is an immutable representation of a Java
 * <code>import</code> statement.  It consists of the string representing the
 * item being imported and the kind which is either indicating that a class
 * is being imported, or that an entire package is being imported.
 */
public interface Import extends Node {
    /** Import kinds: class (e.g., import java.util.Set) or package (e.g.,
     *  import java.util.*).
     *
     * PACKAGE is a bit of a misnomer, since we can import p.C.*, where p.C
     * is a class.  This puts the nested classes of p.C in scope.
     */
    public static class Kind extends Enum {
        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind CLASS = new Kind("class");
    public static final Kind PACKAGE = new Kind("package");

    /** Get the name of the class or package to import. */
    String name();

    /** Set the name of the class or package to import. */
    Import name(String name);

    /** Get the kind of import. */
    Kind kind();

    /** Set the kind of import. */
    Import kind(Kind kind);
}
