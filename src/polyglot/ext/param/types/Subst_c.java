package polyglot.ext.param.types;

import polyglot.ext.jl.types.*;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.*;
import java.util.*;

import polyglot.ext.param.Topics;
import polyglot.main.Report;

/**
 * Implementation of a ClassType that performs substitutions using a
 * map.  Subclasses must define how the substititions are performed and
 * how to cache substituted types.
 */
public class Subst_c implements Subst
{
    /** Map from formal parameters (of type Param) to actuals. */
    protected Map subst;

    /** Cache of types. */
    protected Map cache;

    protected transient ParamTypeSystem ts;

    public Subst_c(ParamTypeSystem ts, Map subst, Map cache)
    {
        this.ts = ts;
        this.subst = subst;
        this.cache = new HashMap();
        this.cache.putAll(cache);
    }

    public ParamTypeSystem typeSystem() {
        return ts;
    }

    public Iterator entries() {
        return substitutions().entrySet().iterator();
    }

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

            for (Iterator i = tsubst.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                Object formal = e.getKey();
                Object actual = e.getValue();

                if (subst.containsKey(actual)) {
                    // In this case:
                    //   this.base is C[T], where T is a formal of C
                    //   t.base is D[U], where U is a formal of D
                    //   t.subst has U -> T, thus t is D[T]
                    //   this.subst has T -> X
                    // so replace U -> T in t.subst with U -> X
                    newSubst.put(formal, subst.get(actual));
                }
                else {
                    newSubst.put(formal, actual);
                }
            }

            // Now add our substitutions, overriding any substitutions
            // performed in t.subst
            newSubst.putAll(subst);

            // We can use the same cache, since newSubst is compatible with
            // this.subst.
            return ts.subst(tbase, newSubst, cache);
        }

        if (t instanceof ClassType) {
            return substClassType((ClassType) t);
        }

        return t;
    }

    /** Perform substitutions on a class type. */
    public ClassType substClassType(ClassType t) {
        return new SubstClassType_c(ts, t.position(), t, this);
    }

    /** Perform substitutions on a type. */
    public Type substType(Type t) {
        if (t == null || t == this)
            return t;

        Type cached = (Type) cache.get(t);

        if (cached == null) {
            cached = uncachedSubstType(t);
            cache.put(t, cached);

            if (Report.should_report(Topics.subst, 2))
                Report.report(2, "substType(" +
                              t + ": " + t.getClass().getName() + ") = " +
                              cached + ": " + cached.getClass().getName());
        }

        return cached;
    }

    /** Perform substititions on a field. */
    public FieldInstance substField(FieldInstance fi) {
        ReferenceType ct = (ReferenceType) substType(fi.container());
        Type t = substType(fi.type());
        return fi.type(t).container(ct);
    }

    /** Perform substititions on a method. */
    public MethodInstance substMethod(MethodInstance mi) {
        ReferenceType ct = (ReferenceType) substType(mi.container());

        Type rt = substType(mi.returnType());

        List formalTypes = mi.argumentTypes();
        formalTypes = substTypeList(formalTypes);

        List throwTypes = mi.exceptionTypes();
        throwTypes = substTypeList(throwTypes);

        return (MethodInstance) mi.returnType(rt).argumentTypes(formalTypes).exceptionTypes(throwTypes).container(ct);
    }

    /** Perform substititions on a constructor. */
    public ConstructorInstance substConstructor(ConstructorInstance ci) {
        ClassType ct = (ClassType) substType(ci.container());

        List formalTypes = ci.argumentTypes();
        formalTypes = substTypeList(formalTypes);

        List throwTypes = ci.exceptionTypes();
        throwTypes = substTypeList(throwTypes);

        return (ConstructorInstance) ci.argumentTypes(formalTypes).exceptionTypes(throwTypes).container(ct);
    }

    public List substTypeList(List list) {
        return new CachingTransformingList(list, new TypeXform());
    }

    public List substMethodList(List list) {
        return new CachingTransformingList(list, new MethodXform());
    }

    public List substConstructorList(List list) {
        return new CachingTransformingList(list, new ConstructorXform());
    }

    public List substFieldList(List list) {
        return new CachingTransformingList(list, new FieldXform());
    }

    ////////////////////////////////////////////////////////////////
    // Substitution machinery

    /** Function object for transforming types. */
    public class TypeXform implements Transformation {
        public Object transform(Object o) {
            if (! (o instanceof Type)) {
                throw new InternalCompilerError(o + " is not a type.");
            }

            return substType((Type) o);
        }
    }

    /** Function object for transforming fields. */
    public class FieldXform implements Transformation {
        public Object transform(Object o) {
            if (! (o instanceof FieldInstance)) {
                throw new InternalCompilerError(o + " is not a field.");
            }

            return substField((FieldInstance) o);
        }
    }

    /** Function object for transforming methods. */
    public class MethodXform implements Transformation {
        public Object transform(Object o) {
            if (! (o instanceof MethodInstance)) {
                throw new InternalCompilerError(o + " is not a method.");
            }

            return substMethod((MethodInstance) o);
        }
    }

    /** Function object for transforming constructors. */
    public class ConstructorXform implements Transformation {
        public Object transform(Object o) {
            if (! (o instanceof ConstructorInstance)) {
                throw new InternalCompilerError(o + " is not a constructor.");
            }

            return substConstructor((ConstructorInstance) o);
        }
    }

    ////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////
    // Utility functions

    public String toString() {
        String str = "[";
        for (Iterator iter = subst.keySet().iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            str += "<" + key + ": " + subst.get(key) + ">";
            if (iter.hasNext())
                str += ", ";
        }
        return str + "]";
    }
}
