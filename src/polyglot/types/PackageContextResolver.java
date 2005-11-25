package polyglot.types;

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.types.Package;

/**
 * A <code>PackageContextResolver</code> is responsible for looking up types
 * and packages in a package by name.
 */
public class PackageContextResolver extends AbstractAccessControlResolver
{
    protected Package p;

    /**
     * Create a package context resolver.
     * @param ts The type system.
     * @param p The package in whose context to search.
     */
    public PackageContextResolver(TypeSystem ts, Package p) {
        super(ts);
	this.p = p;
    }

    /**
     * The package in whose context to search.
     */
    public Package package_() {
        return p;
    }

    /**
     * The system resolver.
     */
    public Resolver outer() {
        return ts.systemResolver();
    }

    /**
     * Find a type object by name.
     */
    public Named find(String name, ClassType accessor) throws SemanticException {
	if (! StringUtil.isNameShort(name)) {
	    throw new InternalCompilerError(
		"Cannot lookup qualified name " + name);
	}
        
        Named n = null;

	try {
	    n = ts.systemResolver().find(p.fullName() + "." + name);
	}
	catch (NoClassException e) {
            // Rethrow if some _other_ class or package was not found.
            if (!e.getClassName().equals(p.fullName() + "." + name)) {
                throw e;
            }
	}

        if (n == null) {
            n = ts.createPackage(p, name);
        }
        
        if (! canAccess(n, accessor)) {
            throw new SemanticException("Cannot access " + n + " from " + accessor + ".");
        }
        
        return n;
    }

    protected boolean canAccess(Named n, ClassType accessor) {
        if (n instanceof ClassType) {
            return accessor == null || ts.classAccessible((ClassType) n, accessor);
        }
        return true;
    }

    public String toString() {
        return "(package-context " + p.toString() + ")";
    }
}
