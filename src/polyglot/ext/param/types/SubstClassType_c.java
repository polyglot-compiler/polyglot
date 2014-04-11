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

package polyglot.ext.param.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ClassType_c;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.MethodInstance;
import polyglot.types.Package;
import polyglot.types.ReferenceType;
import polyglot.types.Resolver;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.util.Copy;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;

/**
 * Implementation of a ClassType that performs substitutions using a
 * map.  Subclasses must define how the substitutions are performed and
 * how to cache substituted types.
 */
public class SubstClassType_c<Formal extends Param, Actual extends TypeObject>
        extends ClassType_c implements SubstType<Formal, Actual> {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected transient List<? extends ReferenceType> interfaces;
    protected transient List<? extends FieldInstance> fields;
    protected transient List<? extends MethodInstance> methods;
    protected transient List<? extends ConstructorInstance> constructors;
    protected transient List<? extends ClassType> memberClasses;
    // substitution caches
    protected transient List<? extends ReferenceType> substInterfaces;
    protected transient List<? extends FieldInstance> substFields;
    protected transient List<? extends MethodInstance> substMethods;
    protected transient List<? extends ConstructorInstance> substConstructors;
    protected transient List<? extends ClassType> substMemberClasses;

    /** The class type we are substituting into. */
    protected ClassType base;

    /** Map from formal parameters (of type Param) to actuals. */
    protected Subst<Formal, Actual> subst;

    public SubstClassType_c(ParamTypeSystem<Formal, Actual> ts, Position pos,
            ClassType base, Subst<Formal, Actual> subst) {
        super(ts, pos);
        this.base = base;
        this.subst = subst;
        if (subst == null) {
            throw new IllegalArgumentException("null subst");
        }
        if (base == null) {
            throw new IllegalArgumentException("null base");
        }
    }

    @Override
    public Iterator<Entry<Formal, Actual>> entries() {
        return subst.entries();
    }

    @Override
    public Type base() {
        return base;
    }

    @Override
    public Subst<Formal, Actual> subst() {
        return subst;
    }

    ////////////////////////////////////////////////////////////////
    // Perform substitutions on these operations of the base class

    @Override
    public Type superType() {
        return subst.substType(base.superType());
    }

    @Override
    public List<? extends ReferenceType> interfaces() {
        List<? extends ReferenceType> interfaces = base.interfaces();
        if (!interfaces.equals(this.interfaces)) {
            this.interfaces = deepCopy(interfaces);
            substInterfaces = subst.substTypeList(interfaces);
        }
        return substInterfaces;
    }

    @Override
    public List<? extends FieldInstance> fields() {
        List<? extends FieldInstance> fields = base.fields();
        if (!fields.equals(this.fields)) {
            this.fields = deepCopy(fields);
            this.substFields = subst.substFieldList(fields);
        }
        return substFields;
    }

    @Override
    public List<? extends MethodInstance> methods() {
        List<? extends MethodInstance> methods = base.methods();
        if (!methods.equals(this.methods)) {
            this.methods = deepCopy(methods);
            substMethods = subst.substMethodList(methods);
        }
        return substMethods;
    }

    @Override
    public List<? extends ConstructorInstance> constructors() {
        List<? extends ConstructorInstance> constructors = base.constructors();
        if (!constructors.equals(this.constructors)) {
            this.constructors = deepCopy(constructors);
            substConstructors = subst.substConstructorList(constructors);
        }
        return substConstructors;
    }

    @Override
    public List<? extends ClassType> memberClasses() {
        List<? extends ClassType> memberClasses = base.memberClasses();
        if (!memberClasses.equals(this.memberClasses)) {
            this.memberClasses = deepCopy(memberClasses);
            substMemberClasses = subst.substTypeList(memberClasses);
        }
        return substMemberClasses;
    }

    @Override
    public ClassType outer() {
        return (ClassType) subst.substType(base.outer());
    }

    protected <T extends TypeObject> List<T> deepCopy(List<T> src) {
        List<T> dst = new ArrayList<>(src.size());
        for (T t : src)
            dst.add(Copy.Util.copy(t));
        return dst;
    }

    ////////////////////////////////////////////////////////////////
    // Delegate the rest of the class operations to the base class

    @Override
    public ClassType.Kind kind() {
        return base.kind();
    }

    @Override
    public boolean inStaticContext() {
        return base.inStaticContext();
    }

    @Override
    public String fullName() {
        return base.fullName();
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public Package package_() {
        return base.package_();
    }

    @Override
    public Flags flags() {
        return base.flags();
    }

    @Override
    public String translate(Resolver c) {
        return base.translate(c);
    }

    ////////////////////////////////////////////////////////////////
    // Equality tests

    @Override
    public boolean typeEqualsImpl(Type t) {
        if (t instanceof SubstType) {
            @SuppressWarnings("unchecked")
            SubstType<Formal, Actual> x = (SubstType<Formal, Actual>) t;
            return base.typeEquals(x.base()) && subst.equals(x.subst());
        }
        return false;
    }

    @Override
    public boolean equalsImpl(TypeObject t) {
        if (t instanceof SubstType) {
            @SuppressWarnings("unchecked")
            SubstType<Formal, Actual> x = (SubstType<Formal, Actual>) t;
            return base.equals(x.base()) && subst.equals(x.subst());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return base.hashCode() ^ subst.hashCode();
    }

    @Override
    public String toString() {
        return base.toString() + subst.toString();
    }

    @Override
    public Job job() {
        return null;
    }

    @Override
    public void setFlags(Flags flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContainer(ReferenceType container) {
        throw new UnsupportedOperationException();
    }
}
