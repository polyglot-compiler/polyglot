package polyglot.types;

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.main.Report;
import java.util.*;

/**
 * An <code>CachingResolver</code> memoizes another Resolver
 */
public class CachingResolver implements Resolver {
    Resolver inner;
    Map cache;

    /**
     * Create a caching resolver.
     * @param inner The resolver whose results this resolver caches.
     */
    public CachingResolver(Resolver inner) {
	this.inner = inner;
	this.cache = new HashMap();
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
        if (Report.should_report(new String[] {Report.types, Report.resolver}, 2))
            Report.report(2, "CachingResolver: find: " + name);

        Qualifier q = (Qualifier) cache.get(name);

	if (q == null) {
            if (Report.should_report(new String[] {Report.types, Report.resolver}, 3))
                Report.report(3, "CachingResolver: not cached: " + name);
	    q = inner.findQualifier(name);
	    cache.put(name, q);
            if (Report.should_report(new String[] {Report.types, Report.resolver}, 3))
                Report.report(3, "CachingResolver: loaded: " + name);
	}
        else {
            if (Report.should_report(new String[] {Report.types, Report.resolver}, 3))
                Report.report(3, "CachingResolver: cached: " + name);
        }


	return q;
    }

    /**
     * Check if a type is in the cache, returning null if not.
     * @param name The name to search for.
     */
    public Type checkType(String name) {
        return (Type) cache.get(name);
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
    public void install(String name, Qualifier q) {
	cache.put(name, q);
    }
}
