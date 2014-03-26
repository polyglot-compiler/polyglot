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
 * A {@code ProcedureInstance} contains the type information for a Java
 * procedure (either a method or a constructor).
 */
public interface ProcedureInstance extends CodeInstance {
    /**
     * List of formal parameter types.
     * @return A list of {@code Type}.
     * @see polyglot.types.Type
     */
    List<? extends Type> formalTypes();

    /**
     * @param formalTypes The formalTypes to set.
     */
    void setFormalTypes(List<? extends Type> l);

    /**
     * List of declared exception types thrown.
     * @return A list of {@code Type}.
     * @see polyglot.types.Type
     */
    List<? extends Type> throwTypes();

    /**
     * @param throwTypes The throwTypes to set.
     */
    void setThrowTypes(List<? extends Type> l);

    /**
     * Returns a String representing the signature of the procedure.
     * This includes just the name of the method (or name of the class, if
     * it is a constructor), and the argument types.
     */
    String signature();

    /**
     * String describing the kind of procedure, (e.g., "method" or "constructor").
     */
    String designator();

    /**
     * Return true if {@code this} is more specific than {@code pi}
     * in terms of method overloading.
     */
    boolean moreSpecific(ProcedureInstance pi);

    /**
     * Returns true if the procedure has the given formal parameter types.
     */
    boolean hasFormals(List<? extends Type> arguments);

    /**
     * Returns true if the procedure throws a subset of the exceptions
     * thrown by {@code pi}.
     */
    boolean throwsSubset(ProcedureInstance pi);

    /**
     * Returns true if the procedure can be called with the given argument types.
     */
    boolean callValid(List<? extends Type> actualTypes);

    /**
     * Return true if {@code this} is more specific than {@code pi}
     * in terms of method overloading.
     */
    boolean moreSpecificImpl(ProcedureInstance pi);

    /**
     * Returns true if the procedure has the given formal parameter types.
     */
    boolean hasFormalsImpl(List<? extends Type> arguments);

    /**
     * Returns true if the procedure throws a subset of the exceptions
     * thrown by {@code pi}.
     */
    boolean throwsSubsetImpl(ProcedureInstance pi);

    /**
     * Returns true if the procedure can be called with the given argument types.
     */
    boolean callValidImpl(List<? extends Type> actualTypes);
}
