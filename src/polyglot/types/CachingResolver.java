package jltools.types;

import jltools.ast.*;
import jltools.util.*;
import java.util.*;

/**
 * An <code>CachingResolver</code> memoizes another Resolver
 */
public class CachingResolver implements Resolver {
    Resolver inner;
    Map cache;

    public CachingResolver(Resolver inner) {
	this.inner = inner;
	this.cache = new HashMap();
    }

    public Resolver inner() {
        return this.inner;
    }

    public String toString() {
        return "(cache " + inner.toString() + ")";
    }

    public Qualifier findQualifier(String name) throws SemanticException {
        Qualifier q = (Qualifier) cache.get(name);

	if (q == null) {
	    q = inner.findQualifier(name);
	    cache.put(name, q);
	}

	return q;
    }

    public Type findType(String name) throws SemanticException {
        Qualifier q = (Qualifier) findQualifier(name);

	if (! (q instanceof Type)) {
	    throw new NoClassException("Could not find type " + name + ".");
	}

	return (Type) q;
    }
}
