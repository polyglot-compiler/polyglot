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

import polyglot.util.Position;

/**
 * A <code>LocalInstance</code> contains type information for a local variable.
 */
public class LocalInstance_c extends VarInstance_c implements LocalInstance {
    /** Used for deserializing types. */
    protected LocalInstance_c() {
    }

    public LocalInstance_c(TypeSystem ts, Position pos, Flags flags, Type type,
            String name) {
        super(ts, pos, flags, type, name);
    }

    @Override
    public LocalInstance orig() {
        return (LocalInstance) declaration();
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof LocalInstance) {
            LocalInstance i = (LocalInstance) o;
            return super.equalsImpl(i);
        }

        return false;
    }

    @Override
    public String toString() {
        return "local " + flags.translate() + type + " " + name
                + (constantValue != null ? (" = " + constantValue) : "");
    }

    @Override
    public boolean isCanonical() {
        return type.isCanonical();
    }

    @Override
    public LocalInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            LocalInstance n = (LocalInstance) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    @Override
    public LocalInstance name(String name) {
        if ((name != null && !name.equals(this.name))
                || (name == null && this.name != null)) {
            LocalInstance n = (LocalInstance) copy();
            n.setName(name);
            return n;
        }
        return this;
    }

    @Override
    public LocalInstance type(Type type) {
        if (this.type != type) {
            LocalInstance n = (LocalInstance) copy();
            n.setType(type);
            return n;
        }
        return this;
    }

    @Override
    public LocalInstance constantValue(Object constantValue) {
        if (!constantValueSet
                || (constantValue != null && !constantValue.equals(this.constantValue))
                || (constantValue == null && this.constantValue != null)) {
            LocalInstance copy = (LocalInstance) this.copy();
            copy.setConstantValue(constantValue);
            return copy;
        }
        return this;
    }

    @Override
    public LocalInstance notConstant() {
        if (!this.constantValueSet || this.isConstant) {
            LocalInstance copy = (LocalInstance) this.copy();
            copy.setNotConstant();
            return copy;
        }
        return this;
    }

}
