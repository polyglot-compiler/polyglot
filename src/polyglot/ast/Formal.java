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

import polyglot.types.Flags;
import polyglot.types.LocalInstance;

/**
 * A {@code Formal} represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public interface Formal extends VarDecl {
    /** Get the flags of the formal. */
    @Override
    Flags flags();

    /** Set the flags of the formal. */
    Formal flags(Flags flags);

    /** Get the type node of the formal. */
    @Override
    TypeNode type();

    /** Set the type node of the formal. */
    Formal type(TypeNode type);

    /** Get the name of the formal. */
    @Override
    Id id();

    /** Set the name of the formal. */
    Formal id(Id name);

    /** Get the name of the formal. */
    @Override
    String name();

    /** Set the name of the formal. */
    Formal name(String name);

    /** Get the local instance of the formal. */
    @Override
    LocalInstance localInstance();

    /** Set the local instance of the formal. */
    Formal localInstance(LocalInstance li);
}
