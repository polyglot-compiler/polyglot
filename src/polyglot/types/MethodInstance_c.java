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

package polyglot.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * A {@code MethodInstance} represents the type information for a Java
 * method.
 */
public class MethodInstance_c extends ProcedureInstance_c implements
        MethodInstance {
    private static final long serialVersionUID = SerialVersionUID.generate();

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

        decl = this;
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
        if (name != null && !name.equals(this.name) || name == null
                && name != this.name) {
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
        if (!CollectionUtil.equals(formalTypes, l)) {
            MethodInstance_c n = (MethodInstance_c) copy();
            n.setFormalTypes(l);
            return n;
        }
        return this;
    }

    @Override
    public MethodInstance throwTypes(List<? extends Type> l) {
        if (!CollectionUtil.equals(throwTypes, l)) {
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

    @Override
    public void setName(String name) {
        this.name = name;
    }

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

    @Override
    public final boolean isSameMethod(MethodInstance mi) {
        return ts.isSameMethod(this, mi);
    }

    @Override
    public boolean isSameMethodImpl(MethodInstance mi) {
        return this.name().equals(mi.name()) && hasFormals(mi.formalTypes());
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
        List<MethodInstance> l = new LinkedList<>();
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
        };

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
    @Deprecated
    public final boolean canOverrideImpl(MethodInstance mj) {
        throw new InternalCompilerError("canOverrideImpl(MethodInstance mj) should not be called.");
    }

    @Override
    public boolean canOverrideImpl(MethodInstance mj, boolean quiet)
            throws SemanticException {
        MethodInstance mi = this;
        String overridOrHid = mi.flags().isStatic() ? "hid" : "overrid";

        if (!(mi.name().equals(mj.name()) && mi.hasFormals(mj.formalTypes()))) {
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot " + overridOrHid + "e "
                    + mj.signature() + " in " + mj.container()
                    + "; incompatible parameter types", mi.position());
        }

        if (!ts.typeEquals(mi.returnType(), mj.returnType())) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              "return type " + mi.returnType() + " != "
                                      + mj.returnType());
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot " + overridOrHid + "e "
                    + mj.signature() + " in " + mj.container()
                    + "; attempting to use incompatible return type\n"
                    + "found: " + mi.returnType() + "\n" + "required: "
                    + mj.returnType(), mi.position());
        }

        if (!ts.throwsSubset(mi, mj)) {
            if (Report.should_report(Report.types, 3))
                Report.report(3,
                              mi.throwTypes() + " not subset of "
                                      + mj.throwTypes());
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                                                + mi.container() + " cannot "
                                                + overridOrHid + "e "
                                                + mj.signature() + " in "
                                                + mj.container()
                                                + "; the throw set "
                                                + mi.throwTypes()
                                                + " is not a subset of the "
                                                + overridOrHid
                                                + "den method's throw set "
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
                                                + " cannot "
                                                + overridOrHid
                                                + "e "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; attempting to assign weaker access privileges",
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
                                                + " cannot "
                                                + overridOrHid
                                                + "e "
                                                + mj.signature()
                                                + " in "
                                                + mj.container()
                                                + "; "
                                                + overridOrHid
                                                + "den method is "
                                                + (mj.flags().isStatic()
                                                        ? "" : "not ")
                                                + "static", mi.position());
        }

        if (mi != mj && !mi.equals(mj) && mj.flags().isFinal()) {
            // mi can "override" a final method mj if mi and mj are the same method instance.
            if (Report.should_report(Report.types, 3))
                Report.report(3, mj.flags() + " final");
            if (quiet) return false;
            throw new SemanticException(mi.signature() + " in "
                    + mi.container() + " cannot " + overridOrHid + "e "
                    + mj.signature() + " in " + mj.container() + "; "
                    + overridOrHid + "den method is final", mi.position());
        }

        return true;
    }

    @Override
    public List<? extends MethodInstance> implemented() {
        return ts.implemented(this);
    }

    @Override
    public List<MethodInstance> implementedImpl(ReferenceType rt) {
        // A method m1 may implement another method m2 which is accessible by rt
        // but is not accessible by rt's direct superclass.  This is why we
        // cannot check for accessibility until we have the entire set of method
        // instances from rt's supertypes.  See JLS 2nd Ed. | 8.4.6.
        List<MethodInstance> l = new LinkedList<>();
        for (MethodInstance mi : implementedImplAux(rt)) {
            if (!mi.flags().isPrivate()
                    && ts.isAccessible(mi, mi.container(), rt, false))
                l.add(mi);
        }
        return l;
    }

    protected List<MethodInstance> implementedImplAux(ReferenceType rt) {
        if (rt == null) {
            return Collections.<MethodInstance> emptyList();
        }

        List<MethodInstance> l = new LinkedList<>();
        l.addAll(rt.methods(name, formalTypes));

        Type superType = rt.superType();
        if (superType != null) {
            l.addAll(implementedImplAux(superType.toReference()));
        }

        List<? extends ReferenceType> ints = rt.interfaces();
        for (ReferenceType rt2 : ints) {
            l.addAll(implementedImplAux(rt2));
        }

        return l;
    }
}
