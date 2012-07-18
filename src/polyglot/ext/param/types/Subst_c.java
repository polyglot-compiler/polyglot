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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import polyglot.ext.param.Topics;
import polyglot.main.Report;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.MethodInstance;
import polyglot.types.ReferenceType;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.util.CachingTransformingList;
import polyglot.util.Transformation;
import polyglot.util.TypeInputStream;

/**
 * Utility class that performs substitutions on type objects using a
 * map.  Subclasses must define how the substititions are performed and
 * how to cache substituted types.
 */
public class Subst_c<Formal extends Param, Actual extends TypeObject> implements Subst<Formal,Actual>
{
    /** Map from formal parameters (of type Param) to actuals. */
    protected Map<Formal, Actual> subst;

    /** Cache of types. From CacheTypeWrapper(t) to subst(t)*/
    protected transient Map<CacheTypeWrapper, Type> cache;

    protected transient ParamTypeSystem<Formal, Actual> ts;

    public Subst_c(ParamTypeSystem<Formal, Actual> ts, Map<Formal, Actual> subst)
    {
        this.ts = ts;
        this.subst = subst;
        this.cache = new HashMap<CacheTypeWrapper, Type>();
    }

    @Override
    public ParamTypeSystem<Formal, Actual> typeSystem() {
        return ts;
    }

    /**
     * Entries of the underlying substitution map.
     * @return an <code>Iterator</code> of <code>Map.Entry</code>.
     */
    @Override
    public Iterator<Entry<Formal, Actual>> entries() {
        return substitutions().entrySet().iterator();
    }

    /**
     * The underlying substitution map.
     */
    @Override
    public Map<Formal, Actual> substitutions() {
        return Collections.unmodifiableMap(subst);
    }

    /** Perform substitutions on a type, without checking the cache. */
    protected Type uncachedSubstType(Type t) {
        if (t.isArray()) {
            ArrayType at = t.toArray();
            return at.base(substType(at.base()));
        }

        // We may have a parameterized type instantiated on the formals.
        if (t instanceof SubstType) {
            @SuppressWarnings("unchecked")
            SubstType<Formal, Actual> substType = (SubstType<Formal, Actual>) t;
            Type tbase = substType.base();
            Map<Formal, Actual> tsubst = substType.subst().substitutions();

            Map<Formal, Actual> newSubst = new HashMap<Formal, Actual>();

            // go through the map, and perform substitution on the actuals
            for (Entry<Formal, Actual> e : tsubst.entrySet()) {
                Formal formal = e.getKey();
                Actual actual = e.getValue();
                
                newSubst.put(formal, substSubstValue(actual));
            }

            return ts.subst(tbase, newSubst);
        }

        if (t instanceof ClassType) {
            return substClassType((ClassType) t);
        }

        return t;
    }


    /**
     * When adding a new substitution A-&gt;B to the map, we need to check if 
     * there are already any existing substitutions, say C-&gt;A, and if so,
     * replace them appropriately, in this case with C-&gt;B.
     * 
     * This method allows subclasses to perform substitution on a value in
     * the substitution map (B in the 
     * example above). Subclasses may need to override this
     * if the keys and values are not the same object.
     */
    protected Actual substSubstValue(Actual value) {
        return value;
    }
    
    /** Perform substitutions on a class type. Substitutions are performed
     * lazily. */
    public ClassType substClassType(ClassType t) {
        return new SubstClassType_c<Formal, Actual>(ts, t.position(), t, this);
    }

    /** Perform substitutions on a type. */
    @Override
    public Type substType(Type t) {
        if (t == null || t == this) // XXX comparison t == this can't succeed! (Findbugs)
            return t;

        Type cached = cacheGet(t);

        if (cached == null) {
            cached = uncachedSubstType(t);
            cachePut(t, cached);

            if (Report.should_report(Topics.subst, 2))
                Report.report(2, "substType(" +
                              t + ": " + t.getClass().getName() + ") = " +
                              cached + ": " + cached.getClass().getName());
        }

        return cached;
    }

    protected void cachePut(Type t, Type cached) {
        cache.put(new CacheTypeWrapper(t), cached);        
    }

    protected Type cacheGet(Type t) {
        return cache.get(new CacheTypeWrapper(t));
    }

    class CacheTypeWrapper {
        final Type t;
        CacheTypeWrapper(Type t) { this.t = t; }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Subst_c.CacheTypeWrapper) {
                @SuppressWarnings("unchecked")
                CacheTypeWrapper wrapper = (CacheTypeWrapper)o;
                return Subst_c.this.cacheTypeEquality(t, wrapper.t);
            }
            if (o instanceof Type) {
                return Subst_c.this.cacheTypeEquality(t, (Type)o);
            }
            return false;
        }
        @Override
        public String toString() {
            return String.valueOf(t);
        }
        @Override
        public int hashCode() {
            return t==null?0:t.hashCode();           
        }
    }
    
    /**
     * This method is used by the cache lookup to test type equality.
     * May be overridden by subclasses as appropriate.
     */
    protected boolean cacheTypeEquality(Type t1, Type t2) {
        return ts.equals(t1, t2);
    }

    /** Perform substitution on a PClass. */
    @Override
    public PClass<Formal, Actual> substPClass(PClass<Formal, Actual> pclazz) {
        MuPClass<Formal, Actual> newPclazz = ts.mutablePClass(pclazz.position());
        newPclazz.formals(pclazz.formals());
        newPclazz.clazz((ClassType) substType(pclazz.clazz()));
        return newPclazz;
    }


    /** Perform substititions on a field. */
    @Override
    public <T extends FieldInstance> T substField(T fi) {
        ReferenceType ct = (ReferenceType) substType(fi.container());
        Type t = substType(fi.type());
        @SuppressWarnings("unchecked")
        T newFI = (T) fi.copy();
        newFI.setType(t);
        newFI.setContainer(ct);
        return newFI;
    }

    /** Perform substititions on a method. */
    @Override
    public <T extends MethodInstance> T substMethod(T mi) {
        ReferenceType ct = (ReferenceType) substType(mi.container());

        Type rt = substType(mi.returnType());

        List<? extends Type> formalTypes = mi.formalTypes();
        formalTypes = substTypeList(formalTypes);

        List<? extends Type> throwTypes = mi.throwTypes();
        throwTypes = substTypeList(throwTypes);

        @SuppressWarnings("unchecked")
        T tmpMi = (T) mi.copy();
        tmpMi.setReturnType(rt);
        tmpMi.setFormalTypes(formalTypes);
        tmpMi.setThrowTypes(throwTypes);
        tmpMi.setContainer(ct);
        
        return tmpMi;
    }

    /** Perform substititions on a constructor. */
    @Override
    public <T extends ConstructorInstance> T substConstructor(T ci) {
        ClassType ct = (ClassType) substType(ci.container());

        List<? extends Type> formalTypes = ci.formalTypes();
        formalTypes = substTypeList(formalTypes);

        List<? extends Type> throwTypes = ci.throwTypes();
        throwTypes = substTypeList(throwTypes);
        

        @SuppressWarnings("unchecked")
        T tmpCi = (T) ci.copy();
        tmpCi.setFormalTypes(formalTypes);
        tmpCi.setThrowTypes(throwTypes);
        tmpCi.setContainer(ct);
        
        return tmpCi;
    }

    /** Perform substititions on a list of <code>Type</code>. */
    @Override
    public <T extends Type> List<T> substTypeList(List<? extends Type> list) {
        return new CachingTransformingList<Type, T>(list, new TypeXform<T>());
    }

    /** Perform substititions on a list of <code>MethodInstance</code>. */
    @Override
    public <T extends MethodInstance> List<T> substMethodList(List<T> list) {
        return new CachingTransformingList<T, T>(list, new MethodXform<T>());
    }

    /** Perform substititions on a list of <code>ConstructorInstance</code>. */
    @Override
    public <T extends ConstructorInstance> List<T> substConstructorList(List<T> list) {
        return new CachingTransformingList<T, T>(list, new ConstructorXform<T>());
    }

    /** Perform substititions on a list of <code>FieldInstance</code>. */
    @Override
    public <T extends FieldInstance> List<T> substFieldList(List<T> list) {
        return new CachingTransformingList<T, T>(list, new FieldXform<T>());
    }

    ////////////////////////////////////////////////////////////////
    // Substitution machinery

    /** Function object for transforming types. */
    public class TypeXform<T extends Type> implements Transformation<Type, T> {
        @Override
        public T transform(Type o) {
            @SuppressWarnings("unchecked")
            T result = (T) substType(o);
            return result;
        }
    }

    /** Function object for transforming fields. */
    public class FieldXform<T extends FieldInstance> implements Transformation<T,T> {
        @Override
        public T transform(T o) {
            return substField(o);
        }
    }

    /** Function object for transforming methods. */
    public class MethodXform<T extends MethodInstance> implements Transformation<T,T> {
        @Override
        public T transform(T o) {
            return substMethod(o);
        }
    }

    /** Function object for transforming constructors. */
    public class ConstructorXform<T extends ConstructorInstance> implements Transformation<T,T> {
        @Override
        public T transform(T o) {
            return substConstructor(o);
        }
    }

    ////////////////////////////////////////////////////////////////
    // Equality

    @Override
    public boolean equals(Object o) {
        if (o instanceof Subst) {
            return subst.equals(((Subst<?,?>) o).substitutions());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return subst.hashCode();
    }

    ////////////////////////////////////////////////////////////////
    // Utility functions

    @Override
    public String toString() {
        String str = "[";
        for (Iterator<Entry<Formal, Actual>> iter = subst.entrySet().iterator(); iter.hasNext(); ) {
            Entry<Formal, Actual> entry = iter.next();
            str += "<" + entry.getKey() + ": " + entry.getValue() + ">";
            if (iter.hasNext())
                str += ", ";
        }	
        return str + "]";
    }

    private void writeObject(java.io.ObjectOutputStream out) 
	throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) 
	throws IOException, ClassNotFoundException
    {
        if (in instanceof TypeInputStream) {
            @SuppressWarnings("unchecked")
            ParamTypeSystem<Formal, Actual> ts = (ParamTypeSystem<Formal, Actual>) ((TypeInputStream) in).getTypeSystem();
            this.ts = ts;
        }

	this.cache = new HashMap<CacheTypeWrapper, Type>();

        in.defaultReadObject();
    }
}
