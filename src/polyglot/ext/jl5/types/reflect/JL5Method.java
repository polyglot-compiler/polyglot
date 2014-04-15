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
package polyglot.ext.jl5.types.reflect;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.Constant;
import polyglot.types.reflect.Exceptions;
import polyglot.types.reflect.Method;

public class JL5Method extends Method {
    protected JL5Signature signature;
    protected Annotations runtimeVisibleAnnotations;
    protected Annotations runtimeInvisibleAnnotations;

    /**
     * Record whether an annotation has a default value.
     */
    protected boolean defaultVal;

    public JL5Method(DataInputStream in, ClassFile clazz) {
        super(in, clazz);
//        System.err.println("JL5Method created for " + clazz);
    }

    @Override
    public void initialize() throws IOException {
        modifiers = in.readUnsignedShort();

        name = in.readUnsignedShort();
        type = in.readUnsignedShort();
//        System.err.println("JL5Method.initialize() for " + clazz );

        int numAttributes = in.readUnsignedShort();

        attrs = new Attribute[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            int nameIndex = in.readUnsignedShort();
            int length = in.readInt();

            Constant name = clazz.getConstants()[nameIndex];
//            System.err.println("    " + name.value());

            if (name != null) {
                if ("Exceptions".equals(name.value())) {
                    exceptions = new Exceptions(clazz, in, nameIndex, length);
                    attrs[i] = exceptions;
                }
                if ("Synthetic".equals(name.value())) {
                    synthetic = true;
                }
                if ("AnnotationDefault".equals(name.value())) {
                    defaultVal = true;
                }
                if ("Signature".equals(name.value())) {
                    signature = new JL5Signature(clazz, in, nameIndex, length);
                    attrs[i] = signature;
                }
                if ("RuntimeVisibleAnnotations".equals(name.value())) {
                    runtimeVisibleAnnotations =
                            new Annotations(clazz, in, nameIndex, length);
                    attrs[i] = runtimeVisibleAnnotations;
                }
                if ("RuntimeInvisibleAnnotations".equals(name.value())) {
                    runtimeVisibleAnnotations =
                            new Annotations(clazz, in, nameIndex, length);
                    attrs[i] = runtimeVisibleAnnotations;
                }
            }

            if (attrs[i] == null) {
                long n = in.skip(length);
                if (n != length) {
                    throw new EOFException();
                }
            }
        }
        this.in = null; // RMF 7/23/2008 - Don't need the input stream any more,
                        // so don't hang onto it
    }

    public JL5Signature getSignature() {
        return signature;
    }

    public boolean hasDefaultVal() {
        return defaultVal;
    }

    public Annotations getRuntimeVisibleAnnotations() {
        return this.runtimeVisibleAnnotations;
    }

    public Annotations getRuntimeInvisibleAnnotations() {
        return this.runtimeInvisibleAnnotations;
    }
}
