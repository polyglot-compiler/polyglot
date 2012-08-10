/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 1997-2001 Purdue Research Foundation
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

package polyglot.types.reflect;

/**
 * Attribute is an abstract class for an attribute defined for a method,
 * field, or class.  An attribute consists of its name (represented as an
 * index into the constant pool) and its length.  Attribute is extended
 * to represent a constant value, code, exceptions, etc.
 *
 * @see polyglot.types.reflect ConstantValue
 * @see polyglot.types.reflect Exceptions
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public abstract class Attribute {
    protected int nameIndex;
    protected int length;

    /**
     * Constructor.
     *
     * @param nameIndex
     *        The index into the constant pool of the name of the attribute.
     * @param length
     *        The length of the attribute, excluding the header.
     */
    public Attribute(int nameIndex, int length) {
        this.nameIndex = nameIndex;
        this.length = length;
    }

    public int getName() {
        return nameIndex;
    }
}
