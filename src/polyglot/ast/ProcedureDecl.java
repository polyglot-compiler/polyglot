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

package polyglot.ast;

import java.util.List;

import polyglot.types.Flags;
import polyglot.types.ProcedureInstance;

/**
 * A procedure declaration.  A procedure is the supertype of methods and
 * constructors.
 */
public interface ProcedureDecl extends CodeDecl, Documentable {
    /** The procedure's flags. */
    Flags flags();

    /** Set the procedure's flags. */
    ProcedureDecl flags(Flags flags);

    /** Set the procedure's name. */
    ProcedureDecl id(Id id);

    /** The procedure's name. */
    String name();

    /** Set the procedure's name. */
    ProcedureDecl name(String name);

    /**
     * The procedure's formal parameters.
     * @return A list of {@link polyglot.ast.Formal Formal}.
     */
    List<Formal> formals();

    /**
     * Set the procedure's formal parameters.
     * @param formals A list of {@link polyglot.ast.Formal Formal}.
     */
    ProcedureDecl formals(List<Formal> formals);

    /**
     * The procedure's exception throw types.
     * @return A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    List<TypeNode> throwTypes();

    /**
     * Set the procedure's exception throw types.
     * @param throwTypes A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    ProcedureDecl throwTypes(List<TypeNode> throwTypes);

    /**
     * The procedure's type object.  This field may not be valid until
     * after signature disambiguation.
     */
    ProcedureInstance procedureInstance();
}
