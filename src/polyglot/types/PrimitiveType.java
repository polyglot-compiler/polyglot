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

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

/**
 * A {@code PrimitiveType} represents a type which may not be directly 
 * coerced to java.lang.Object (under the standard Java type system).    
 * <p>
 * This class should never be instantiated directly. Instead, you should
 * use the {@code TypeSystem.get*} methods.
 */
public interface PrimitiveType extends Type, Named {
    /** The kind of the primitive type. */
    public static class Kind extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind VOID = new Kind("void");
    public static final Kind BOOLEAN = new Kind("boolean");
    public static final Kind BYTE = new Kind("byte");
    public static final Kind CHAR = new Kind("char");
    public static final Kind SHORT = new Kind("short");
    public static final Kind INT = new Kind("int");
    public static final Kind LONG = new Kind("long");
    public static final Kind FLOAT = new Kind("float");
    public static final Kind DOUBLE = new Kind("double");

    /**
     * The kind of primitive.
     */
    Kind kind();

    /**
     * A string representing the type used to box this primitive.
     */
    String wrapperTypeString(TypeSystem ts);
}
