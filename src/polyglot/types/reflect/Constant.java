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
 * A Constant is used to represent an item in the constant pool of a class.
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public class Constant {
    protected int tag;
    protected Object value;

    /**
     * Constant tag for class types.
     * This is used to reference other classes, such as the superclass,
     * and is used by the checkcast and instanceof instructions.
     * The Fieldref, Methodref and InterfaceMethodref constant types
     * refer to this constant type.
     */
    public static final byte CLASS = 7;

    /**
     * Constant tag for field references.
     * This is used to reference a field in (possibly) another class.
     * The getfield, putfield, getstatic, and putstatic instructions use
     * this constant type.
     */
    public static final byte FIELD_REF = 9;

    /**
     * Constant tag for method references.
     * This is used to reference a method in (possibly) another class.
     * The invokevirtual, invokespecial, and invokestatic instructions use
     * this constant type.
     */
    public static final byte METHOD_REF = 10;

    /**
     * Constant tag for java.lang.String constants.
     * The actual string value is stored indirectly in a Utf8 constant.
     */
    public static final byte STRING = 8;

    /**
     * Constant tag for int, short, byte, char, and boolean constants. 
     */
    public static final byte INTEGER = 3;

    /**
     * Constant tag for float constants. 
     */
    public static final byte FLOAT = 4;

    /**
     * Constant tag for long constants. 
     */
    public static final byte LONG = 5;

    /**
     * Constant tag for double constants. 
     */
    public static final byte DOUBLE = 6;

    /**
     * Constant tag for method references.
     * This is used to reference a method in an interface.
     * The invokeinterface instruction uses this constant type.
     */
    public static final byte INTERFACE_METHOD_REF = 11;

    /**
     * Constant tag for holding the name and type of a field or method.
     * The Fieldref, Methodref and InterfaceMethodref constant types
     * refer to this constant type.
     */
    public static final byte NAME_AND_TYPE = 12;

    /**
     * Constant tag for holding the a UTF8 format string.
     * The string is used to hold the name and type descriptor for
     * NameandType constants, the class name for Class constants,
     * the string value for String constants.
     */
    public static final byte UTF8 = 1;

    /**
     * @param tag
     *        The constant's tag.
     * @param value
     *        The constant's value.
     */
    Constant(final int tag, final Object value) {
        this.tag = tag;
        this.value = value;
    }

    /**
     * Get the tag of the constant.
     *
     * @return
     *        The tag.
     */
    public final int tag() {
        return tag;
    }

    /**
     * Get the value of the constant.
     *
     * @return
     *        The value.
     */
    public final Object value() {
        return value;
    }

    /**
     * Hash the constant.
     *
     * @return
     *        The hash code.
     */
    @Override
    public int hashCode() {
        switch (tag) {
        case CLASS:
        case STRING:
        case INTEGER:
        case FLOAT:
        case LONG:
        case DOUBLE:
        case UTF8:
            return tag ^ value.hashCode();
        case FIELD_REF:
        case METHOD_REF:
        case INTERFACE_METHOD_REF:
        case NAME_AND_TYPE:
            return tag ^ ((int[]) value)[0] ^ ((int[]) value)[1];
        }

        return tag;
    }

    /**
     * Check if an object is equal to this constant.
     *
     * @param other
     *        The object to compare against.
     * @return
     *        true if equal, false if not.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Constant)) {
            return false;
        }

        Constant c = (Constant) other;

        if (tag != c.tag) {
            return false;
        }

        switch (tag) {
        case CLASS:
        case STRING:
        case INTEGER:
        case FLOAT:
        case LONG:
        case DOUBLE:
        case UTF8:
            return value.equals(c.value);
        case FIELD_REF:
        case METHOD_REF:
        case INTERFACE_METHOD_REF:
        case NAME_AND_TYPE:
            return ((int[]) value)[0] == ((int[]) c.value)[0]
                    && ((int[]) value)[1] == ((int[]) c.value)[1];
        }

        return false;
    }
}
