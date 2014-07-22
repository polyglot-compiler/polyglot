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
package polyglot.ext.jl7.types.reflect;

import java.io.DataInputStream;
import java.io.IOException;

import javax.tools.FileObject;

import polyglot.ext.jl5.types.reflect.JL5ClassFile;
import polyglot.frontend.ExtensionInfo;
import polyglot.types.reflect.Constant;

public class JL7ClassFile_c extends JL5ClassFile {
    public JL7ClassFile_c(FileObject classFileSource, byte[] code,
            ExtensionInfo ext) throws IOException {
        super(classFileSource, code, ext);
    }

    @Override
    protected Object readConstantInfo(DataInputStream in, int tag)
            throws IOException {
        switch (tag) {
        case JL7Constant.METHOD_HANDLE:
            return new int[] { in.readUnsignedByte(), in.readUnsignedShort() };
        case JL7Constant.METHOD_TYPE:
            return new Integer(in.readUnsignedShort());
        case JL7Constant.INVOKE_DYNAMIC:
            return new int[] { in.readUnsignedShort(), in.readUnsignedShort() };
        default:
            return super.readConstantInfo(in, tag);
        }
    }

    @Override
    protected Constant createConstant(int tag, Object value) {
        return new JL7Constant(tag, value);
    }
}
