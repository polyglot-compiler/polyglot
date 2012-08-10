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

import polyglot.frontend.MissingDependencyException;
import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.Goal;
import polyglot.util.Position;

/**
 * A <code>FieldInstance</code> contains type information for a field.
 */
public class FieldInstance_c extends VarInstance_c implements FieldInstance {
    protected ReferenceType container;

    /** Used for deserializing types. */
    protected FieldInstance_c() {
    }

    public FieldInstance_c(TypeSystem ts, Position pos,
            ReferenceType container, Flags flags, Type type, String name) {
        super(ts, pos, flags, type, name);
        this.container = container;
    }

    @Override
    public FieldInstance orig() {
        return (FieldInstance) declaration();
    }

    @Override
    public FieldInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            FieldInstance n = (FieldInstance) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    @Override
    public FieldInstance name(String name) {
        if ((name != null && !name.equals(this.name))
                || (name == null && this.name != null)) {
            FieldInstance n = (FieldInstance) copy();
            n.setName(name);
            return n;
        }
        return this;
    }

    @Override
    public FieldInstance type(Type type) {
        if (this.type != type) {
            FieldInstance n = (FieldInstance) copy();
            n.setType(type);
            return n;
        }
        return this;
    }

    @Override
    public FieldInstance container(ReferenceType container) {
        if (this.container != container) {
            FieldInstance_c n = (FieldInstance_c) copy();
            n.setContainer(container);
            return n;
        }
        return this;
    }

    @Override
    public FieldInstance constantValue(Object constantValue) {
        if (!constantValueSet
                || (constantValue != null && !constantValue.equals(this.constantValue))
                || (constantValue == null && this.constantValue != null)) {
            FieldInstance copy = (FieldInstance) this.copy();
            copy.setConstantValue(constantValue);
            return copy;
        }
        return this;
    }

    @Override
    public FieldInstance notConstant() {
        if (!this.constantValueSet || this.isConstant) {
            FieldInstance copy = (FieldInstance) this.copy();
            copy.setNotConstant();
            return copy;
        }
        return this;
    }

    @Override
    public ReferenceType container() {
        return container;
    }

    @Override
    public boolean isConstant() {
        if (this != orig()) {
            return orig().isConstant();
        }

        if (!constantValueSet) {
            if (this.isCanonical()
                    && (!flags.isFinal() || !type.isPrimitive()
                            && !type.equals(ts.String()))) {
                // Only primitive-typed or String-typed fields can be constant.
                setNotConstant();
                return isConstant;
            }

            Scheduler scheduler = typeSystem().extensionInfo().scheduler();
            Goal g = scheduler.FieldConstantsChecked(this);
            throw new MissingDependencyException(g);
        }

        return isConstant;
    }

    /**
     * @param container The container to set.
     */
    @Override
    public void setContainer(ReferenceType container) {
        this.container = container;
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof FieldInstance) {
            FieldInstance i = (FieldInstance) o;
            return super.equalsImpl(i) && ts.equals(container, i.container());
        }

        return false;
    }

    @Override
    public String toString() {
        Object v = constantValue;
        if (v instanceof String) {
            String s = (String) v;

            if (s.length() > 8) {
                s = s.substring(0, 8) + "...";
            }

            v = "\"" + s + "\"";
        }

        return "field " + flags.translate() + type + " " + container + "."
                + name + (isConstant ? (" = " + v) : "");
    }

    @Override
    public boolean isCanonical() {
        return container.isCanonical() && type.isCanonical();
    }
}
