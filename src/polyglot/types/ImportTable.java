package jltools.types;

import jltools.util.*;

import java.util.*;


/**
 * An <code>ImportTable</code> is a type of <code>ClassResolver</code> that
 * corresponds to a particular source file.
 * <p>
 * It has a set of package and class imports, which caches the results of
 * lookups for future reference.
 */
public  class ImportTable implements ClassResolver 
{
  /** The underlying resolver. */
  protected ClassResolver resolver;
  /** Determines whether this table caches classes. */
  protected boolean caching;
  /** A list of all package imports. */
  protected Set packageImports;
  /** Map from names to classes found. */
  protected Map map;
  /** List of imports which will be lazily added to the table. */
  protected List lazyImports;
  /** Used to report errors for lazily added imports. */
  protected ErrorQueue eq;

  // DOCME
  public ImportTable( ClassResolver base, boolean caching, ErrorQueue eq)
  {
    resolver = base;
    this.caching = caching;
    this.eq = eq;

    map = new HashMap();
    packageImports = new HashSet();
    lazyImports = new ArrayList();
  }

  public void addClassImport( String className) throws SemanticException 
  {
    lazyImports.add( className);
  }
  
  public void addPackageImport( String pkgName) throws NoClassException {
    resolver.findPackage(pkgName);
    packageImports.add(pkgName);
  }
    
  public void findPackage( String name) throws NoClassException {
    resolver.findPackage(name);
  }

  public ClassType findClass( String name)  throws SemanticException {
    // FIXME: need to keep on looking to find conflicts.

    /* First add any lazy imports. */
    if( lazyImports.size() > 0) {
      for( Iterator iter = lazyImports.iterator(); iter.hasNext(); ) {
        String longName = (String)iter.next();
        //        System.out.println( "lazily adding: " + longName);
        try 
        {
          ClassType clazz = resolver.findClass( longName);
          String shortName = TypeSystem.getShortNameComponent( longName);

          map.put( longName, clazz);
          map.put( shortName, clazz);    
        }
        catch( NoClassException e)
        {
          eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), -1);
        }
      }

      lazyImports = new ArrayList();
    }

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
}
