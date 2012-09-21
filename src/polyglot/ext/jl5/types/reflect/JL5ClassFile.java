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
import java.io.IOException;

import javax.tools.FileObject;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.reflect.Attribute;
import polyglot.types.reflect.ClassFile_c;
import polyglot.types.reflect.Field;
import polyglot.types.reflect.Method;

public class JL5ClassFile extends ClassFile_c {
    private JL5Signature signature;
    private Annotations runtimeVisibleAnnotations;
    private Annotations runtimeInvisibleAnnotations;

    public JL5ClassFile(FileObject classFileSource, byte[] code,
            ExtensionInfo ext) throws IOException {
        super(classFileSource, code, ext);
    }

    @Override
    public Method createMethod(DataInputStream in) throws IOException {
        Method m = new JL5Method(in, this);
        m.initialize();
        return m;
    }

    @Override
    public Field createField(DataInputStream in) throws IOException {
        Field f = new JL5Field(in, this);
        f.initialize();
        return f;
    }

    @Override
    public Attribute createAttribute(DataInputStream in, String name,
            int nameIndex, int length) throws IOException {
        if (name.equals("Signature")) {
            signature = new JL5Signature(this, in, nameIndex, length);
            return signature;
        }
        if (name.equals("RuntimeVisibleAnnotations")) {
            runtimeVisibleAnnotations =
                    new Annotations(this, in, nameIndex, length);
            return runtimeVisibleAnnotations;
        }
        if (name.equals("RuntimeInvisibleAnnotations")) {
            runtimeInvisibleAnnotations =
                    new Annotations(this, in, nameIndex, length);
            return runtimeInvisibleAnnotations;
        }
        return super.createAttribute(in, name, nameIndex, length);
    }

    public JL5Signature getSignature() {
        return signature;
    }

    public Annotations getRuntimeVisibleAnnotations() {
        return this.runtimeVisibleAnnotations;
    }

    public Annotations getRuntimeInvisibleAnnotations() {
        return this.runtimeInvisibleAnnotations;
    }

}
