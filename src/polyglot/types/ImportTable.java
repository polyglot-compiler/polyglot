package polyglot.types;

import polyglot.util.*;
import polyglot.main.Report;

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
    /** Map from names to classes found. */
    protected Map map;
    /** List of imports which will be lazily added to the table. */
    protected List lazyImports;
    /** List of imports which will be lazily added to the table. */
    protected List classImports;
    /** Source name to use for debugging and error reporting */
    protected String sourceName;
    /** Position to use for error reporting */
    protected Position sourcePos;
    /** Our package */
    protected Package pkg;

    /**
     * Create an import table.
     * @param ts The type system
     * @param base The outermost resolver to use for looking up types.
     * @param pkg The package of the source we are importing types into.
     */
    public ImportTable(TypeSystem ts, Resolver base, Package pkg) {
        this(ts, base, pkg, null);
    }

    /**
     * Create an import table.
     * @param ts The type system
     * @param base The outermost resolver to use for looking up types.
     * @param pkg The package of the source we are importing types into.
     * @param src The name of the source file we are importing into.
     */
    public ImportTable(TypeSystem ts, Resolver base, Package pkg, String src) {
        this.resolver = base;
        this.ts = ts;
        this.sourceName = src;
        this.sourcePos = src != null ? new Position(src) : null;
        this.pkg = pkg;

	this.map = new HashMap();
	this.packageImports = new ArrayList();
	this.lazyImports = new ArrayList();
	this.classImports = new ArrayList();
    }

    /**
     * The package of the source we are importing types into.
     */
    public Package package_() {
        return pkg;
    }

    /**
     * Add a class import.
     */
    public void addClassImport(String className) {
        if (Report.should_report(new String[] {Report.types, Report.resolver, Report.imports}, 2))
            Report.report(2, this + ": lazy import " + className);

	lazyImports.add(className);
        classImports.add(className);
    }

    /**
     * Add a package import.
     */
    public void addPackageImport(String pkgName) {
	packageImports.add(pkgName);
    }

    /**
     * List the packages we import from.
     */
    public List packageImports() {
        return packageImports;
    }

    /**
     * List the classes explicitly imported.
     */
    public List classImports() {
        return classImports;
    }

    /**
     * The name of the source file we are importing into.
     */
    public String sourceName() {
        return sourceName;
    }

    /**
     * Find a type by name.
     */
    public Named find(String name) throws SemanticException {
	// FIXME: need to keep on looking to find conflicts.
        if (Report.should_report(new String[] {Report.types, Report.resolver, Report.imports}, 2))
	    Report.report(2, this + ".find(" + name + ")");

	/* First add any lazy imports. */
	lazyImport();

	if (StringUtil.isNameShort(name)) {
	    // First see if we have a mapping already.
	    Object res = map.get(name);

	    if (res != null) {
		return (Named) res;
	    }

            List imports = new ArrayList(packageImports.size() + 5);

            if (pkg != null) {
                imports.add(pkg.fullName());
            }

            imports.addAll(ts.defaultPackageImports());
            imports.addAll(packageImports);

            // It wasn't a ClassImport.  Maybe it was a PackageImport?
	    for (Iterator iter = imports.iterator(); iter.hasNext(); ) {
		String pkgName = (String) iter.next();
		String fullName = pkgName + "." + name;

		try {
                    Named n = resolver.find(fullName); 
                    
                    // Check if the type is visible in this package.
                    if (isVisibleFrom(n, pkgName)) {
                        // Memoize.
                        map.put(name, n);

                        return n;
                    }
		}
		catch (NoClassException ex) {
		    // Do nothing.
		}
	    }

	    // The name was short, but not in any imported class or package.
	    // Check the null package.
	    Named n = resolver.find(name); // may throw exception

            if (!isVisibleFrom(n, "")) {
	      // Not visible.
	      throw new NoClassException(name, sourcePos);
	    }

	    // Memoize.
	    map.put(name, n);

	    return n;
	}

	// The name was long.
	return resolver.find(name);
    }

    /**
     * Return whether <code>n</code> is visible from within
     * package <code>pkg</code>.  The empty string may
     * be passed in to represent the default package.
     */
    protected boolean isVisibleFrom(Named n, String pkgName) {
        boolean isVisible = false;
        boolean inSamePackage = this.pkg != null 
                && this.pkg.fullName().equals(pkgName)
            || this.pkg == null 
                && pkgName.equals("");
        if (n instanceof Type) {
            Type t = (Type) n;
            //FIXME: Assume non-class types are always visible.
            isVisible = !t.isClass() 
                || t.toClass().flags().isPublic() 
                || inSamePackage; 
        } else {
            //FIXME: Assume non-types are always visible.
            isVisible = true;
        }
        return isVisible;
    }
    
    /**
     * Load the class imports, lazily.
     */
    protected void lazyImport() throws SemanticException {
	if (lazyImports.isEmpty()) {
            return;
	}

	for (int i = 0; i < lazyImports.size(); i++) {
	    String longName = (String) lazyImports.get(i);

            if (Report.should_report(new String[] {Report.types, Report.resolver, Report.imports}, 2))
		Report.report(2, this + ": import " + longName);

	    try {
		Named t = resolver.find(longName);
		String shortName = StringUtil.getShortNameComponent(longName);

                if (Report.should_report(new String[] {Report.types, Report.resolver, Report.imports}, 2))
		    Report.report(2, this + ": import " + shortName + " as " + t);

		if (map.containsKey(shortName)) {
		    Named s = (Named) map.get(shortName);

		    if (! ts.equals(s, t)) {
			throw new SemanticException("Class " + shortName +
			    " already defined as " + map.get(shortName),
                            sourcePos);
		    }
		}

		map.put(longName, t);
		map.put(shortName, t);
	    }
	    catch (SemanticException e) {
                if (e.position == null) {
                    e.position = sourcePos;
                }

                throw e;
	    }
	}

	lazyImports = new ArrayList();
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
