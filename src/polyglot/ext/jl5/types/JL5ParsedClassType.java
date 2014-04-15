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

import java.util.List;

import polyglot.ext.param.types.PClass;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.util.CodeWriter;

/*
 * A JL5ParsedClassType represents a class with uninstantiated
 * type parameters. In some ways it corresponds to a raw type.
 */
public interface JL5ParsedClassType extends ParsedClassType, JL5ClassType {
    /**
     * Get the pclass for this class. The pclass is used by the param type system to keep
     * track of instantiated types.
     */
    PClass<TypeVariable, ReferenceType> pclass();

    void setPClass(PClass<TypeVariable, ReferenceType> pc);

    void setTypeVariables(List<TypeVariable> typeVars);

    List<TypeVariable> typeVariables();

    void addEnumConstant(EnumInstance ei);

    @Override
    List<EnumInstance> enumConstants();

    @Override
    EnumInstance enumConstantNamed(String name);

    // find methods with compatible name and formals as the given one
    List<? extends JL5MethodInstance> methods(JL5MethodInstance mi);

    //    boolean wasGeneric();

    /**
     * Have the annotations for this class (and for declarations within this class) been resolved?
     */
    boolean annotationsResolved();

    void setAnnotationsResolved(boolean annotationsResolved);

    /**
     * Returns a subst suitable for the erased type: the subst
     * maps any type variables to their erasure. Will return null
     * if the substitution is empty.
     * @return
     */
    JL5Subst erasureSubst();

    void printNoParams(CodeWriter w);

    String toStringNoParams();

    /**
     * Add an AnnotationElemInstance. Should only be used if the
     * ClassType is an Annotation type (i.e., declared using "@interface")
     */
    void addAnnotationElem(AnnotationTypeElemInstance ai);

    void setAnnotations(Annotations annotations);

    boolean enumValueOfMethodNeeded();

    boolean enumValuesMethodNeeded();

}
