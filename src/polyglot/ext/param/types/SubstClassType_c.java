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

package polyglot.ext.param.types;

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
import polyglot.util.Position;

/**
 * Implementation of a ClassType that performs substitutions using a
 * map.  Subclasses must define how the substititions are performed and
 * how to cache substituted types.
 */
public class SubstClassType_c<Formal extends Param, Actual extends TypeObject>
        extends ClassType_c implements SubstType<Formal, Actual> {
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

    /**
     * Entries of the underlying substitution object.
     * @return an <code>Iterator</code> of <code>Map.Entry</code>.
     */
    @Override
    public Iterator<Entry<Formal, Actual>> entries() {
        return subst.entries();
    }

    /** Get the class on that we are performing substitutions. */
    @Override
    public Type base() {
        return base;
    }

    /** The substitution object. */
    @Override
    public Subst<Formal, Actual> subst() {
        return subst;
    }

    ////////////////////////////////////////////////////////////////
    // Perform substitutions on these operations of the base class

    /** Get the class's super type. */
    @Override
    public Type superType() {
        return subst.substType(base.superType());
    }

    /** Get the class's interfaces. */
    @Override
    public List<? extends ReferenceType> interfaces() {
        return subst.substTypeList(base.interfaces());
    }

    /** Get the class's fields. */
    @Override
    public List<? extends FieldInstance> fields() {
        return subst.substFieldList(base.fields());
    }

    /** Get the class's methods. */
    @Override
    public List<? extends MethodInstance> methods() {
        return subst.substMethodList(base.methods());
    }

    /** Get the class's constructors. */
    @Override
    public List<? extends ConstructorInstance> constructors() {
        return subst.substConstructorList(base.constructors());
    }

    /** Get the class's member classes. */
    @Override
    public List<? extends ClassType> memberClasses() {
        return subst.substTypeList(base.memberClasses());
    }

    /** Get the class's outer class, if a nested class. */
    @Override
    public ClassType outer() {
        return (ClassType) subst.substType(base.outer());
    }

    ////////////////////////////////////////////////////////////////
    // Delegate the rest of the class operations to the base class

    /** Get the class's kind: top-level, member, local, or anonymous. */
    @Override
    public ClassType.Kind kind() {
        return base.kind();
    }

    /** Get whether the class was declared in a static context */
    @Override
    public boolean inStaticContext() {
        return base.inStaticContext();
    }

    /** Get the class's full name, if possible. */
    @Override
    public String fullName() {
        return base.fullName();
    }

    /** Get the class's short name, if possible. */
    @Override
    public String name() {
        return base.name();
    }

    /** Get the class's package, if possible. */
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

    /** Type equality test. */
    @Override
    public boolean typeEqualsImpl(Type t) {
        if (t instanceof SubstType) {
            @SuppressWarnings("unchecked")
            SubstType<Formal, Actual> x = (SubstType<Formal, Actual>) t;
            return base.typeEquals(x.base()) && subst.equals(x.subst());
        }
        return false;
    }

    /** Type equality test. */
    @Override
    public boolean equalsImpl(TypeObject t) {
        if (t instanceof SubstType) {
            @SuppressWarnings("unchecked")
            SubstType<Formal, Actual> x = (SubstType<Formal, Actual>) t;
            return base.equals(x.base()) && subst.equals(x.subst());
        }
        return false;
    }

    /** Hash code. */
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

    /**
     * 
     */
    @Override
    public void setFlags(Flags flags) {
        throw new UnsupportedOperationException();
    }

    /**
     * 
     */
    @Override
    public void setContainer(ReferenceType container) {
        throw new UnsupportedOperationException();
    }
}
