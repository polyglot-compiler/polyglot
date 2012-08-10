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

import polyglot.types.FieldInstance;

/**
 * A <code>Field</code> is an immutable representation of a Java field
 * access.  It consists of field name and may also have either a 
 * <code>Type</code> or an <code>Expr</code> containing the field being 
 * accessed.
 */
public interface Field extends NamedVariable {
    /**
     * Get the type object for the field.  This field may not be valid until
     * after type checking.
     */
    FieldInstance fieldInstance();

    /** Set the type object for the field. */
    Field fieldInstance(FieldInstance fi);

    /**
     * Get the field's container object or type.  May be null before
     * disambiguation.
     */
    Receiver target();

    /** Set the field's container object or type. */
    Field target(Receiver target);

    /**
     * Returns whether the target of this field is implicit, that is if the
     * target is either "this" or a classname, and the source code did not
     * explicitly provide a target. 
     */
    boolean isTargetImplicit();

    /** 
     * Set whether the target of the field is implicit.
     */
    Field targetImplicit(boolean implicit);

    /** Get the field's name. */
    Id id();

    /** Set the field's name. */
    Field id(Id name);

    /** Get the field's name. */
    @Override
    String name();

    /** Set the field's name. */
    Field name(String name);
}
