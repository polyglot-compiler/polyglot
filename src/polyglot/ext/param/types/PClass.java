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
import polyglot.types.Importable;
import polyglot.types.SemanticException;
import polyglot.types.TypeObject;
import polyglot.util.Position;

/**
 * Parametric class.  This class is a wrapper around
 * a ClassType that associates formal parameters with the class.
 * formals can be any type object.
 */
public interface PClass<Formal extends Param, Actual extends TypeObject>
        extends Importable {
    /**
     * The formal type parameters associated with <code>this</code>.
     */
    List<Formal> formals();

    /**
     * The class associated with <code>this</code>.  Note that
     * <code>this</code> should never be used as a first-class type.
     */
    ClassType clazz();

    /**
     * Instantiate <code>this</code>.
     * @param pos The position of the instantiation
     * @param actuals The actual type parameters for the instantiation
     */
    ClassType instantiate(Position pos, List<Actual> actuals)
            throws SemanticException;
}
