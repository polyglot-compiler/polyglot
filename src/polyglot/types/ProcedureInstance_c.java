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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SubtypeSet;

/**
 * A <code>ProcedureInstance_c</code> contains the type information for a Java
 * procedure (either a method or a constructor).
 */
public abstract class ProcedureInstance_c extends TypeObject_c implements
        ProcedureInstance {
    protected ReferenceType container;
    protected Flags flags;
    protected List<Type> formalTypes;
    protected List<Type> throwTypes;

    /** Used for deserializing types. */
    protected ProcedureInstance_c() {
    }

    public ProcedureInstance_c(TypeSystem ts, Position pos,
            ReferenceType container, Flags flags,
            List<? extends Type> formalTypes, List<? extends Type> excTypes) {
        super(ts, pos);
        this.container = container;
        this.flags = flags;
        this.formalTypes = ListUtil.copy(formalTypes, true);
        this.throwTypes = ListUtil.copy(excTypes, true);
    }

    public ReferenceType container() {
        return container;
    }

    @Override
    public Flags flags() {
        return flags;
    }

    @Override
    public List<Type> formalTypes() {
        return Collections.unmodifiableList(formalTypes);
    }

    @Override
    public List<Type> throwTypes() {
        return Collections.unmodifiableList(throwTypes);
    }

    /**
     * @param container The container to set.
     */
    public void setContainer(ReferenceType container) {
        this.container = container;
    }

    /**
     * @param flags The flags to set.
     */
    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    /**
     * @param formalTypes The formalTypes to set.
     */
    @Override
    public void setFormalTypes(List<? extends Type> formalTypes) {
        this.formalTypes = ListUtil.copy(formalTypes, true);
    }

    /**
     * @param throwTypes The throwTypes to set.
     */
    @Override
    public void setThrowTypes(List<? extends Type> throwTypes) {
        this.throwTypes = ListUtil.copy(throwTypes, true);
    }

    @Override
    public int hashCode() {
        return container.hashCode() + flags.hashCode();
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof ProcedureInstance) {
            ProcedureInstance i = (ProcedureInstance) o;
            // FIXME: Check excTypes too?
            return flags.equals(i.flags())
                    && ts.hasFormals(this, i.formalTypes());
        }

        return false;
    }

    protected boolean listIsCanonical(List<? extends TypeObject> l) {
        for (TypeObject o : l) {
            if (!o.isCanonical()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final boolean moreSpecific(ProcedureInstance p) {
        return ts.moreSpecific(this, p);
    }

    /**
     * Returns whether <code>this</code> is <i>more specific</i> than
     * <code>p</code>, where <i>more specific</i> is defined as JLS
     * 15.12.2.2.
     *<p>
     * <b>Note:</b> There is a fair amount of guesswork since the JLS
     * does not include any info regarding Java 1.2, so all inner class
     * rules are found empirically using jikes and javac.
     */
    @Override
    public boolean moreSpecificImpl(ProcedureInstance p) {
        ProcedureInstance p1 = this;
        ProcedureInstance p2 = p;

        // rule 1:
        ReferenceType t1 = null;
        ReferenceType t2 = null;

        if (p1 instanceof MemberInstance) {
            if (p1 instanceof Declaration) {
                t1 =
                        ((MemberInstance) ((Declaration) p1).declaration()).container();
            }
            else {
                t1 = ((MemberInstance) p1).container();
            }
        }
        if (p2 instanceof MemberInstance) {
            if (p2 instanceof Declaration) {
                t2 =
                        ((MemberInstance) ((Declaration) p2).declaration()).container();
            }
            else {
                t2 = ((MemberInstance) p2).container();
            }
        }

        if (t1 != null && t2 != null) {
            if (t1.isClass() && t2.isClass()) {
                if (!t1.isSubtype(t2) && !t1.toClass().isEnclosed(t2.toClass())) {
                    return false;
                }
            }
            else {
                if (!t1.isSubtype(t2)) {
                    return false;
                }
            }
        }

        // rule 2:
        return p2.callValid(p1.formalTypes());
    }

    /** Returns true if the procedure has the given formal parameter types. */
    @Override
    public final boolean hasFormals(List<? extends Type> formalTypes) {
        return ts.hasFormals(this, formalTypes);
    }

    /** Returns true if the procedure has the given formal parameter types. */
    @Override
    public boolean hasFormalsImpl(List<? extends Type> formalTypes) {
        List<? extends Type> l1 = this.formalTypes();
        List<? extends Type> l2 = formalTypes;

        Iterator<? extends Type> i1 = l1.iterator();
        Iterator<? extends Type> i2 = l2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            Type t1 = i1.next();
            Type t2 = i2.next();

            if (!ts.equals(t1, t2)) {
                return false;
            }
        }

        return !(i1.hasNext() || i2.hasNext());
    }

    /** Returns true iff <code>this</code> throws fewer exceptions than
     * <code>p</code>. */
    @Override
    public final boolean throwsSubset(ProcedureInstance p) {
        return ts.throwsSubset(this, p);
    }

    /** Returns true iff <code>this</code> throws fewer exceptions than
     * <code>p</code>. */
    @Override
    public boolean throwsSubsetImpl(ProcedureInstance p) {
        SubtypeSet s1 = new SubtypeSet(ts.Throwable());
        SubtypeSet s2 = new SubtypeSet(ts.Throwable());

        s1.addAll(this.throwTypes());
        s2.addAll(p.throwTypes());

        for (Type t : s1) {
            if (!ts.isUncheckedException(t) && !s2.contains(t)) {
                return false;
            }
        }

        return true;
    }

    /** Returns true if a call can be made with the given argument types. */
    @Override
    public final boolean callValid(List<? extends Type> argTypes) {
        return ts.callValid(this, argTypes);
    }

    /** Returns true if a call can be made with the given argument types. */
    @Override
    public boolean callValidImpl(List<? extends Type> argTypes) {
        List<? extends Type> l1 = this.formalTypes();
        List<? extends Type> l2 = argTypes;

        Iterator<? extends Type> i1 = l1.iterator();
        Iterator<? extends Type> i2 = l2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            Type t1 = i1.next();
            Type t2 = i2.next();

            if (!ts.isImplicitCastValid(t2, t1)) {
                return false;
            }
        }

        return !(i1.hasNext() || i2.hasNext());
    }
}
