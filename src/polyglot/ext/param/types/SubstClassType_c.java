package polyglot.ext.param.types;

import polyglot.ext.jl.types.*;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.*;
import java.util.*;

/**
 * Implementation of a ClassType that performs substitutions using a
 * map.  Subclasses must define how the substititions are performed and
 * how to cache substituted types.
 */
public abstract class SubstClassType_c extends ClassType_c
{
    /** The class type we are substituting into. */
    protected ClassType base;

    /** Map from formal parameters (of type Param) to actuals. */
    protected Map subst;

    /** Map from formal parameters (of type Param) to actuals. */
    protected Map cache;

    public SubstClassType_c(TypeSystem ts, Position pos,
                            ClassType base, Map subst, Map cache)
    {
        super(ts, pos);
        this.base = base;
        this.subst = subst;
        this.cache = new HashMap();
        this.cache.put(base, this);
        this.cache.putAll(cache);
    }

    ////////////////////////////////////////////////////////////////
    // Type caching

    /** Lookup a type in the cache. */
    protected Type lookup(Type type) {
        return (Type) cache.get(type);
    }

    /** Install a type into the cache. */
    protected void cache(Type type, Type substType) {
        cache.put(type, substType);
    }

    ////////////////////////////////////////////////////////////////
    // Substitution methods

    /** Perform substitutions on a type, without checking the cache. */
    public Type uncachedSubstType(Type t) {
        ParamTypeSystem ts = (ParamTypeSystem) this.ts;

        // We only need to perform substitutions if t might mention one of the
        // formal parameters.  This can only happen if t is enclosed in base.
        if (t.isClass() && t.toClass().isEnclosed(base)) {
            return ts.subst(t.position(), t, subst, cache);
        }

        return t;
    }

    /** Perform substitutions on a type using the cache. */
    public Type substType(Type t) {
        if (t == base) {
            return this;
        }

        if (t == null) {
            return null;
        }

        Type cached = lookup(t);

        if (cached != null) {
            return cached;
        }

        Type st = uncachedSubstType(t);

        if (t != st) {
            cache(t, st);
        }

        return st;
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
        formalTypes = new CachingTransformingList(formalTypes, new TypeXform());

        List throwTypes = mi.exceptionTypes();
        throwTypes = new CachingTransformingList(throwTypes, new TypeXform());

        return (MethodInstance) mi.returnType(rt).argumentTypes(formalTypes).exceptionTypes(throwTypes).container(ct);
    }

    /** Perform substititions on a constructor. */
    public ConstructorInstance substConstructor(ConstructorInstance ci) {
        ClassType ct = (ClassType) substType(ci.container());

        List formalTypes = ci.argumentTypes();
        formalTypes = new CachingTransformingList(formalTypes, new TypeXform());

        List throwTypes = ci.exceptionTypes();
        throwTypes = new CachingTransformingList(throwTypes, new TypeXform());

        return (ConstructorInstance) ci.argumentTypes(formalTypes).exceptionTypes(throwTypes).container(ct);
    }

    ////////////////////////////////////////////////////////////////
    // Perform substitutions on these operations of the base class

    /** Get the class's super type. */
    public Type superType() {
        return substType(base.superType());
    }

    /** Get the class's interfaces. */
    public List interfaces() {
        return new CachingTransformingList(base.interfaces(),
                                           new TypeXform());
    }

    /** Get the class's fields. */
    public List fields() {
        return new CachingTransformingList(base.fields(),
                                           new FieldXform());
    }

    /** Get the class's methods. */
    public List methods() {
        return new CachingTransformingList(base.methods(),
                                           new MethodXform());
    }

    /** Get the class's constructors. */
    public List constructors() {
        return new CachingTransformingList(base.constructors(),
                                           new ConstructorXform());
    }

    /** Get the class's member classes. */
    public List memberClasses() {
        return new CachingTransformingList(base.memberClasses(),
                                           new TypeXform());
    }

    /** Get the class's outer class, if an inner class. */
    public ClassType outer() {
        return (ClassType) substType(base.outer());
    }

    ////////////////////////////////////////////////////////////////
    // Delegate the rest of the class operations to the base class

    /** Get the class's kind: top-level, member, local, or anonymous. */
    public ClassType.Kind kind() {
        return base.kind();
    }

    /** Get the class's full name, if possible. */
    public String fullName() {
        return base.fullName();
    }

    /** Get the class's short name, if possible. */
    public String name() {
        return base.name();
    }

    /** Get the class's package, if possible. */
    public Package package_() {
        return base.package_();
    }

    public Flags flags() {
        return base.flags();
    }

    public String translate(Resolver c) {
        return base.translate(c);
    }

    ////////////////////////////////////////////////////////////////
    // Equality tests

    /** Type equality test. */
    public boolean isSameImpl(Type t) {
        if (! (t instanceof SubstClassType_c)) return false;

        SubstClassType_c x = (SubstClassType_c) t;
        if (! base.isSame(x.base)) return false;
        if (! subst.equals(x.subst)) return false;

        return true;
    }

    /** Hash code. */
    public int hashCode() {
        return base.hashCode();
    }

    /** Get the class on that we are performing substitutions. */
    public ClassType parametricType() {
        return base;
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
}
