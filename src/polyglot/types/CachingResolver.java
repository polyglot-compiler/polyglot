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
    public CachingResolver(Resolver inner) {
	this.inner = inner;
	this.cache = new HashMap();
	this.workingCache = new HashMap();
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
	    Qualifier qq = (Qualifier) workingCache.get(name);
	    if (qq!=null) return qq;
	    q = inner.findQualifier(name);
	    cache.put(name, q);
	}

	return q;
    }

    public Type checkType(String name) {
        Type t = (Type) cache.get(name);
        if (t == null) {
            return (Type) workingCache.get(name);
        }
        return t;
    }

    public Type findType(String name) throws SemanticException {
        Qualifier q = (Qualifier) findQualifier(name);

	if (! (q instanceof Type)) {
	    throw new NoClassException("Could not find type " + name + ".");
	}

	return (Type) q;
    }
    
    public void medianResult(String name, Qualifier q) {
	workingCache.put(name, q);
    }
}
