package jltools.types;

import jltools.util.*;
import jltools.frontend.Source;

import java.util.*;


/**
 * An <code>ImportTable</code> is a type of <code>ClassResolver</code> that
 * corresponds to a particular source file.
 * <p>
 * It has a set of package and class imports, which caches the results of
 * lookups for future reference.
 */
public class ImportTable extends ClassResolver
{
    protected TypeSystem ts;

    /** The underlying resolver. */
    protected Resolver resolver;
    /** A list of all package imports. */
    protected List packageImports;
    /** A list of all class imports. */
    protected List classImports;
    /** Map from names to classes found. */
    protected Map map;
    /** Our package */
    protected Package pkg;
    /** Source name to use for debugging and error reporting */
    protected String sourceName;

    public ImportTable(TypeSystem ts, Resolver base, Package pkg) {
        this(ts, base, pkg, null);
    }

    public ImportTable(TypeSystem ts, Resolver base, Package pkg, String src) {
	this.resolver = base;
	this.ts = ts;
        this.pkg = pkg;
        this.sourceName = src;

	this.map = new HashMap();
	this.packageImports = new ArrayList();
        this.classImports = new ArrayList();
    }

    public Package package_() {
        return pkg;
    }

    public void addClassImport(String className) throws SemanticException {
	Types.report(1, this + ": class import " + className);

        // Bring the class in now.
        Type t = resolver.findType(className);
        String shortName = StringUtil.getShortNameComponent(className);

        Types.report(1, this + ": import " + shortName + " as " + t);

        if (map.containsKey(shortName)) {
            Type s = (Type) map.get(shortName);

            if (! ts.isSame(s, t)) {
                throw new SemanticException("Class " + shortName +
                    " already defined as " + map.get(shortName),
                    new Position(sourceName));
            }
        }

        map.put(className, t);
        map.put(shortName, t);

        classImports.add(className);
    }

    public void addPackageImport(String pkgName) {
	packageImports.add(pkgName);
    }

    public Type findType(String name) throws SemanticException {
	// FIXME: need to keep on looking to find conflicts.
	Types.report(1, this + ".findType(" + name + ")");


	if (StringUtil.isNameShort(name)) {
	    // First see if we have a mapping already.
	    Object res = map.get(name);

	    if (res != null) {
		return (Type) res;
	    }

            List imports = new ArrayList(packageImports.size() + 5);

            if (pkg != null) {
                imports.add(pkg.fullName());
            }

            imports.addAll(ts.defaultPackageImports());
            imports.addAll(packageImports);

	    // It wasn't a class import.  Maybe it was a package import?
	    for (Iterator iter = imports.iterator(); iter.hasNext(); ) {
		String pkgName = (String) iter.next();
		String fullName = pkgName + "." + name;

		boolean inSamePackage = this.pkg != null &&
					this.pkg.fullName().equals(pkgName);

		try {
		    Type t = resolver.findType(fullName);

		    // Check if the type is visible in this package.
		    // FIXME: Assume non-class types are always visible.
		    if (! t.isClass() ||
			t.toClass().flags().isPublic() ||
		        inSamePackage) {

			// Memoize.
			map.put(name, t);

			return t;
		    }
		}
		catch (NoClassException ex) {
		    // Do nothing.
		}
	    }

	    // The name was short, but not in any imported class or package.
	    // Check the null package.
	    Type t = resolver.findType(name); // may throw exception

	    if (t.isClass() &&
		t.toClass().flags().isPackage() && this.pkg != null) {
	      // Not visible.
	      throw new NoClassException("Class \"" + name + "\" not found.",
                                         new Position(sourceName));
	    }

	    // Memoize.
	    map.put(name, t);

	    return t;
	}

	// The name was long.
	return resolver.findType(name);
    }

    public String toString() {
        if (sourceName != null) {
            return "(import " + sourceName + ")";
        }
        else {
            return "(import)";
        }
    }
}
