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
import java.util.Set;

import polyglot.types.ClassType;
import polyglot.types.Resolver;
import polyglot.types.Type;

public interface JL5ClassType extends ClassType {

    /**
     * Is this class a Raw Class? See JLS 3rd ed., 4.8 
     */
    boolean isRawClass();

    EnumInstance enumConstantNamed(String name);

    List<EnumInstance> enumConstants();

    AnnotationTypeElemInstance annotationElemNamed(String name);

    List<AnnotationTypeElemInstance> annotationElems();

    /**
     * Return a chain of types that show that this class can be implicitly cast
     * to toType.
     * @param toType
     * @return null if this class cannot be cast to toType. Otherwise, list where the
     *    first element is this and the last element is toType, and each conversion
     *    adds an element to the list.
     */
    LinkedList<Type> isImplicitCastValidChainImpl(Type toType);

    /**
     * Translate the type as it should be if it were the receiver
     * of a field or method call.
     * @param context
     */
    String translateAsReceiver(Resolver resolver);

    /**
     * Annotations on the declaration of this type. For types loaded from
     * a class file, this may contain only the retained annotations.
     */
    Annotations annotations();

    /**
     * All direct superclasses of the class. In JL5, a class may have more than
     * one direct superclass. For those classes that have just one
     * superclass, this will be equivalent to the singleton set containing
     * this.supertype(). Note that the superclasses of a class
     * is not in general the same as the supertypes.
     */
    Set<? extends Type> superclasses();
}
