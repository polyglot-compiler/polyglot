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
     * Find a type object by name.
     * @param name The name to search for.
     */
    public Named find(String name) throws SemanticException {
        if (Report.should_report(new String[] {Report.types, Report.resolver}, 2))
            Report.report(2, "CachingResolver: find: " + name);

        Named q = (Named) cache.get(name);

	if (q == null) {
            if (Report.should_report(new String[] {Report.types, Report.resolver}, 3))
                Report.report(3, "CachingResolver: not cached: " + name);
	    q = inner.find(name);
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
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    public void install(String name, Qualifier q) {
	cache.put(name, q);
    }
}
