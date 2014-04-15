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

import polyglot.ext.param.types.Param;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;

public interface TypeVariable extends ReferenceType, Param {

    public static enum TVarDecl {
        CLASS_TYPE_VARIABLE, PROCEDURE_TYPE_VARIABLE, SYNTHETIC_TYPE_VARIABLE
    }

    TVarDecl declaredIn();

    void setSyntheticOrigin();

    void setDeclaringProcedure(JL5ProcedureInstance pi);

    void setDeclaringClass(ClassType ct);

    ClassType declaringClass();

    JL5ProcedureInstance declaringProcedure();

//	void setBounds(List<ReferenceType> newBounds);
//	List<ReferenceType> bounds();
    ReferenceType erasureType();

//	/**
//	 * Non-destructive update of the bounds list
//	 */
//    Type bounds(List<ReferenceType> newbounds);

    /**
     * Does this type variable have a lower bound? See JLS 3rd ed 4.10.2 and 5.1.10
     * @return
     */
    boolean hasLowerBound();

    ReferenceType upperBound();

    ReferenceType lowerBound();

    void setUpperBound(ReferenceType upperBound);

    void setLowerBound(ReferenceType lowerBound);

    TypeVariable upperBound(ReferenceType upperBound);
}
