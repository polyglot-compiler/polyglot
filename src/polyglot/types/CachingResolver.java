package polyglot.types;

import polyglot.util.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Report;
import polyglot.types.Package;
import java.util.*;

/**
 * A <code>CachingResolver</code> memoizes another Resolver
 */
public class CachingResolver implements Resolver {
    Resolver inner;
    Map cache;
    boolean cacheNotFound;

    /**
     * Create a caching resolver.
     * @param inner The resolver whose results this resolver caches.
     */
    public CachingResolver(Resolver inner, boolean cacheNotFound) {
	this.inner = inner;
        this.cacheNotFound = cacheNotFound;
	this.cache = new HashMap();
    }

    public CachingResolver(Resolver inner) {
        this(inner, true);
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
        if (Report.should_report(TOPICS, 2))
            Report.report(2, "CachingResolver: find: " + name);

        Object o = cache.get(name);

        if (o instanceof SemanticException) throw ((SemanticException)o);

        Named q = (Named) o;

	    if (q == null) {
			if (Report.should_report(TOPICS, 3))
                Report.report(3, "CachingResolver: not cached: " + name);

            try {
                q = inner.find(name);
            }
            catch (NoClassException e) {
                if (Report.should_report(TOPICS, 3)) {
                    Report.report(3, "CachingResolver: " + e.getMessage());
                    Report.report(3, "CachingResolver: installing " + name + "-> (not found) in resolver cache");
                }
                if (cacheNotFound) {
                    cache.put(name, e);
                }
                throw e;
            }

            addNamed(name, q);

            if (Report.should_report(TOPICS, 3))
                Report.report(3, "CachingResolver: loaded: " + name);
	}
        else {
            if (Report.should_report(TOPICS, 3))
                Report.report(3, "CachingResolver: cached: " + name);
        }

	return q;
    }

    /**
     * Check if a type object is in the cache, returning null if not.
     * @param name The name to search for.
     */
    public Named check(String name) {
        Object o = cache.get(name);
        if (o instanceof Throwable) return null;
        return (Named) o;
    }

    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    public void install(String name, Named q) {
        if (Report.should_report(TOPICS, 3))
            Report.report(3, "CachingResolver: installing " + name + "->" + q + " in resolver cache");
        if (Report.should_report(TOPICS, 5))
            new Exception().printStackTrace();
        cache.put(name, q);
    }

    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    public void addNamed(String name, Named q) throws SemanticException {
	install(name, q);
    }

    public void dump() {
        Report.report(1, "Dumping " + this);
        for (Iterator i = cache.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            Report.report(2, e.toString());
        }
    }

    private static final Collection TOPICS =
                    CollectionUtil.list(Report.types,
                                        Report.resolver,
                                        "sysresolver");
}
