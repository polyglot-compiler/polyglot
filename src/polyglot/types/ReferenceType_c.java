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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * A {@code ReferenceType} represents a reference type --
 * a type on which contains methods and fields and is a subtype of
 * Object.
 */
public abstract class ReferenceType_c extends Type_c implements ReferenceType {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected ReferenceType_c() {
        super();
    }

    public ReferenceType_c(TypeSystem ts) {
        this(ts, null);
    }

    public ReferenceType_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public ReferenceType toReference() {
        return this;
    }

    @Override
    public List<? extends MemberInstance> members() {
        List<MemberInstance> l = new ArrayList<>();
        l.addAll(methods());
        l.addAll(fields());
        return l;
    }

    @Override
    public abstract List<? extends MethodInstance> methods();

    @Override
    public abstract List<? extends FieldInstance> fields();

    @Override
    public abstract Type superType();

    @Override
    public abstract List<? extends ReferenceType> interfaces();

    @Override
    public final boolean hasMethod(MethodInstance mi) {
        return ts.hasMethod(this, mi);
    }

    @Override
    public boolean hasMethodImpl(MethodInstance mi) {
        for (MethodInstance mj : methods()) {
            if (ts.isSameMethod(mi, mj)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (!ancestor.isCanonical()) {
            return false;
        }

        if (ancestor.isNull()) {
            return false;
        }

        if (ts.typeEquals(this, ancestor)) {
            return false;
        }

        if (!ancestor.isReference()) {
            return false;
        }

        if (ts.typeEquals(ancestor, ts.Object())) {
            return true;
        }

        // Next check interfaces.
        for (Type parentType : interfaces()) {
            if (ts.isSubtype(parentType, ancestor)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        return ts.isSubtype(this, toType);
    }

    @Override
    public List<? extends MethodInstance> methodsNamed(String name) {
        List<MethodInstance> l = new LinkedList<>();

        for (MethodInstance mi : methods()) {
            if (mi.name().equals(name)) {
                l.add(mi);
            }
        }

        return l;
    }

    @Override
    public List<? extends MethodInstance> methods(String name,
            List<? extends Type> argTypes) {
        List<MethodInstance> l = new LinkedList<>();

        for (MethodInstance mi : methodsNamed(name)) {
            if (mi.hasFormals(argTypes)) {
                l.add(mi);
            }
        }

        return l;
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from this to toType is valid; in other
     * words, some non-null members of this are also members of toType.
     **/
    @Override
    public boolean isCastValidImpl(Type toType) {
        if (!toType.isReference()) return false;
        return ts.isSubtype(this, toType) || ts.isSubtype(toType, this);
    }
}
