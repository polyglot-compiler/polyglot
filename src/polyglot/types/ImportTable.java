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
    /** Map from names to classes found. */
    protected Map map;
    /** List of imports which will be lazily added to the table. */
    protected List lazyImports;
    /** Used to report errors for lazily added imports. */
    protected ErrorQueue eq;
    /** The source file associated with the import table. */
    protected Source source;
    /** Our package */
    protected String pkgName;
    protected Package pkg;

    public ImportTable(TypeSystem ts, Resolver base,
	               Source source, ErrorQueue eq) {
	this.resolver = base;
	this.ts = ts;
	this.source = source;
	this.eq = eq;

	this.map = new HashMap();
	this.packageImports = new ArrayList();
	this.lazyImports = new ArrayList();
    }

    public void addDefaultImports() throws SemanticException {
	List imports = ts.defaultPackageImports();

	for (Iterator i = imports.iterator(); i.hasNext(); ) {
	    String s = (String) i.next();
	    addPackageImport(s);
	}
    }

    public Package package_() {
        return pkg;
    }

    public void setPackage(String pkgName) {
	this.pkgName = pkgName;
        this.pkg = ts.packageForName(pkgName);

	if (packageImports.size() != 0) {
	  throw new InternalCompilerError(
	      "ImportTable.setPackage() must be called before any " +
	      "package imports are added.");
	}

	if (pkgName != null) {
	    addPackageImport(pkgName);
	}
    }

    public void addClassImport(String className) {
	Types.report(1, this + ": lazy import " + className);
	lazyImports.add(className);
    }

    public void addPackageImport(String pkgName) {
	packageImports.add(pkgName);
    }

    public Type findType(String name) throws SemanticException {
	// FIXME: need to keep on looking to find conflicts.
	Types.report(1, this + ".findType(" + name + ")");

	/* First add any lazy imports. */
	lazyImport();

	if (StringUtil.isNameShort(name)) {
	    // First see if we have a mapping already.
	    Object res = map.get(name);

	    if (res != null) {
		return (Type) res;
	    }

	    // It wasn't a ClassImport.  Maybe it was a PackageImport?
	    for (Iterator iter = packageImports.iterator(); iter.hasNext(); ) {
		String pkgName = (String) iter.next();
		String fullName = pkgName + "." + name;

		boolean inSamePackage = this.pkgName != null &&
					this.pkgName.equals(pkgName);

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
		t.toClass().flags().isPackage() && this.pkgName != null) {
	      // Not visible.
	      throw new NoClassException("Class \"" + name + "\" not found.",
					 new Position(source.name()));
	    }

	    // Memoize.
	    map.put(name, t);

	    return t;
	}

	// The name was long.
	return resolver.findType(name);
    }

    public String toString() {
      return "(import " + source.name() + ")";
    }

    protected void lazyImport() {
	if (lazyImports.isEmpty()) {
	  return;
	}

	for (int i = 0; i < lazyImports.size(); i++) {
	    String longName = (String) lazyImports.get(i);

	    Types.report(1, this + ": import " + longName);

	    try {
		Type t = resolver.findType(longName);
		String shortName = StringUtil.getShortNameComponent(longName);

		Types.report(1, this + ": import " + shortName + " as " + t);

		if (map.containsKey(shortName)) {
		    Type s = (Type) map.get(shortName);

		    if (! ts.isSame(s, t)) {
			throw new SemanticException("Class " + shortName +
			    " already defined as " + map.get(shortName),
			    new Position(source.name()));
		    }
		}

		map.put(longName, t);
		map.put(shortName, t);
	    }
	    catch (SemanticException e) {
		eq.enqueue(ErrorInfo.SEMANTIC_ERROR, e.getMessage(),
			      new Position(source.name()));
	    }
	}

	lazyImports = new ArrayList();
    }
}
