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
import polyglot.types.TypeObject;

/*
 * Mutable parametric classes.  This interface is a wrapper around
 * a ClassType that associates formal parameters with the class.
 * formals can be any type object.
 */
public interface MuPClass<Formal extends Param, Actual extends TypeObject>
        extends PClass<Formal, Actual> {

    /**
     * Destructively update the formals.
     * @param formals The new formals
     */
    void formals(List<Formal> formals);

    /**
     * Destructively add a formal to the end of the formals list.
     * @param param The new formal
     */
    void addFormal(Formal param);

    /**
     * Destructively update the clazz.
     * @param clazz The new clazz
     */
    void clazz(ClassType clazz);
}
