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

package polyglot.ast;

import java.util.List;

import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;

/**
 * A <code>ConstructorDecl</code> is an immutable representation of a
 * constructor declaration as part of a class body. 
 */
public interface ConstructorDecl extends ProcedureDecl {
    /** The constructor's flags. */
    @Override
    Flags flags();

    /** Set the constructor's flags. */
    ConstructorDecl flags(Flags flags);

    /**
     * The constructor's name.  This should be the short name of the
     * containing class.
     */
    Id id();

    /** Set the constructor's name. */
    ConstructorDecl id(Id name);

    /**
     * The constructor's name.  This should be the short name of the
     * containing class.
     */
    @Override
    String name();

    /** Set the constructor's name. */
    ConstructorDecl name(String name);

    /** The constructor's formal parameters.
     * @return A list of {@link polyglot.ast.Formal Formal}.
     */
    @Override
    List<Formal> formals();

    /** Set the constructor's formal parameters.
     * @param formals A list of {@link polyglot.ast.Formal Formal}.
     */
    ConstructorDecl formals(List<Formal> formals);

    /** The constructor's exception throw types.
     * @return A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    @Override
    List<TypeNode> throwTypes();

    /** Set the constructor's exception throw types.
     * @param throwTypes A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    ConstructorDecl throwTypes(List<TypeNode> throwTypes);

    /**
     * The constructor type object.  This field may not be valid until
     * after signature disambiguation.
     */
    ConstructorInstance constructorInstance();

    /** Set the constructor's type object. */
    ConstructorDecl constructorInstance(ConstructorInstance ci);
}
