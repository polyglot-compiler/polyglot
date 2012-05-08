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
import java.util.*;

import polyglot.ext.param.Topics;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

/**
 * Utility class that performs substitutions on type objects using a map.
 * Subclasses must define how the substititions are performed and how to cache
 * substituted types.
 */
public class Subst_c implements Subst {
	/** Map from formal parameters (of type Param) to actuals. */
	protected Map subst;

	/** Cache of types. From CacheTypeWrapper(t) to subst(t) */
	protected transient Map cache;

	protected transient ParamTypeSystem ts;

	public Subst_c(ParamTypeSystem ts, Map subst, Map cache) {
		this.ts = ts;
		this.subst = subst;
		this.cache = new HashMap();
		this.cache.putAll(cache);
	}

	public ParamTypeSystem typeSystem() {
		return ts;
	}

	/**
	 * Entries of the underlying substitution map.
	 * 
	 * @return an <code>Iterator</code> of <code>Map.Entry</code>.
	 */
	public Iterator entries() {
		return substitutions().entrySet().iterator();
	}

	/**
	 * The underlying substitution map.
	 */
	public Map substitutions() {
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
			Type tbase = ((SubstType) t).base();
			Map tsubst = ((SubstType) t).subst().substitutions();

			Map newSubst = new HashMap();

			// go through the map, and perform substitution on the actuals
			for (Iterator i = tsubst.entrySet().iterator(); i.hasNext();) {
				Map.Entry e = (Map.Entry) i.next();
				Object formal = e.getKey();
				Object actual = e.getValue();

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
	 * This method allows subclasses to perform substitution on a value in the
	 * substitution map (B in the example above). Subclasses may need to
	 * override this if the keys and values are not the same object.
	 */
	protected Object substSubstValue(Object value) {
		return value;
	}

	/**
	 * Perform substitutions on a class type. Substitutions are performed
	 * lazily.
	 */
	public ClassType substClassType(ClassType t) {
		return new SubstClassType_c(ts, t.position(), t, this);
	}

	/** Perform substitutions on a type. */
	public Type substType(Type t) {
		if (t == null || t == this) // XXX comparison t == this can't succeed!
									// (Findbugs)
			return t;

		Type cached = cacheGet(t);

		if (cached == null) {
			cached = uncachedSubstType(t);
			cachePut(t, cached);

			if (Report.should_report(Topics.subst, 2))
				Report.report(2, "substType(" + t + ": "
						+ t.getClass().getName() + ") = " + cached + ": "
						+ cached.getClass().getName());
		}

		return cached;
	}

	protected void cachePut(Type t, Type cached) {
		cache.put(new CacheTypeWrapper(t), cached);
	}

	protected Type cacheGet(Type t) {
		return (Type) cache.get(new CacheTypeWrapper(t));
	}

	class CacheTypeWrapper {
		final Type t;

		CacheTypeWrapper(Type t) {
			this.t = t;
		}

		public boolean equals(Object o) {
			if (o instanceof CacheTypeWrapper) {
				return Subst_c.this.cacheTypeEquality(t,
						((CacheTypeWrapper) o).t);
			}
			if (o instanceof Type) {
				return Subst_c.this.cacheTypeEquality(t, (Type) o);
			}
			return false;
		}

		public String toString() {
			return String.valueOf(t);
		}

		public int hashCode() {
			return t == null ? 0 : t.hashCode();
		}
	}

	/**
	 * This method is used by the cache lookup to test type equality. May be
	 * overridden by subclasses as appropriate.
	 */
	protected boolean cacheTypeEquality(Type t1, Type t2) {
		return ts.equals(t1, t2);
	}

	/** Perform substitution on a PClass. */
	public PClass substPClass(PClass pclazz) {
		MuPClass newPclazz = ts.mutablePClass(pclazz.position());
		newPclazz.formals(pclazz.formals());
		newPclazz.clazz((ClassType) substType(pclazz.clazz()));
		return newPclazz;
	}

	/** Perform substititions on a field. */
	public FieldInstance substField(FieldInstance fi) {
		ReferenceType ct = (ReferenceType) substType(fi.container());
		Type t = substType(fi.type());
		FieldInstance newFI = (FieldInstance) fi.copy();
		newFI.setType(t);
		newFI.setContainer(ct);
		return newFI;
	}

	/** Perform substititions on a method. */
	public MethodInstance substMethod(MethodInstance mi) {
		ReferenceType ct = (ReferenceType) substType(mi.container());

		Type rt = substType(mi.returnType());

		List formalTypes = mi.formalTypes();
		formalTypes = substTypeList(formalTypes);

		List throwTypes = mi.throwTypes();
		throwTypes = substTypeList(throwTypes);

		MethodInstance tmpMi = (MethodInstance) mi.copy();
		tmpMi.setReturnType(rt);
		tmpMi.setFormalTypes(formalTypes);
		tmpMi.setThrowTypes(throwTypes);
		tmpMi.setContainer(ct);

		return tmpMi;
	}

	/** Perform substititions on a constructor. */
	public ConstructorInstance substConstructor(ConstructorInstance ci) {
		ClassType ct = (ClassType) substType(ci.container());

		List formalTypes = ci.formalTypes();
		formalTypes = substTypeList(formalTypes);

		List throwTypes = ci.throwTypes();
		throwTypes = substTypeList(throwTypes);

		ConstructorInstance tmpCi = (ConstructorInstance) ci.copy();
		tmpCi.setFormalTypes(formalTypes);
		tmpCi.setThrowTypes(throwTypes);
		tmpCi.setContainer(ct);

		return tmpCi;
	}

	/** Perform substititions on a list of <code>Type</code>. */
	public List substTypeList(List list) {
		return new CachingTransformingList(list, new TypeXform());
	}

	/** Perform substititions on a list of <code>MethodInstance</code>. */
	public List substMethodList(List list) {
		return new CachingTransformingList(list, new MethodXform());
	}

	/** Perform substititions on a list of <code>ConstructorInstance</code>. */
	public List substConstructorList(List list) {
		return new CachingTransformingList(list, new ConstructorXform());
	}

	/** Perform substititions on a list of <code>FieldInstance</code>. */
	public List substFieldList(List list) {
		return new CachingTransformingList(list, new FieldXform());
	}

	// //////////////////////////////////////////////////////////////
	// Substitution machinery

	/** Function object for transforming types. */
	public class TypeXform implements Transformation {
		public Object transform(Object o) {
			if (!(o instanceof Type)) {
				throw new InternalCompilerError(o + " is not a type.");
			}

			return substType((Type) o);
		}
	}

	/** Function object for transforming fields. */
	public class FieldXform implements Transformation {
		public Object transform(Object o) {
			if (!(o instanceof FieldInstance)) {
				throw new InternalCompilerError(o + " is not a field.");
			}

			return substField((FieldInstance) o);
		}
	}

	/** Function object for transforming methods. */
	public class MethodXform implements Transformation {
		public Object transform(Object o) {
			if (!(o instanceof MethodInstance)) {
				throw new InternalCompilerError(o + " is not a method.");
			}

			return substMethod((MethodInstance) o);
		}
	}

	/** Function object for transforming constructors. */
	public class ConstructorXform implements Transformation {
		public Object transform(Object o) {
			if (!(o instanceof ConstructorInstance)) {
				throw new InternalCompilerError(o + " is not a constructor.");
			}

			return substConstructor((ConstructorInstance) o);
		}
	}

	// //////////////////////////////////////////////////////////////
	// Equality

	public boolean equals(Object o) {
		if (o instanceof Subst) {
			return subst.equals(((Subst) o).substitutions());
		}

		return false;
	}

	public int hashCode() {
		return subst.hashCode();
	}

	// //////////////////////////////////////////////////////////////
	// Utility functions

	public String toString() {
		String str = "[";
		for (Iterator iter = subst.keySet().iterator(); iter.hasNext();) {
			Object key = iter.next();
			str += "<" + key + ": " + subst.get(key) + ">";
			if (iter.hasNext())
				str += ", ";
		}
		return str + "]";
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		if (in instanceof TypeInputStream) {
			this.ts = (ParamTypeSystem) ((TypeInputStream) in).getTypeSystem();
		}

		this.cache = new HashMap();

		in.defaultReadObject();
	}
}
