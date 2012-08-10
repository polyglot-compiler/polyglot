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

import polyglot.types.Flags;
import polyglot.types.ParsedClassType;

/**
 * A <code>ClassDecl</code> represents a top-level, member, or local class
 * declaration.
 */
public interface ClassDecl extends Term, TopLevelDecl, ClassMember {
    /**
     * The type of the class declaration.
     */
    ParsedClassType type();

    /**
     * Set the type of the class declaration.
     */
    ClassDecl type(ParsedClassType type);

    /**
     * The class declaration's flags.
     */
    @Override
    Flags flags();

    /**
     * Set the class declaration's flags.
     */
    ClassDecl flags(Flags flags);

    /**
     * The class declaration's name.
     */
    Id id();

    /**
     * Set the class declaration's name.
     */
    ClassDecl id(Id name);

    /**
     * The class declaration's name.
     */
    @Override
    String name();

    /**
     * Set the class declaration's name.
     */
    ClassDecl name(String name);

    /**
     * The class's super class.
     */
    TypeNode superClass();

    /**
     * Set the class's super class.
     */
    ClassDecl superClass(TypeNode superClass);

    /**
     * The class's interface list.
     * @return A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    List<TypeNode> interfaces();

    /**
     * Set the class's interface list.
     * @param interfaces A list of {@link polyglot.ast.TypeNode TypeNode}.
     */
    ClassDecl interfaces(List<TypeNode> interfaces);

    /**
     * The class's body.
     */
    ClassBody body();

    /**
     * Set the class's body.
     */
    ClassDecl body(ClassBody body);
}
