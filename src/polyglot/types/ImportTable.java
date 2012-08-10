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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import polyglot.main.Report;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StringUtil;

/**
 * An <code>ImportTable</code> is a type of <code>ClassResolver</code> that
 * corresponds to a particular source file.
 * <p>
 * It has a set of package and class imports, which caches the results of
 * lookups for future reference.
 */
public class ImportTable implements Resolver {
    protected TypeSystem ts;
    /** A list of all package imports. */
    protected List<String> packageImports;
    /** Map from names to classes found, or to the NOT_FOUND object. */
    protected Map<String, Named> map;
    /** List of class imports which will be lazily added to the table at the
     * next lookup. */
    protected List<String> lazyImports;
    /** Parallel list of positions for lazyImports. */
    protected List<Position> lazyImportPositions;
    /** List of explicitly imported classes added to the table or pending in
     * the lazyImports list. */
    protected List<String> classImports;
    /** Source name to use for debugging and error reporting */
    protected String sourceName;
    /** Position to use for error reporting */
    protected Position sourcePos;
    /** Our package */
    protected Package pkg;

    protected static final Named NOT_FOUND = new Named() {
        @Override
        public boolean isCanonical() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public TypeSystem typeSystem() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Position position() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean equalsImpl(TypeObject t) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Object copy() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String name() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String fullName() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    /**
     * Create an import table.
     * @param ts The type system
     * @param pkg The package of the source we are importing types into.
     */
    public ImportTable(TypeSystem ts, Package pkg) {
        this(ts, pkg, null);
    }

    /**
     * Create an import table.
     * @param ts The type system
     * @param pkg The package of the source we are importing types into.
     * @param src The name of the source file we are importing into.
     */
    public ImportTable(TypeSystem ts, Package pkg, String src) {
        this.ts = ts;
        this.sourceName = src;
        this.sourcePos = src != null ? new Position(null, src) : null;
        this.pkg = pkg;

        this.map = new HashMap<String, Named>();
        this.packageImports = new ArrayList<String>();
        this.lazyImports = new ArrayList<String>();
        this.lazyImportPositions = new ArrayList<Position>();
        this.classImports = new ArrayList<String>();
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
        addClassImport(className, null);
    }

    /**
     * Add a class import.
     */
    public void addClassImport(String className, Position pos) {
        if (Report.should_report(TOPICS, 2))
            Report.report(2, this + ": lazy import " + className);

        lazyImports.add(className);
        lazyImportPositions.add(pos);
        classImports.add(className);
    }

    /**
     * Add a package import.
     */
    public void addPackageImport(String pkgName, Position pos) {
        // pos ignored since it's never used
        addPackageImport(pkgName);
    }

    /**
     * Add a package import.
     */
    public void addPackageImport(String pkgName) {
        // don't add the import if it is the same as the current package,
        // the same as a default import, or has already been imported
        if ((pkg != null && pkg.fullName().equals(pkgName))
                || ts.defaultPackageImports().contains(pkgName)
                || packageImports.contains(pkgName)) {
            return;
        }

        packageImports.add(pkgName);
    }

    /**
     * List the packages we import from.
     */
    public List<String> packageImports() {
        return packageImports;
    }

    /**
     * List the classes explicitly imported.
     */
    public List<String> classImports() {
        return classImports;
    }

    /**
     * The name of the source file we are importing into.
     */
    public String sourceName() {
        return sourceName;
    }

    /**
     * Find a type by name, using the cache and the outer resolver,
     * but not the import table.
     */
    protected Named cachedFind(String name) throws SemanticException {
        Named res = map.get(name);

        if (res != null) {
            return res;
        }

        Named t = ts.systemResolver().find(name);
        map.put(name, t);
        return t;
    }

    /**
     * Find a type by name, searching the import table.
     */
    @Override
    public Named find(String name) throws SemanticException {
        if (Report.should_report(TOPICS, 2))
            Report.report(2, this + ".find(" + name + ")");

        /* First add any lazy imports. */
        lazyImport();

        if (!StringUtil.isNameShort(name)) {
            // The name was long.
            return ts.systemResolver().find(name);
        }

        // The class name is short.
        // First see if we have a mapping already.
        Named res = map.get(name);

        if (res != null) {
            if (res == NOT_FOUND) {
                throw new NoClassException(name, sourcePos);
            }
            return res;
        }

        try {
            if (pkg != null) {
                // check if the current package defines it.
                // If so, this takes priority over the package imports (or 
                // "type-import-on-demand" declarations as they are called in
                // the JLS), so even if another package defines the same name,
                // there is no conflict. See Section 6.5.2 of JLS, 2nd Ed.
                Named n = findInPkg(name, pkg.fullName());
                if (n != null) {
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, this + ".find(" + name
                                + "): found in current package");

                    // Memoize the result.
                    map.put(name, n);
                    return n;
                }
            }

            List<String> imports =
                    new ArrayList<String>(packageImports.size() + 5);

            imports.addAll(ts.defaultPackageImports());
            imports.addAll(packageImports);

            // It wasn't a ClassImport.  Maybe it was a PackageImport?
            Named resolved = null;
            for (String pkgName : imports) {
                Named n = findInPkg(name, pkgName);
                if (n != null) {
                    if (resolved == null) {
                        // This is the first occurrence of name we've found
                        // in a package import.
                        // Record it, and keep going, to see if there
                        // are any conflicts.
                        resolved = n;
                    }
                    else {
                        // This is the 2nd occurrence of name we've found
                        // in an imported package.
                        // That's bad.
                        throw new SemanticException("Reference to \"" + name
                                + "\" is ambiguous; both "
                                + resolved.fullName() + " and " + n.fullName()
                                + " match.");
                    }
                }
            }

            if (resolved == null) {
                // The name was short, but not in any imported class or package.
                // Check the null package.
                resolved = ts.systemResolver().find(name); // may throw exception

                if (!isVisibleFrom(resolved, "")) {
                    // Not visible.
                    throw new NoClassException(name, sourcePos);
                }
            }

            // Memoize the result.
            if (Report.should_report(TOPICS, 3))
                Report.report(3, this + ".find(" + name + "): found as "
                        + resolved.fullName());
            map.put(name, resolved);
            return resolved;
        }
        catch (NoClassException e) {
            // memoize the no class exception
            if (Report.should_report(TOPICS, 3))
                Report.report(3, this + ".find(" + name + "): didn't find it");
            map.put(name, NOT_FOUND);
            throw e;
        }
    }

    protected Named findInPkg(String name, String pkgName)
            throws SemanticException {
        String fullName = pkgName + "." + name;

        try {
            Named n = ts.systemResolver().find(fullName);

            // Check if the type is visible in this package.
            if (isVisibleFrom(n, pkgName)) {
                return n;
            }
        }
        catch (NoClassException ex) {
            // Do nothing.
        }
        /*
        try {
            Named n = ts.systemResolver().find(pkgName);

            if (n instanceof ClassType) {
                n = ((ClassType) n).resolver().find(name);
                return n;
            }
        }
        catch (NoClassException ex) {
            // Do nothing.
        }
         */

        return null;
    }

    /**
     * Return whether <code>n</code> in package <code>pkgName</code> is visible from within
     * package <code>pkg</code>.  The empty string may
     * be passed in to represent the default package.
     */
    protected boolean isVisibleFrom(Named n, String pkgName) {
        boolean isVisible = false;
        boolean inSamePackage =
                this.pkg != null && this.pkg.fullName().equals(pkgName)
                        || this.pkg == null && pkgName.equals("");
        if (n instanceof Type) {
            Type t = (Type) n;
            //FIXME: Assume non-class types are always visible.
            isVisible =
                    !t.isClass() || t.toClass().flags().isPublic()
                            || inSamePackage;
        }
        else {
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
            String longName = lazyImports.get(i);

            if (Report.should_report(TOPICS, 2))
                Report.report(2, this + ": import " + longName);

            try {
                lazyImportLongName(longName);
            }
            catch (SemanticException e) {
                System.err.println("  foo " + e.getClass());
                if (e.position == null) {
                    e.position = lazyImportPositions.get(i);
                }
                if (e.position == null) {
                    e.position = sourcePos;
                }

                throw e;
            }
        }

        lazyImports = new ArrayList<String>();
        lazyImportPositions = new ArrayList<Position>();
    }

    /**
     * Try to import the class longName
     * @param longName
     * @throws SemanticException
     */
    protected void lazyImportLongName(String longName) throws SemanticException {
        try {
            // first try finding the long name. If it works, great! It's nice and simple
            Named t = ts.systemResolver().find(longName);
            String shortName = StringUtil.getShortNameComponent(longName);
            map.put(shortName, t);
            return;
        }
        catch (NoClassException e) {
            // didn't find it
        }
        // The class may be a static member class of another, 
        lazyImportLongNameStaticMember(longName);
    }

    /**
     * The class longName may be a static nested class. Try to import it.
     * @param longName
     * @throws SemanticException
     */
    protected void lazyImportLongNameStaticMember(String longName)
            throws SemanticException {
        // Try to find the shortest prefix of longName that is a class

        StringTokenizer st = new StringTokenizer(longName, ".");
        StringBuffer name = new StringBuffer();

        Named t = null;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (name.length() > 0) {
                name.append(".");
            }
            name.append(s);

            try {
                t = cachedFind(name.toString());
            }
            catch (NoClassException e) {
                if (!st.hasMoreTokens()) {
                    // no more types to try to find.
                    throw e;
                }
            }
            if (t != null) {
                // we found a type t!
                if (!st.hasMoreTokens()) {
                    // this is the one we were looking for!
                    break;
                }

                // We now have a type t, and we need to navigate to the appropriate nested class
                while (st.hasMoreTokens()) {
                    String n = st.nextToken();

                    if (t instanceof ClassType) {
                        // If we find a class that is further qualfied,
                        // search for member classes of that class.
                        ClassType ct = (ClassType) t;
                        t = ct.resolver().find(n);
                        if (t instanceof ClassType) {
                            // map.put(n, t); SC: no need to make n to the type.
                        }
                        else {
                            // In JL, the result must be a class.
                            throw new NoClassException(n, ct);
                        }
                    }
                    else if (t instanceof Package) {
                        Package p = (Package) t;
                        t = p.resolver().find(n);
                        if (t instanceof ClassType) {
                            // map.put(n, p); SC: no need to map n to the type
                        }
                    }
                    else {
                        // t, whatever it is, is further qualified, but 
                        // should be, at least in Java, a ClassType.
                        throw new InternalCompilerError("Qualified type \"" + t
                                + "\" is not a class type.", sourcePos);
                    }

                }
            }
        }

        if (t == null) {
            // couldn't find it, so throw an exception be executing the find again.
            cachedFind(longName);
        }

        // at this point, we found it, and it is in t!
        String shortName = StringUtil.getShortNameComponent(longName);

        if (Report.should_report(TOPICS, 2))
            Report.report(2, this + ": import " + shortName + " as " + t);

        if (map.containsKey(shortName)) {
            Named s = map.get(shortName);

            if (!ts.equals(s, t)) {
                throw new SemanticException("Class " + shortName
                                                    + " already defined as "
                                                    + map.get(shortName),
                                            sourcePos);
            }
        }

        // map.put(longName, t); // should already be in the cache
        map.put(shortName, t);
    }

    @Override
    public String toString() {
        if (sourceName != null) {
            return "(import " + sourceName + ")";
        }
        else {
            return "(import)";
        }
    }

    private static final Collection<String> TOPICS =
            CollectionUtil.list(Report.types, Report.resolver, Report.imports);

}
