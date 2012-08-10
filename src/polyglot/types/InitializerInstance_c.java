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
 * A <code>InitializerInstance</code> contains the type information for a
 * static or anonymous initializer.
 */
public class InitializerInstance_c extends TypeObject_c implements
        InitializerInstance {
    protected ClassType container;
    protected Flags flags;

    /** Used for deserializing types. */
    protected InitializerInstance_c() {
    }

    public InitializerInstance_c(TypeSystem ts, Position pos,
            ClassType container, Flags flags) {
        super(ts, pos);
        this.container = container;
        this.flags = flags;
    }

    @Override
    public ReferenceType container() {
        return container;
    }

    @Override
    public InitializerInstance container(ClassType container) {
        if (this.container != container) {
            InitializerInstance_c n = (InitializerInstance_c) copy();
            n.setContainer(container);
            return n;
        }
        return this;
    }

    @Override
    public void setContainer(ReferenceType container) {
        this.container = (ClassType) container;
    }

    @Override
    public Flags flags() {
        return flags;
    }

    @Override
    public InitializerInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            InitializerInstance_c n = (InitializerInstance_c) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    /**
     * @param container The container to set.
     */
    public void setContainer(ClassType container) {
        this.container = container;
    }

    /**
     * @param flags The flags to set.
     */
    @Override
    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    @Override
    public int hashCode() {
        return container.hashCode() + flags.hashCode();
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof InitializerInstance) {
            InitializerInstance i = (InitializerInstance) o;
            return flags.equals(i.flags())
                    && ts.equals(container, i.container());
        }

        return false;
    }

    @Override
    public String toString() {
        return flags.translate() + "initializer";
    }

    @Override
    public boolean isCanonical() {
        return true;
    }
}
