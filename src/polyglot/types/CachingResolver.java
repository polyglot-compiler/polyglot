package polyglot.types;

import polyglot.ast.*;
import polyglot.util.*;
import java.util.*;

/**
 * An <code>CachingResolver</code> memoizes another Resolver
 */
public class CachingResolver implements Resolver {
    Resolver inner;
    Map cache;
    Map workingCache; //storing the unrestored class types

    /**
     * Create a caching resolver.
     * @param inner The resolver whose results this resolver caches.
     */
    public CachingResolver(Resolver inner) {
	this.inner = inner;
	this.cache = new HashMap();
	this.workingCache = new HashMap();
    }

    /**
     * The resolver whose results this resolver caches.
     */
    public Resolver inner() {
        return this.inner;
    }

    public String toString() {
        return "(cache " + inner.toString() + ")";
    }

    /**
     * Find a qualifier (a type or package) by name.
     * @param name The name to search for.
     */
    public Qualifier findQualifier(String name) throws SemanticException {
        Qualifier q = (Qualifier) cache.get(name);

	if (q == null) {
	    Qualifier qq = (Qualifier) workingCache.get(name);
	    if (qq!=null) return qq;
	    q = inner.findQualifier(name);
	    cache.put(name, q);
	}

	return q;
    }

    /**
     * Check if a type is in the cache, returning null if not.
     * @param name The name to search for.
     */
    public Type checkType(String name) {
        Type t = (Type) cache.get(name);
        if (t == null) {
            return (Type) workingCache.get(name);
        }
        return t;
    }

    /**
     * Lookup a type, checking the cache first, then the inner resolver. 
     * @param name The name to search for.
     */
    public Type findType(String name) throws SemanticException {
        Qualifier q = (Qualifier) findQualifier(name);

	if (! (q instanceof Type)) {
	    throw new NoClassException("Could not find type " + name + ".");
	}

	return (Type) q;
    }
    
    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    public void medianResult(String name, Qualifier q) {
	workingCache.put(name, q);
    }
}
