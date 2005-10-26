package polyglot.types;

import polyglot.util.*;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Report;
import polyglot.types.Package;
import java.util.*;

/**
 * The <code>SystemResolver</code> is the main resolver for
 * fully-qualified names.
 */
public class SystemResolver extends CachingResolver implements TopLevelResolver {
    Map packageCache;
    ExtensionInfo extInfo;

    /**
     * Create a caching resolver.
     * @param inner The resolver whose results this resolver caches.
     */
    public SystemResolver(TopLevelResolver inner, ExtensionInfo extInfo) {
        super(inner);
        this.extInfo = extInfo;
	this.packageCache = new HashMap();
    }

    /** Check if a package exists in the resolver cache. */
    protected boolean packageExistsInCache(String name) {
        for (Iterator i = cache.values().iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof Importable) {
                Importable im = (Importable) o;
                if (im.package_() != null &&
                    im.package_().fullName() != null &&
                    im.package_().fullName().startsWith(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if a package exists.
     */
    public boolean packageExists(String name) {
	Boolean b = (Boolean) packageCache.get(name);
	if (b != null) {
	    return b.booleanValue();
	}
	else {
            String prefix = StringUtil.getPackageComponent(name);

            if (packageCache.get(prefix) == Boolean.FALSE) {
                packageCache.put(name, Boolean.FALSE);
                return false;
            }

            boolean exists;
            exists = packageExistsInCache(name);
            if (! exists) {
                exists = ((TopLevelResolver) inner).packageExists(name);
            }

            if (exists) {
                packageCache.put(name, Boolean.TRUE);

                do {
                    packageCache.put(prefix, Boolean.TRUE);
                    prefix = StringUtil.getPackageComponent(prefix);
                } while (! prefix.equals(""));
            }
            else {
                packageCache.put(name, Boolean.FALSE);
            }

            return exists;
	}
    }

    protected void cachePackage(Package p) {
        if (p != null) {
            packageCache.put(p.fullName(), Boolean.TRUE);
            cachePackage(p.prefix());
        }
    }

    /**
     * Check if a type is in the cache, returning null if not.
     * @param name The name to search for.
     */
    public Type checkType(String name) {
        return (Type) check(name);
    }

    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    /*
    public void install(String name, Named q) {
        if (Report.should_report(TOPICS, 1)) {
            Report.report(3, "CachingResolver: installing " + name + "->" + q + " in resolver cache");
        }
        Object p = cache.get(name);
        if (p != null) {
            if (p != q) {
                throw new InternalCompilerError("Attempt to install duplicate class in resolver cache: " + q + " duplicate of " + p);
            }
        }
        else {
        }
        cache.put(name, q);
    }
    */

    /**
     * Install a qualifier in the cache.
     * @param name The name of the qualifier to insert.
     * @param q The qualifier to insert.
     */
    public void addNamed(String name, Named q) throws SemanticException {
	super.addNamed(name, q);
        
        if (q instanceof ClassType) {
            ClassType ct = (ClassType) q;
            if (ct.isTopLevel()) {
                Package p = ((ClassType) q).package_();
                cachePackage(p);
            }
            else if (ct.isMember()) {
                if (name.equals(ct.fullName())) {
                    // Check that the names match; we could be installing
                    // a member class under its class file name, not its Java
                    // source full name.
                    addNamed(StringUtil.getPackageComponent(name), ct.outer());
                }
            }
        }
        else if (q instanceof Package) {
            cachePackage((Package) q);
        }

	if (q instanceof Type && packageExists(name)) {
	    throw new SemanticException("Type \"" + name +
					"\" clashes with package of the same name.", q.position());
	}
    }

    private static final Collection TOPICS =
                    CollectionUtil.list(Report.types,
                                        Report.resolver,
                                        "sysresolver");
}
