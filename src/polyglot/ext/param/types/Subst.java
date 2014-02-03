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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.Type;
import polyglot.types.TypeObject;

/**
 * Utility class that performs substitutions on type objects.
 */
public interface Subst<Formal extends Param, Actual extends TypeObject> extends
        Serializable {
    /**
     * Entries of the underlying substitution map.
     * @return An {@code Iterator} of {@code Map.Entry}.
     */
    public Iterator<Entry<Formal, Actual>> entries();

    /** An Iterable that calls entries(). */
    public Iterable<Entry<Formal, Actual>> is_entry();

    /** Type system */
    public ParamTypeSystem<Formal, Actual> typeSystem();

    /** The map of formals to actuals. */
    public Map<Formal, Actual> substitutions();

    /** Perform substitutions on a type. */
    public Type substType(Type t);

    /** Perform substitutions on a PClass. */
    public PClass<Formal, Actual> substPClass(PClass<Formal, Actual> pc);

    /** Perform substitutions on a field. */
    public <T extends FieldInstance> T substField(T fi);

    /** Perform substitutions on a method. */
    public <T extends MethodInstance> T substMethod(T mi);

    /** Perform substitutions on a constructor. */
    public <T extends ConstructorInstance> T substConstructor(T ci);

    /** Perform substitutions on a list of {@code Type}s. */
    public <T extends Type> List<T> substTypeList(List<? extends Type> list);

    /** Perform substitutions on a list of {@code MethodInstance}s. */
    public <T extends MethodInstance> List<T> substMethodList(List<T> list);

    /** Perform substitutions on a list of {@code ConstructorInstance}s. */
    public <T extends ConstructorInstance> List<T> substConstructorList(
            List<T> list);

    /** Perform substitutions on a list of {@code FieldInstance}s. */
    public <T extends FieldInstance> List<T> substFieldList(List<T> list);
}
