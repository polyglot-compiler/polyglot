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

package polyglot.types;

import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * Signals an error in the class resolver system. This exception is thrown
 * when a {@code ClassResolver} finds a class file that contains encoded 
 * Polyglot type information, but is unable to read in the serialized class 
 * information. The most likely cause of this exception is that the compiler 
 * (or compiler extension) has been modified since the class file was created, 
 * resulting in incompatible serializations. The solution is to delete the class 
 * file, and recompile it from the source.
 */
public class BadSerializationException extends SemanticException {
    private static final long serialVersionUID = SerialVersionUID.generate();

    private String className;

    private static String message(String className) {
        return "The deserialization of the Polyglot type information for \""
                + className + "\" failed. The most likely cause of this "
                + "failure is that the compiler (or compiler extension) has "
                + "been modified since the class file was created, resulting "
                + "in incompatible serializations. The solution is to delete "
                + "the class file for the class \"" + className
                + "\", and recompile it from the source.";
    }

    public BadSerializationException(String className) {
        super(message(className));
        this.className = className;
    }

    public BadSerializationException(String className, Position position) {
        super(message(className), position);
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
