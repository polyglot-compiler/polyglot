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

package polyglot.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Exceptions describes the types of exceptions that a method may throw.
 * The Exceptions attribute stores a list of indices into the constant
 * pool of the typs of exceptions thrown by the method.
 *
 * @see polyglot.types.reflect Method
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public class InnerClasses extends Attribute {
    private Info[] classes;

    public static class Info {
        public int classIndex;
        public int outerClassIndex;
        public int nameIndex;
        public int modifiers;
    }

    /**
     * Constructor.  Create an Exceptions attribute from a data stream.
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
    public InnerClasses(DataInputStream in, int nameIndex, int length) throws IOException {
        super(nameIndex, length);

        int count = in.readUnsignedShort();

        classes = new Info[count];

        for (int i = 0; i < count; i++) {
            classes[i] = new Info();

            // index of a Constant.CLASS
            classes[i].classIndex = in.readUnsignedShort();

            // index of a Constant.CLASS != 0 iff a member class.
            classes[i].outerClassIndex = in.readUnsignedShort();

            // index of a Constant.UTF == 0 iff an anonymous class.
            classes[i].nameIndex = in.readUnsignedShort();

            // modifiers of inner class
            classes[i].modifiers = in.readUnsignedShort();
        }
    }

    public Info[] getClasses() {
        return classes;
    }
}
