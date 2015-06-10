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

import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.InitializerInstance;
import polyglot.types.Type;

/**
 * A {@code FieldDecl} is an immutable representation of the declaration
 * of a field of a class.
 */
public interface FieldDecl extends ClassMember, VarInit, CodeNode, Documentable {
    /** Get the type object for the declaration's type. */
    Type declType();

    /** Get the declaration's flags. */
    Flags flags();

    /** Set the declaration's flags. */
    FieldDecl flags(Flags flags);

    /** Get the declaration's type. */
    TypeNode type();

    /** Set the declaration's type. */
    FieldDecl type(TypeNode type);

    /** Set the declaration's name. */
    FieldDecl id(Id name);

    /** Get the declaration's name. */
    String name();

    /** Set the declaration's name. */
    FieldDecl name(String name);

    /** Get the declaration's initializer, or null. */
    Expr init();

    /** Set the declaration's initializer. */
    FieldDecl init(Expr init);

    /**
     * Get the type object for the field we are declaring.  This field may
     * not be valid until after signature disambiguation.
     */
    FieldInstance fieldInstance();

    /** Set the type object for the field we are declaring. */
    FieldDecl fieldInstance(FieldInstance fi);

    /**
     * Get the type object for the initializer expression, or null.
     * We evaluate the initializer expression as if it were in an
     * initializer block (e.g., {@code { }} or {@code static { }}).
     */
    InitializerInstance initializerInstance();

    /** Set the type object for the initializer expression. */
    FieldDecl initializerInstance(InitializerInstance fi);
}
