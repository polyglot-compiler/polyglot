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
import java.util.LinkedList;
import java.util.List;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;

/**
 * A <code>MethodInstance</code> represents the type information for a Java
 * method.
 */
public class MethodInstance_c extends ProcedureInstance_c implements
        MethodInstance {
    protected String name;
    protected Type returnType;

    /** Used for deserializing types. */
    protected MethodInstance_c() {
    }

    public MethodInstance_c(TypeSystem ts, Position pos,
            ReferenceType container, Flags flags, Type returnType, String name,
            List<? extends Type> formalTypes, List<? extends Type> excTypes) {
        super(ts, pos, container, flags, formalTypes, excTypes);
        this.returnType = returnType;
        this.name = name;

        this.decl = this;
    }

    protected MethodInstance decl;

    @Override
    public Declaration declaration() {
        return decl;
    }

    @Override
    public void setDeclaration(Declaration decl) {
        this.decl = (MethodInstance) decl;
    }

    @Override
    public MethodInstance orig() {
        return (MethodInstance) declaration();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Type returnType() {
        return returnType;
    }

    @Override
    public MethodInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    @Override
    public MethodInstance name(String name) {
        if ((name != null && !name.equals(this.name))
                || (name == null && name != this.name)) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setName(name);
            return n;
        }
        return this;
    }

    @Override
    public MethodInstance returnType(Type returnType) {
        if (this.returnType != returnType) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setReturnType(returnType);
            return n;
        }
        return this;
    }

    @Override
    public MethodInstance formalTypes(List<? extends Type> l) {
        if (!CollectionUtil.equals(this.formalTypes, l)) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setFormalTypes(l);
            return n;
        }
        return this;
    }

    @Override
    public MethodInstance throwTypes(List<? extends Type> l) {
        if (!CollectionUtil.equals(this.throwTypes, l)) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setThrowTypes(l);
            return n;
        }
        return this;
    }

    @Override
    public MethodInstance container(ReferenceType container) {
        if (this.container != container) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setContainer(container);
            return n;
        }
        return this;
    }

    /**
     * @param name The name to set.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param returnType The returnType to set.
     */
    @Override
    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public int hashCode() {
        //return container.hashCode() + flags.hashCode() +
        //       returnType.hashCode() + name.hashCode();
        return flags.hashCode() + name.hashCode();
    }

    @Override
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof MethodInstance) {
            MethodInstance i = (MethodInstance) o;
            return ts.equals(returnType, i.returnType())
                    && name.equals(i.name())
                    && ts.equals(container, i.container())
                    && super.equalsImpl(i);
        }

        return false;
    }

    @Override
    public String toString() {
        String s =
                designator() + " " + flags.translate() + returnType + " "
                        + container() + "." + signature();

        if (!throwTypes.isEmpty()) {
            s += " throws " + TypeSystem_c.listToString(throwTypes);
        }

        return s;
    }

    @Override
    public String signature() {
        return name + "(" + TypeSystem_c.listToString(formalTypes) + ")";
    }

    @Override
    public String designator() {
        return "method";
    }

    /** Returns true iff <this> is the same method as <m> */
    @Override
    public final boolean isSameMethod(MethodInstance m) {
        return ts.isSameMethod(this, m);
    }

    /** Returns true iff <this> is the same method as <m> */
    @Override
    public boolean isSameMethodImpl(MethodInstance m) {
        return this.name().equals(m.name()) && hasFormals(m.formalTypes());
    }

    @Override
    public boolean isCanonical() {
        return container.isCanonical() && returnType.isCanonical()
                && listIsCanonical(formalTypes) && listIsCanonical(throwTypes);
    }

    @Override
    public final boolean methodCallValid(String name,
            List<? extends Type> argTypes) {
        return ts.methodCallValid(this, name, argTypes);
    }

    @Override
    public boolean methodCallValidImpl(String name,
            List<? extends Type> argTypes) {
        return name().equals(name) && ts.callValid(this, argTypes);
    }

    @Override
    public List<MethodInstance> overrides() {
        return ts.overrides(this);
    }

    @Override
    public List<MethodInstance> overridesImpl() {
        List<MethodInstance> l = new LinkedList<MethodInstance>();
        ReferenceType rt = container();

        while (rt != null) {
            // add any method with the same name and formalTypes from 
            // rt
            l.addAll(rt.methods(name, formalTypes));

            ReferenceType sup = null;
            if (rt.superType() != null && rt.superType().isReference()) {
                sup = (ReferenceType) rt.superType();
            }

            rt = sup;
        }
        ;

        return l;
    }

    @Override
    public final boolean canOverride(MethodInstance mj) {
        return ts.canOverride(this, mj);
    }

    @Override
    public final void checkOverride(MethodInstance mj) throws SemanticException {
        ts.checkOverride(this, mj);
    }

    /**
     * Leave this method in for historic reasons, to make sure that extensions
     * modify their code correctly.
     */
    public final boolean canOverrideImpl(MethodInstance mj) {
        throw new RuntimeException("canOverrideImpl(MethodInstance mj) should not be called.");
    }

    /**
     * @param quiet If true, then no Semantic Exceptions will be thrown, and the
     *              return value will be true or false. Otherwise, if the method
     *              cannot override, then a SemanticException will be thrown, else
     *              the method will return true.
     */
    @Override
    public boolean canOverrideImpl(MethodInstance mj, boolean quiet)
            throws SemanticException {
        MethodInstance mi = this;

        if (!(mi.name().equals(mj.name()) && mi.hasFormals(mj.formalTypes()))) {
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot override " + mj.signature()
                    + " in " + mj.container() + "; incompatible "
                    + "parameter types", mi.position());
        }

        // HACK: Java5 allows return types to be covariant.  We'll allow covariant
        // return if we mj is defined in a class file.
        boolean allowCovariantReturn = false;

        if (mj.container() instanceof ParsedClassType) {
            ParsedClassType ct = (ParsedClassType) mj.container();
            if (ct.initializer() instanceof LazyClassInitializer) {
                LazyClassInitializer init =
                        (LazyClassInitializer) ct.initializer();
                if (init.fromClassFile()) {
                    allowCovariantReturn = true;
                }
            }
        }

        if ((allowCovariantReturn && !ts.isSubtype(mi.returnType(),
                                                   mj.returnType()))
                || (!allowCovariantReturn && !ts.typeEquals(mi.returnType(),
                                                            mj.returnType()))) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              "return type " + mi.returnType() + " != "
                                      + mj.returnType());
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot override " + mj.signature()
                    + " in " + mj.container()
                    + "; attempting to use incompatible " + "return type\n"
                    + "found: " + mi.returnType() + "\n" + "required: "
                    + mj.returnType(), mi.position());
        }

        if (!ts.throwsSubset(mi, mj)) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              mi.throwTypes() + " not subset of "
                                      + mj.throwTypes());
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; the throw set "
                                                + mi.throwTypes()
                                                + " is not a subset of the "
                                                + "overridden method's throw set "
                                                + mj.throwTypes() + ".",
                                        mi.position());
        }

        if (mi.flags().moreRestrictiveThan(mj.flags())) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              mi.flags() + " more restrictive than "
                                      + mj.flags());
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; attempting to assign weaker "
                                                + "access privileges",
                                        mi.position());
        }

        if (mi.flags().isStatic() != mj.flags().isStatic()) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, mi.signature() + " is "
                        + (mi.flags().isStatic() ? "" : "not") + " static but "
                        + mj.signature() + " is "
                        + (mj.flags().isStatic() ? "" : "not") + " static");
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; overridden method is "
                                                + (mj.flags().isStatic() ? ""
                                                        : "not") + "static",
                                        mi.position());
        }

        if (mi != mj && !mi.equals(mj) && mj.flags().isFinal()) {
            // mi can "override" a final method mj if mi and mj are the same method instance.
            if (Report.should_report(Report.types, 3))
                Report.report(3, mj.flags() + " final");
            if (quiet) return false;
            throw new SemanticException(mi.signature()
                                                + " in "
                                                + mi.container()
                                                + " cannot override "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; overridden method is final",
                                        mi.position());
        }

        return true;
    }

    @Override
    public List<? extends MethodInstance> implemented() {
        return ts.implemented(this);
    }

    @Override
    public List<MethodInstance> implementedImpl(ReferenceType rt) {
        if (rt == null) {
            return Collections.<MethodInstance> emptyList();
        }

        List<MethodInstance> l = new LinkedList<MethodInstance>();
        l.addAll(rt.methods(name, formalTypes));

        Type superType = rt.superType();
        if (superType != null) {
            l.addAll(implementedImpl(superType.toReference()));
        }

        List<? extends ReferenceType> ints = rt.interfaces();
        for (ReferenceType rt2 : ints) {
            l.addAll(implementedImpl(rt2));
        }

        return l;
    }
}
