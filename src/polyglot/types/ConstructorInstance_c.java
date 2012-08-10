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

import java.util.List;

import polyglot.util.CollectionUtil;
import polyglot.util.Position;

/**
 * A <code>ConstructorInstance</code> contains type information for a
 * constructor.
 */
public class ConstructorInstance_c extends ProcedureInstance_c implements
        ConstructorInstance {
    /** Used for deserializing types. */
    protected ConstructorInstance_c() {
    }

    public ConstructorInstance_c(TypeSystem ts, Position pos,
            ClassType container, Flags flags, List<? extends Type> formalTypes,
            List<? extends Type> excTypes) {
        super(ts, pos, container, flags, formalTypes, excTypes);
        this.decl = this;
    }

    protected ConstructorInstance decl;

    @Override
    public Declaration declaration() {
        return decl;
    }

    @Override
    public void setDeclaration(Declaration decl) {
        this.decl = (ConstructorInstance) decl;
    }

    @Override
    public ConstructorInstance orig() {
        return (ConstructorInstance) declaration();
    }

    @Override
    public ConstructorInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    @Override
    public ConstructorInstance formalTypes(List<? extends Type> l) {
        if (!CollectionUtil.equals(this.formalTypes, l)) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setFormalTypes(l);
            return n;
        }
        return this;
    }

    @Override
    public ConstructorInstance throwTypes(List<? extends Type> l) {
        if (!CollectionUtil.equals(this.throwTypes, l)) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setThrowTypes(l);
            return n;
        }
        return this;
    }

    @Override
    public ConstructorInstance container(ClassType container) {
        if (this.container != container) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setContainer(container);
            return n;
        }
        return this;
    }

    @Override
    public String toString() {
        return designator() + " " + flags.translate() + signature();
    }

    @Override
    public String signature() {
        return container + "(" + TypeSystem_c.listToString(formalTypes) + ")";
    }

    @Override
    public String designator() {
        return "constructor";
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof ConstructorInstance) {
            ConstructorInstance i = (ConstructorInstance) o;
            return ts.equals(container, i.container()) && super.equalsImpl(i);
        }

        return false;
    }

    @Override
    public boolean isCanonical() {
        return container.isCanonical() && listIsCanonical(formalTypes)
                && listIsCanonical(throwTypes);
    }
}
