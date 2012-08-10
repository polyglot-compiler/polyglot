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

import polyglot.types.MethodInstance;
import polyglot.types.Flags;
import java.util.List;

/**
 * A method declaration.
 */
public interface MethodDecl extends ProcedureDecl {
    /** The method's flags. */
    @Override
    Flags flags();

    /** Set the method's flags. */
    MethodDecl flags(Flags flags);

    /** The method's return type.  */
    TypeNode returnType();

    /** Set the method's return type.  */
    MethodDecl returnType(TypeNode returnType);

    /** The method's name. */
    Id id();

    /** Set the method's name. */
    MethodDecl id(Id name);

    /** The method's name. */
    @Override
    String name();

    /** Set the method's name. */
    MethodDecl name(String name);

    /** The method's formal parameters.
     * @return A list of {@link polyglot.ast.Formal Formal}.
     */
    @Override
    List<Formal> formals();

    /** Set the method's formal parameters.
     * @param formals A list of {@link polyglot.ast.Formal Formal}.
     */
    MethodDecl formals(List<Formal> formals);

    /** The method's exception throw types.
     * @return A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    @Override
    List<TypeNode> throwTypes();

    /** Set the method's exception throw types.
     * @param throwTypes A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    MethodDecl throwTypes(List<TypeNode> throwTypes);

    /**
     * The method type object.  This field may not be valid until
     * after signature disambiguation.
     */
    MethodInstance methodInstance();

    /** Set the method's type object. */
    MethodDecl methodInstance(MethodInstance mi);
}
