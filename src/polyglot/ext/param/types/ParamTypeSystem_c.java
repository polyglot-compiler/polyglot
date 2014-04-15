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

package polyglot.ext.param.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem_c;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * Implementation of type system for parameterized types.
 */
public abstract class ParamTypeSystem_c<Formal extends Param, Actual extends TypeObject>
        extends TypeSystem_c implements ParamTypeSystem<Formal, Actual> {
    protected Map<Map<Formal, ? extends Actual>, Subst<Formal, Actual>> substCache =
            new HashMap<>();

    @Override
    public MuPClass<Formal, Actual> mutablePClass(Position pos) {
        return new MuPClass_c<>(this, pos);
    }

    @Override
    public ClassType instantiate(Position pos, PClass<Formal, Actual> base,
            List<? extends Actual> actuals) throws SemanticException {
        checkInstantiation(pos, base, actuals);
        return uncheckedInstantiate(pos, base, actuals);
    }

    /**
     * Check that an instantiation of a parametric type on a list of actual
     * parameters is legal.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     * @param actuals The list of actuals
     *
     * @throws SemanticException when the actuals do not agree with the formals
     */
    protected void checkInstantiation(Position pos,
            PClass<Formal, Actual> base, List<? extends Actual> actuals)
            throws SemanticException {
        if (base.formals().size() != actuals.size()) {
            throw new SemanticException("Wrong number of actual parameters "
                    + "for instantiation of \"" + base.clazz() + "\".", pos);
        }
    }

    /**
     * Instantiate a parametric type on a list of actual parameters, but
     * do not check that the instantiation is legal.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     * @param actuals The list of actuals
     */
    protected ClassType uncheckedInstantiate(Position pos,
            PClass<Formal, Actual> base, List<? extends Actual> actuals) {
        Map<Formal, Actual> substMap = new HashMap<>();
        Iterator<Formal> i = base.formals().iterator();
        Iterator<? extends Actual> j = actuals.iterator();

        while (i.hasNext() && j.hasNext()) {
            Formal formal = i.next();
            Actual actual = j.next();
            substMap.put(formal, actual);
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("Wrong number of actual "
                                                    + "parameters for instantiation "
                                                    + "of \"" + base + "\".",
                                            pos);
        }

        Type inst = subst(base.clazz(), substMap);
        if (!inst.isClass()) {
            throw new InternalCompilerError("Instantiating a PClass "
                    + "produced something other than a ClassType.", pos);
        }

        return inst.toClass();
    }

    @Override
    public Type subst(Type t, Map<Formal, ? extends Actual> substMap) {
        return subst(substMap).substType(t);
    }

    @Override
    public final Subst<Formal, Actual> subst(
            Map<Formal, ? extends Actual> substMap) {
        Subst<Formal, Actual> subst = substCache.get(substMap);
        if (subst == null) {
            subst = substImpl(substMap);
            substCache.put(substMap, subst);
        }
        return subst;
    }

    protected Subst<Formal, Actual> substImpl(
            Map<Formal, ? extends Actual> substMap) {
        return new Subst_c<>(this, substMap);
    }
}
