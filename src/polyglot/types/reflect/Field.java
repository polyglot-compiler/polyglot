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
import java.io.EOFException;
import java.io.IOException;

import polyglot.types.SemanticException;
import polyglot.types.Type;

/**
 * Field models a field (member variable) in a class.  The Field class
 * grants access to information such as the field's modifiers, its name
 * and type descriptor (represented as indices into the constant pool),
 * and any attributes of the field.  Static fields have a ConstantValue
 * attribute.
 *
 * @see polyglot.types.reflect ConstantValue
 *
 * @author Nate Nystrom
 *         (<a href="mailto:nystrom@cs.purdue.edu">nystrom@cs.purdue.edu</a>)
 */
public class Field {
    protected DataInputStream in;
    protected ClassFile clazz;
    protected int modifiers;
    protected int name;
    protected int type;
    protected Attribute[] attrs;
    protected ConstantValue constantValue;
    protected boolean synthetic;

    /**
     * Constructor.  Read a field from a class file.
     *
     * @param in
     *        The data stream of the class file.
     * @param clazz
     *        The class file containing the field.
     * @exception IOException
     *        If an error occurs while reading.
     */
    protected Field(DataInputStream in, ClassFile clazz) throws IOException {
        this.clazz = clazz;
        this.in = in;
    }

    public void initialize() throws IOException {
        modifiers = in.readUnsignedShort();

        name = in.readUnsignedShort();
        type = in.readUnsignedShort();

        int numAttributes = in.readUnsignedShort();

        attrs = new Attribute[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            int nameIndex = in.readUnsignedShort();
            int length = in.readInt();

            Constant name = clazz.getConstants()[nameIndex];

            if (name != null) {
                if ("ConstantValue".equals(name.value())) {
                    constantValue = new ConstantValue(in, nameIndex, length);
                    attrs[i] = constantValue;
                }
                if ("Synthetic".equals(name.value())) {
                    synthetic = true;
                }
            }

            if (attrs[i] == null) {
                long n = in.skip(length);
                if (n != length) {
                    throw new EOFException();
                }
            }
        }
        this.in = null; // RMF 7/23/2008 - Don't need the input stream any more, so don't hang onto it
    }

    /**
     * Return true of t is java.lang.String.
     * We don't compare against ts.String() because ts.String() may not
     * yet be set.
     */
    public boolean isString(Type t) {
        return t.isClass() && t.toClass().isTopLevel()
                && t.toClass().fullName().equals("java.lang.String");
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public boolean isConstant() {
        return this.constantValue != null;
    }

    public Constant constantValue() {
        if (this.constantValue != null) {
            int index = this.constantValue.getIndex();
            return clazz.getConstants()[index];
        }

        return null;
    }

    public int getInt() throws SemanticException {
        Constant c = constantValue();

        if (c != null && c.tag() == Constant.INTEGER) {
            Integer v = (Integer) c.value();
            return v.intValue();
        }

        throw new SemanticException("Could not find expected constant "
                + "pool entry with tag INTEGER.");
    }

    public float getFloat() throws SemanticException {
        Constant c = constantValue();

        if (c != null && c.tag() == Constant.FLOAT) {
            Float v = (Float) c.value();
            return v.floatValue();
        }

        throw new SemanticException("Could not find expected constant "
                + "pool entry with tag FLOAT.");
    }

    public double getDouble() throws SemanticException {
        Constant c = constantValue();

        if (c != null && c.tag() == Constant.DOUBLE) {
            Double v = (Double) c.value();
            return v.doubleValue();
        }

        throw new SemanticException("Could not find expected constant "
                + "pool entry with tag DOUBLE.");
    }

    public long getLong() throws SemanticException {
        Constant c = constantValue();

        if (c != null && c.tag() == Constant.LONG) {
            Long v = (Long) c.value();
            return v.longValue();
        }

        throw new SemanticException("Could not find expected constant "
                + "pool entry with tag LONG.");
    }

    public String getString() throws SemanticException {
        Constant c = constantValue();

        if (c != null && c.tag() == Constant.STRING) {
            Integer i = (Integer) c.value();
            c = clazz.getConstants()[i.intValue()];

            if (c != null && c.tag() == Constant.UTF8) {
                String v = (String) c.value();
                return v;
            }
        }

        throw new SemanticException("Could not find expected constant "
                + "pool entry with tag STRING or UTF8.");
    }

    public Attribute[] getAttrs() {
        return attrs;
    }

    public ClassFile getClazz() {
        return clazz;
    }

    public ConstantValue getConstantValue() {
        return constantValue;
    }

    public int getModifiers() {
        return modifiers;
    }

    public int getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String name() {
        return (String) clazz.getConstants()[this.name].value();
    }
}
