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

package polyglot.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;

/**
 * A <code>CachingResolver</code> memoizes another Resolver
 */
public class CachingResolver implements Resolver, Copy {
    protected Resolver inner;
    private Map<String, CachedResult> cache;
    private boolean cacheNotFound;

    protected static class CachedResult {
        protected static final class Success extends CachedResult {
            final Named named;

            Success(Named named) {
                this.named = named;
            }

            @Override
            public String toString() {
                return named.toString();
            }
        }

        protected static final class Error extends CachedResult {
            final NoClassException exc;

            Error(NoClassException exc) {
                this.exc = exc;
            }

            @Override
            public String toString() {
                return exc.toString();
            }
        }
    }

    /**
     * Create a caching resolver.
     * @param inner The resolver whose results this resolver caches.
     */
    public CachingResolver(Resolver inner, boolean cacheNotFound) {
        this.inner = inner;
        this.cacheNotFound = cacheNotFound;
        this.cache = new HashMap<String, CachedResult>();
    }

    public CachingResolver(Resolver inner) {
        this(inner, true);
    }

    protected boolean shouldReport(int level) {
        return (Report.should_report("sysresolver", level) && this instanceof SystemResolver)
                || Report.should_report(TOPICS, level);
    }

    @Override
    public CachingResolver copy() {
        try {
            CachingResolver r = (CachingResolver) super.clone();
            r.cache = new HashMap<String, CachedResult>(this.cache);
            return r;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("clone failed");
        }
    }

    /**
     * The resolver whose results this resolver caches.
     */
    public Resolver inner() {
        return this.inner;
    }

    @Override
    public String toString() {
        return "(cache " + inner.toString() + ")";
    }

    protected Collection<CachedResult> cachedResults() {
        return cache.values();
    }

    /**
     * Find a type object by name.
     * @param name The name to search for.
     */
    @Override
    public Named find(String name) throws SemanticException {
        if (shouldReport(2))
            Report.report(2, "CachingResolver: find: " + name);

        CachedResult cached = cache.get(name);

        if (cached instanceof CachedResult.Error)
            throw ((CachedResult.Error) cached).exc;

        Named q = cached == null ? null : ((CachedResult.Success) cached).named;

        if (q == null) {
            if (shouldReport(3))
                Report.report(3, "CachingResolver: not cached: " + name);

            try {
                q = inner.find(name);
            }
            catch (NoClassException e) {
                if (shouldReport(3)) {
                    Report.report(3, "CachingResolver: " + e.getMessage());
                    Report.report(3, "CachingResolver: installing " + name
                            + "-> (not found) in resolver cache");
                }
                if (cacheNotFound) {
                    cache.put(name, new CachedResult.Error(e));
                }
                throw e;
            }

            addNamed(name, q);

            if (shouldReport(3))
                Report.report(3, "CachingResolver: loaded: " + name);
        }
        else {
            if (shouldReport(3))
                Report.report(3, "CachingResolver: cached: " + name);
        }

        return q;
    }

    /**
     * Check if a type object is in the cache, returning null if not.
     * @param name The name to search for.
     */
    public Named check(String name) {
        CachedResult cached = cache.get(name);
        if (!(cached instanceof CachedResult.Success)) return null;
        return ((CachedResult.Success) cached).named;
    }

    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    public void install(String name, Named q) {
        if (shouldReport(3))
            Report.report(3, "CachingResolver: installing " + name + "->" + q
                    + " in resolver cache");
        if (shouldReport(5)) new Exception().printStackTrace();

        cache.put(name, new CachedResult.Success(q));
    }

    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     * @throws SemanticException 
     */
    public void addNamed(String name, Named q) throws SemanticException {
        install(name, q);
    }

    public void dump() {
        Report.report(1, "Dumping " + this);
        for (Map.Entry<String, CachedResult> e : cache.entrySet()) {
            Report.report(2, e.toString());
        }
    }

    private static final Collection<String> TOPICS =
            CollectionUtil.list(Report.types, Report.resolver);
}
