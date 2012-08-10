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

import polyglot.types.Flags;
import polyglot.types.LocalInstance;

/**
 * A <code>Formal</code> represents a formal parameter to a method
 * or constructor or to a catch block.  It consists of a type and a variable
 * identifier.
 */
public interface Formal extends VarDecl {
    /** Get the flags of the formal. */
    @Override
    public Flags flags();

    /** Set the flags of the formal. */
    public Formal flags(Flags flags);

    /** Get the type node of the formal. */
    @Override
    public TypeNode type();

    /** Set the type node of the formal. */
    public Formal type(TypeNode type);

    /** Get the name of the formal. */
    @Override
    public Id id();

    /** Set the name of the formal. */
    public Formal id(Id name);

    /** Get the name of the formal. */
    @Override
    public String name();

    /** Set the name of the formal. */
    public Formal name(String name);

    /** Get the local instance of the formal. */
    @Override
    public LocalInstance localInstance();

    /** Set the local instance of the formal. */
    public Formal localInstance(LocalInstance li);
}
