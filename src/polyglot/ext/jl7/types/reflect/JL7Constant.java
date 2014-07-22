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

import polyglot.types.reflect.Constant;

/**
 * JL7Constant defines additional constants for the constant pool of JL7 class
 * files.
 */
public class JL7Constant extends Constant {
    /**
     * Constant tag for holding a method handle.
     */
    public static final byte METHOD_HANDLE = 15;

    /**
     * Constant tag for holding a method type.
     */
    public static final byte METHOD_TYPE = 16;

    /**
     * Constant tag for holding a bootstrap method.
     */
    public static final byte INVOKE_DYNAMIC = 18;

    /**
     * @param tag
     *        The constant's tag.
     * @param value
     *        The constant's value.
     */
    protected JL7Constant(final int tag, final Object value) {
        super(tag, value);
    }

    @Override
    public int hashCode() {
        switch (tag) {
        case METHOD_TYPE:
            return tag ^ value.hashCode();
        case METHOD_HANDLE:
        case INVOKE_DYNAMIC:
            return tag ^ ((int[]) value)[0] ^ ((int[]) value)[1];
        default:
            return super.hashCode();
        }
    }

    @Override
    protected boolean valueEquals(Constant c, int tag) {
        Object value = c.value();
        switch (tag) {
        case METHOD_TYPE:
            return this.value.equals(value);
        case METHOD_HANDLE:
        case INVOKE_DYNAMIC:
            return ((int[]) this.value)[0] == ((int[]) value)[0]
                    && ((int[]) this.value)[1] == ((int[]) value)[1];
        }
        return super.valueEquals(c, tag);
    }
}
