/*
 * Type.java
 */

package jltools.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * ImportTable
 *
 * An ImportTable is a ClassResolver.  It has a set of package and
 * class imports, which caches the results of lookups for future
 * reference.
 **/
public  class ImportTable implements ClassResolver {
  // DOCME
  public ImportTable(ClassResolver base, boolean shouldCache) {
    resolver = base;
    caching = shouldCache;

    map = new HashMap();
    packageImports = new HashSet();
  }

  public void dump()
  {
    Iterator iter = map.keySet().iterator();
    while( iter.hasNext()) {
      System.out.println( "import " + (String)iter.next());
    }

    iter = packageImports.iterator();
    while( iter.hasNext()) {
      System.out.println( "import " + (String)iter.next() + ".*");
    }
  }

  public void addClassImport(String className) throws TypeCheckException {
    ClassType class_ = resolver.findClass(className);
    String shortName = TypeSystem.getShortNameComponent(className);
    map.put(className, class_);
    map.put(shortName, class_);    
  }
  
  public void addPackageImport(String pkgName) throws NoClassException {
    resolver.findPackage(pkgName);
    packageImports.add(pkgName);
  }
    
  public void findPackage(String name) throws NoClassException {
    resolver.findPackage(name);
  }

  public ClassType findClass(String name)  throws TypeCheckException {
    // FIXME: need to keep on looking to find conflicts.
    //System.out.println( "looking " + name + " " + TypeSystem.isNameShort(name));
    if (TypeSystem.isNameShort(name)) {
      Object res = map.get(name);
      // First see if we have a mapping already.
      if (res != null) {
	return (ClassType) res;
      } 
      // It wasn't a ClassImport.  Maybe it was a PackageImport?
      for (Iterator iter = packageImports.iterator(); iter.hasNext(); ) {
	String pkgName = (String) iter.next();
	String fullName = pkgName + "." + name;
	try {
	  ClassType class_ = resolver.findClass(fullName);
          if( caching) {
            map.put(name, class_);
          }
	  return class_;
	} catch (NoClassException ex) { /* Do nothing. */ }
      }
      // The name was short, but not in any imported class or package.
      // Check the null package.
      ClassType class_ = resolver.findClass(name); // may throw exception
      if( caching) {
        map.put(name,class_);
      }
      return class_;
    } 
    // The name was long.
    return resolver.findClass(name);
  }

  // The underlying resolver.
  ClassResolver resolver;
  // Should this table cache classes.
  boolean caching;
  // A list of all package imports.
  Set packageImports;
  // Map from names to classes found.
  Map map;
}
