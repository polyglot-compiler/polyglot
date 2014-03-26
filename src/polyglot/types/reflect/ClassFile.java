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
import java.net.URI;

/**
 * Interface for defining .class files
 */
public interface ClassFile {

    URI getClassFileURI();

    /**
     * Get the encoded source modified time.
     */
    long sourceLastModified(String ts);

    /**
     * Get the encoded compiler version used to compile the source.
     */
    String compilerVersion(String ts);

    /**
     * Get the encoded class type for the given type system.
     */
    String encodedClassType(String typeSystemKey);

    /**
     * Get the class name at the given constant pool index.
     */
    String classNameCP(int index);

    /**
     * Get the name of the class, including the package name.
     * 
     * @return The name of the class.
     */
    String name();

    /**
     * Read the class's attributes. Since none of the attributes are required,
     * just read the length of each attribute and skip that many bytes.
     * 
     * @param in
     *            The stream from which to read.
     * @exception IOException
     *                If an error occurs while reading.
     */
    void readAttributes(DataInputStream in) throws IOException;

    Method createMethod(DataInputStream in) throws IOException;

    Field createField(DataInputStream in) throws IOException;

    Attribute createAttribute(DataInputStream in, String name, int nameIndex,
            int length) throws IOException;

    Attribute[] getAttrs();

    Constant[] getConstants();

    Field[] getFields();

    InnerClasses getInnerClasses();

    int[] getInterfaces();

    Method[] getMethods();

    int getModifiers();

    int getSuperClass();

    int getThisClass();

}
