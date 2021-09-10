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
package polyglot.ext.jl5.types;

import java.util.Collections;

import polyglot.types.ArrayType;
import polyglot.types.ArrayType_c;
import polyglot.types.MethodInstance;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public class JL5ArrayType_c extends ArrayType_c implements JL5ArrayType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected boolean isVarArg;

    public JL5ArrayType_c(TypeSystem ts, Position pos, Type base, boolean isVarargs) {
        super(ts, pos, base);
        this.isVarArg = isVarargs;
    }

    @Override
    protected MethodInstance createCloneMethodInstance() {
        return ts.methodInstance(
                position(),
                this,
                ts.Public(),
                this, // clone returns this type
                "clone",
                Collections.<Type>emptyList(),
                Collections.<Type>emptyList());
    }

    @Override
    public boolean isVarArg() {
        return this.isVarArg;
    }

    @Override
    public void setVarArg() {
        this.isVarArg = true;
    }

    @Override
    public boolean isSubtypeImpl(Type t) {
        if (super.isSubtypeImpl(t)) {
            return true;
        }

        /* See JLS 3rd Ed 4.10 */
        if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            return this.base().isSubtype(at.base());
        }

        return false;
    }
}
