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

import java.util.LinkedList;
import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.PrimitiveType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

public abstract class JL5ClassType_c extends ClassType_c implements
        JL5ClassType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected JL5ClassType_c() {
    }

    public JL5ClassType_c(JL5TypeSystem ts) {
        this(ts, null);
    }

    public JL5ClassType_c(JL5TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    @Override
    public abstract List<EnumInstance> enumConstants();

    @Override
    public EnumInstance enumConstantNamed(String name) {
        for (EnumInstance ei : enumConstants()) {
            if (ei.name().equals(name)) {
                return ei;
            }
        }
        return null;
    }

    @Override
    public boolean isCastValidImpl(Type toType) {
        if (super.isCastValidImpl(toType)) {
            return true;
        }
        return (this.isSubtype(toType) || toType.isSubtype(this));
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        throw new InternalCompilerError("Should not be called in JL5");
    }

    @Override
    public LinkedList<Type> isImplicitCastValidChainImpl(Type toType) {
        JL5TypeSystem ts = (JL5TypeSystem) this.ts;
        LinkedList<Type> chain = null;
        if (ts.isSubtype(this, toType)) {
            chain = new LinkedList<>();
            chain.add(this);
            chain.add(toType);
        }
        else if (toType.isPrimitive()) {
            // see if unboxing will let us cast to the primitive
            PrimitiveType pt = toType.toPrimitive();
            ClassType wrapperType = ts.wrapperClassOfPrimitive(pt);
            chain = ts.isImplicitCastValidChain(this, wrapperType);
            if (chain != null) {
                chain.addLast(toType);
            }
        }
        return chain;
    }

    @Override
    public String translate(Resolver c) {
        // it is a nested class of a parameterized class, use the full name.
        if (isMember()) {
            ClassType container = container().toClass();
            if (container instanceof JL5SubstClassType) {
                container = ((JL5SubstClassType) container).base();
            }
            if (container instanceof JL5ParsedClassType
                    && !((JL5ParsedClassType) container).typeVariables()
                                                        .isEmpty()) {
                return container().translate(c) + "." + name();
            }
        }
        return super.translate(c);
    }

}
