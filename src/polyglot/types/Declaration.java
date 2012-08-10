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

/**
 * A Declaration is a type object that has declarations and uses. Some instances
 * may be uses of the declaration; these have references to the original
 * declaration. For example, extensions may perform substitutions on the
 * original declaration to produce the type object for a use of the declaration.
 * To make it easy to create distinct uses by copying the original declaration
 * object, copy() will preserve the pointer to the original declaration; it
 * won't update it to point to the copy. A Declaration used as a declaration has
 * a reference to itself.
 */
public interface Declaration extends TypeObject {
    /** Get the original declaration. */
    Declaration declaration();

    /** Set the original declaration. */
    void setDeclaration(Declaration decl);
}
