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

import java.io.Serializable;

import polyglot.util.Copy;
import polyglot.util.Position;

/**
 * A <code>TypeObject</code> is a compile-time value created by the type system.
 * It is a static representation of a type that is not necessarily 
 * first-class.  It is similar to a compile-time meta-object.
 */
public interface TypeObject extends Copy, Serializable {
    /**
     * Return true if the type object contains no unknown/ambiguous types.
     */
    boolean isCanonical();

    /**
     * The object's type system.
     */
    TypeSystem typeSystem();

    /**
     * The object's position, or null.
     */
    Position position();

    /**
     * Return true iff this type object is the same as <code>t</code>.
     * All Polyglot extensions should attempt to maintain pointer
     * equality between equal TypeObjects.  If this cannot be done,
     * extensions can override TypeObject_c.equalsImpl(), and
     * don't forget to override hashCode().
     *
     * @see polyglot.types.TypeObject_c#equalsImpl(TypeObject)
     * @see java.lang.Object#hashCode()
     */
    boolean equalsImpl(TypeObject t);
}
