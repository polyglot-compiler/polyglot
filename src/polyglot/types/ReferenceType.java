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

import java.util.List;

/**
 * A {@code ReferenceType} represents a reference type: a type that
 * contains methods and fields and is a subtype of Object. Both class
 * types and array types are reference types.
 */
public interface ReferenceType extends Type {
    /** 
     * Returns the supertype of this type.  For every class except Object,
     * this is non-null.
     */
    Type superType();

    /**
     * Returns a list of all the type's interfaces.
     * @return A list of {@code Type}.
     * @see polyglot.types.Type
     */
    List<? extends ReferenceType> interfaces();

    /**
     * Return a list of all the type's members.
     * @return A list of {@code MemberInstance}.
     * @see polyglot.types.MemberInstance
     */
    List<? extends MemberInstance> members();

    /**
     * Returns a list of fields declared in this type.
     * It does not return fields declared in supertypes.
     * @return A list of {@code FieldInstance}.
     * @see polyglot.types.FieldInstance
     */
    List<? extends FieldInstance> fields();

    /**
     * Returns a list of methods declared in this type.
     * It does not return methods declared in supertypes.
     * @return A list of {@code MethodInstance}.
     * @see polyglot.types.MethodInstance
     */
    List<? extends MethodInstance> methods();

    /**
     * Return the field named {@code name}, or null.
     */
    FieldInstance fieldNamed(String name);

    /**
     * Return the methods named {@code name}, if any.
     * @param name Name of the method to search for.
     * @return A list of {@code MethodInstance}.
     * @see polyglot.types.MethodInstance
     */
    List<? extends MethodInstance> methodsNamed(String name);

    /**
     * Return the methods named {@code name} with the given formal
     * parameter types, if any.
     * @param name Name of the method to search for.
     * @param argTypes A list of {@code Type}.
     * @return A list of {@code MethodInstance}.
     * @see polyglot.types.Type
     * @see polyglot.types.MethodInstance
     */
    List<? extends MethodInstance> methods(String name,
            List<? extends Type> argTypes);

    /**
     * Return true if the type has the given method.
     */
    boolean hasMethod(MethodInstance mi);

    /**
     * Return true if the type has the given method.
     */
    boolean hasMethodImpl(MethodInstance mi);
}
