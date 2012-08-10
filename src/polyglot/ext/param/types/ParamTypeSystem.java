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
import java.util.Map;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.Position;

/**
 * Type system for parameterized types.
 */
public interface ParamTypeSystem<Formal extends Param, Actual extends TypeObject>
        extends TypeSystem {
    /**
     * Create a new mutable PClass.
     *
     * @param pos The position of the pclass
     */
    MuPClass<Formal, Actual> mutablePClass(Position pos);

    /**
     * Instantiate a parametric type on a list of actual parameters.
     *
     * @param pos The position of the instantiated type
     * @param base The parameterized type
     * @param actuals The list of actuals
     *
     * @throws SemanticException when the actuals do not agree with the formals
     */
    ClassType instantiate(Position pos, PClass<Formal, Actual> base,
            List<? extends Actual> actuals) throws SemanticException;

    /**
     * Apply a parameter substitution to a type.
     *
     * @param base The type on which we perform substitutions.
     * @param substMap Map from formal parameters to actuals; the formals are
     * not necessarily formals of <code>base</code>.
     */
    Type subst(Type base, Map<Formal, ? extends Actual> substMap);

    /**
     * Create a substitutor.
     *
     * @param substMap Map from formal parameters to actuals; the formals are
     * not necessarily formals of <code>base</code>.
     * @param cache Cache of substitutions performed, implemented as a map from
     * base type to substituted type.  This is passed in to ensure pointers to
     * outer classes are substituted correctly.
     */
    Subst<Formal, Actual> subst(Map<Formal, ? extends Actual> substMap);
}
