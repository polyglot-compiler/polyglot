/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
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

package polyglot.ext.param.types;

import java.util.List;

import polyglot.types.ClassType;
import polyglot.types.Package;
import polyglot.types.SemanticException;
import polyglot.types.TypeObject;
import polyglot.types.TypeObject_c;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * A base implementation for parametric classes.
 * This class is a wrapper around
 * a ClassType that associates formal parameters with the class.
 * formals can be any type object.
 */
public abstract class PClass_c<Formal extends Param, Actual extends TypeObject>
        extends TypeObject_c implements PClass<Formal, Actual> {
    protected PClass_c() {
    }

    public PClass_c(TypeSystem ts) {
        this(ts, null);
    }

    public PClass_c(TypeSystem ts, Position pos) {
        super(ts, pos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParamTypeSystem<Formal, Actual> typeSystem() {
        return (ParamTypeSystem<Formal, Actual>) super.typeSystem();
    }

    /////////////////////////////////////////////////////////////////////////
    // Implement PClass

    @Override
    public ClassType instantiate(Position pos, List<Actual> actuals)
            throws SemanticException {
        ParamTypeSystem<Formal, Actual> pts = typeSystem();
        return pts.instantiate(pos, this, actuals);
    }

    /////////////////////////////////////////////////////////////////////////
    // Implement TypeObject

    @Override
    public boolean isCanonical() {
        if (!clazz().isCanonical()) {
            return false;
        }

        for (Param p : formals()) {
            if (!p.isCanonical()) {
                return false;
            }
        }

        return true;
    }

    /////////////////////////////////////////////////////////////////////////
    // Implement Named

    @Override
    public String name() {
        return clazz().name();
    }

    @Override
    public String fullName() {
        return clazz().fullName();
    }

    /////////////////////////////////////////////////////////////////////////
    // Implement Importable

    @Override
    public Package package_() {
        return clazz().package_();
    }

}
