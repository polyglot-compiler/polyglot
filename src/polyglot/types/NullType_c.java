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

package polyglot.types;

import polyglot.util.InternalCompilerError;

/**
 * A <code>NullType</code> represents the type of the Java keyword
 * <code>null</code>.
 */
public class NullType_c extends Type_c implements NullType {
    /** Used for deserializing types. */
    protected NullType_c() {
    }

    public NullType_c(TypeSystem ts) {
        super(ts);
    }

    @Override
    public String translate(Resolver c) {
        throw new InternalCompilerError("Cannot translate a null type.");
    }

    @Override
    public String toString() {
        return "type(null)";
    }

    @Override
    public boolean equalsImpl(TypeObject t) {
        return t instanceof NullType;
    }

    @Override
    public int hashCode() {
        return 6060842;
    }

    @Override
    public boolean isCanonical() {
        return true;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public NullType toNull() {
        return this;
    }

    @Override
    public boolean descendsFromImpl(Type ancestor) {
        if (ancestor.isNull()) return false;
        if (ancestor.isReference()) return true;
        return false;
    }

    @Override
    public boolean isImplicitCastValidImpl(Type toType) {
        return toType.isNull() || toType.isReference();
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from this to toType is valid; in other
     * words, some non-null members of this are also members of toType.
     **/
    @Override
    public boolean isCastValidImpl(Type toType) {
        return toType.isNull() || toType.isReference();
    }
}
