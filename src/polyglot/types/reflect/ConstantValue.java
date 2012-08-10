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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * The ConstantValue attribute stores an index into the constant pool
 * that represents constant value.  A class's static fields have 
 * constant value attributes.
 *
 * @see polyglot.types.reflect Field
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public class ConstantValue extends Attribute {
    private int index;

    /**
     * Constructor.  Create a ConstantValue attribute from a data stream.
     *
     * @param in
     *        The data stream of the class file.
     * @param nameIndex
     *        The index into the constant pool of the name of the attribute.
     * @param length
     *        The length of the attribute, excluding the header.
     * @exception IOException
     *        If an error occurs while reading.
     */
    public ConstantValue(DataInputStream in, int nameIndex, int length)
            throws IOException {
        super(nameIndex, length);
        index = in.readUnsignedShort();
    }

    public int getIndex() {
        return index;
    }
}
