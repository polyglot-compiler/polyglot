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

package polyglot.types;

import java.util.List;

/**
 * A <code>ReferenceType</code> represents a reference type: a type which
 * contains methods and fields and which is a subtype of Object.
 */
public interface ReferenceType extends Type {
    /**
     * Return the type's super type.
     */
    Type superType();

    /**
     * Return the type's interfaces.
     * @return A list of <code>Type</code>.
     * @see polyglot.types.Type
     */
    List<? extends ReferenceType> interfaces();

    /**
     * Return a list of a all the type's members.
     * @return A list of <code>MemberInstance</code>.
     * @see polyglot.types.MemberInstance
     */
    List<? extends MemberInstance> members();

    /**
     * Return the type's fields.
     * @return A list of <code>FieldInstance</code>.
     * @see polyglot.types.FieldInstance
     */
    List<? extends FieldInstance> fields();

    /**
     * Return the type's methods.
     * @return A list of <code>MethodInstance</code>.
     * @see polyglot.types.MethodInstance
     */
    List<? extends MethodInstance> methods();

    /**
     * Return the field named <code>name</code>, or null.
     */
    FieldInstance fieldNamed(String name);

    /**
     * Return the methods named <code>name</code>, if any.
     * @param name Name of the method to search for.
     * @return A list of <code>MethodInstance</code>.
     * @see polyglot.types.MethodInstance
     */
    List<? extends MethodInstance> methodsNamed(String name);

    /**
     * Return the methods named <code>name</code> with the given formal
     * parameter types, if any.
     * @param name Name of the method to search for.
     * @param argTypes A list of <code>Type</code>.
     * @return A list of <code>MethodInstance</code>.
     * @see polyglot.types.Type
     * @see polyglot.types.MethodInstance
     */
    List<? extends MethodInstance> methods(String name,
            List<? extends Type> argTypes);

    /**
     * Return the true if the type has the given method.
     */
    boolean hasMethod(MethodInstance mi);

    /**
     * Return the true if the type has the given method.
     */
    boolean hasMethodImpl(MethodInstance mi);
}
