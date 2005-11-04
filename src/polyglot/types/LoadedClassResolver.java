package polyglot.types;

import java.io.InvalidClassException;
import java.util.*;

import polyglot.frontend.Scheduler;
import polyglot.frontend.SchedulerException;
import polyglot.main.Report;
import polyglot.main.Version;
import polyglot.types.reflect.*;
import polyglot.util.CollectionUtil;
import polyglot.util.TypeEncoder;
import polyglot.util.InternalCompilerError;

/**
 * Loads class information from class files, or serialized class infomation
 * from within class files.  It does not load from source files.
 */
public class LoadedClassResolver implements TopLevelResolver
{
  protected final static int NOT_COMPATIBLE = -1;
  protected final static int MINOR_NOT_COMPATIBLE = 1;
  protected final static int COMPATIBLE = 0;

  TypeSystem ts;
  TypeEncoder te;
  ClassPathLoader loader;
  Version version;
  Set nocache;
  boolean allowRawClasses;

  final static Collection report_topics = CollectionUtil.list(
    Report.types, Report.resolver, Report.loader);

  /**
   * Create a loaded class resolver.
   * @param ts The type system
   * @param classpath The class path
   * @param loader The class file loader to use.
   * @param version The version of classes to load.
   * @param allowRawClasses allow class files without encoded type information 
   */
  public LoadedClassResolver(TypeSystem ts, String classpath,
                             ClassFileLoader loader, Version version,
                             boolean allowRawClasses)
  {
    this.ts = ts;
    this.te = new TypeEncoder(ts);
    this.loader = new ClassPathLoader(classpath, loader);
    this.version = version;
    this.nocache = new HashSet();
    this.allowRawClasses = allowRawClasses;
  }

  public boolean allowRawClasses() {
    return allowRawClasses;
  }

  public boolean packageExists(String name) {
    return loader.packageExists(name);
  }

  /**
   * Load a class file for class <code>name</code>.
   */
  protected ClassFile loadFile(String name) {
    if (nocache.contains(name)) {
        return null;
    }
    
    try {
        ClassFile clazz = loader.loadClass(name);

        if (clazz == null) {
            if (Report.should_report(report_topics, 4)) {
                Report.report(4, "Class " + name + " not found in classpath "
                        + loader.classpath());
            }
        }
        else {
            if (Report.should_report(report_topics, 4)) {
                Report.report(4, "Class " + name + " found in classpath "
                        + loader.classpath());
            }
            return clazz;
        }
    }
    catch (ClassFormatError e) {
        if (Report.should_report(report_topics, 4))
            Report.report(4, "Class " + name + " format error");
    }

    nocache.add(name);

    return null;
  }

  /**
   * Find a type by name.
   */
  public Named find(String name) throws SemanticException {
    if (Report.should_report(report_topics, 3))
      Report.report(3, "LoadedCR.find(" + name + ")");

    // First try the class file.
    ClassFile clazz = loadFile(name);
    if (clazz == null) {
        throw new NoClassException(name);
    }

    // Check for encoded type information.
    if (clazz.encodedClassType(version.name()) != null) {
      if (Report.should_report(report_topics, 4))
	Report.report(4, "Using encoded class type for " + name);
      return getEncodedType(clazz, name);
    }
    
    if (allowRawClasses) {
      if (Report.should_report(report_topics, 4))
	Report.report(4, "Using raw class file for " + name);
      return new ClassFileLazyClassInitializer(clazz, ts).type();
    }

    // We have a raw class, but are not allowed to use it, and
    // cannot find appropriate encoded info. 
    throw new SemanticException("Unable to find a suitable definition of \""
        + name +"\". A class file was found,"
        + " but it did not contain appropriate information for this" 
        + " language extension. If the source for this file is written"
        + " in the language extension, try recompiling the source code.");
    
  }

  boolean recursive = false;

  /**
   * Extract an encoded type from a class file.
   */
  protected ClassType getEncodedType(ClassFile clazz, String name)
    throws SemanticException
  {
    // At this point we've decided to go with the Class. So if something
    // goes wrong here, we have only one choice, to throw an exception.

    // Check to see if it has serialized info. If so then check the
    // version.
    FieldInstance field;
    
    int comp = checkCompilerVersion(clazz.compilerVersion(version.name()));

    if (comp == NOT_COMPATIBLE) {
      throw new SemanticException("Unable to find a suitable definition of "
                                  + clazz.name()
                                  + ". Try recompiling or obtaining "
                                  + " a newer version of the class file.");
    }

    // Alright, go with it!
    TypeObject dt;
    SystemResolver oldResolver = null;

    if (Report.should_report(Report.serialize, 1))
        Report.report(1, "Saving system resolver");
    oldResolver = ts.saveSystemResolver();

    boolean okay = false;

    boolean oldRecursive = recursive;

    if (! recursive) {
        ts.systemResolver().clearAdded();
    }

    recursive = true;
    
    try {
        try {
            if (Report.should_report(Report.serialize, 1))
                Report.report(1, "Decoding " + name + " in " + clazz);
            
            dt = te.decode(clazz.encodedClassType(version.name()), name);
            
            if (dt == null) {
                if (Report.should_report(Report.serialize, 1))
                    Report.report(1, "* Decoding " + name + " failed");
                
                // Deserialization failed because one or more types could not
                // be resolved.  Abort this pass.  Dependencies have already
                // been set up so that this goal will be reattempted after
                // the types are resolved.
                throw new SchedulerException("Could not decode " + name);
            }
        }
        catch (InternalCompilerError e) {
            throw e;
        }
        catch (InvalidClassException e) {
            throw new BadSerializationException(clazz.name());
        }
        
        if (dt instanceof ClassType) {
            ClassType ct = (ClassType) dt;
            // Install the decoded type into the *new* system resolver.
            // It will be installed into the old resolver below by putAll.
            ts.systemResolver().addNamed(name, ct);
            
            if (Report.should_report(Report.serialize, 1))
                Report.report(1, "* Decoding " + name + " succeeded");
            
            if (Report.should_report(Report.serialize, 2)) {
                LazyInitializer init = null;

                // Save and restore the initializer to print the members.
                // We can't access the members of ct until after we return from
                // the resolver because the initializer may set up goals on ct,
                // which may get discarded because of a missing dependency.
                if (ct instanceof ParsedClassType) {
                    ParsedClassType pct = (ParsedClassType) ct;
                    init = pct.initializer();
                    pct.setInitializer(new LazyClassInitializer() {
                        public boolean fromClassFile() { return false; }
                        public void setClass(ParsedClassType ct) { }
                        public void initTypeObject() { }
                        public boolean isTypeObjectInitialized() { return true; }
                        public void initSuperclass() { }
                        public void initInterfaces() { }
                        public void initMemberClasses() { }
                        public void initConstructors() { }
                        public void initMethods() { }
                        public void initFields() { }
                        public void canonicalConstructors() { }
                        public void canonicalMethods() { }
                        public void canonicalFields() { }
                    });
                }

                for (Iterator i = ct.methods().iterator(); i.hasNext(); ) {
                    MethodInstance mi = (MethodInstance) i.next();
                    Report.report(2, "* " + mi);
                }
                for (Iterator i = ct.fields().iterator(); i.hasNext(); ) {
                    FieldInstance fi = (FieldInstance) i.next();
                    Report.report(2, "* " + fi);
                }
                for (Iterator i = ct.constructors().iterator(); i.hasNext(); ) {
                    ConstructorInstance ci = (ConstructorInstance) i.next();
                    Report.report(2, "* " + ci);
                }

                if (ct instanceof ParsedClassType) {
                    ParsedClassType pct = (ParsedClassType) ct;
                    pct.setInitializer(init);
                }
            }

            if (Report.should_report(report_topics, 2))
                Report.report(2, "Returning serialized ClassType for " +
                              clazz.name() + ".");

            okay = true;
            return ct;
        }
        else {
            throw new SemanticException("Class " + name + " not found in " + clazz.name() + ".");
        }
    }
    finally {
        recursive = oldRecursive;

        if (okay) {
            if (Report.should_report(Report.serialize, 1))
                Report.report(1, "Deserialization successful.  Installing " + ts.systemResolver().justAdded() + " into restored system resolver.");

            oldResolver.putAll(ts.systemResolver());
        }
        else {
            if (Report.should_report(Report.serialize, 1))
                Report.report(1, "Deserialization failed.  Restoring previous system resolver.");
            if (Report.should_report(Report.serialize, 1))
                Report.report(1, "Discarding " + ts.systemResolver().justAdded());
        }

        ts.restoreSystemResolver(oldResolver);
    }
  }

  /**
   * Compare the encoded type's version against the loader's version.
   */
  protected int checkCompilerVersion(String clazzVersion) {
    if (clazzVersion == null) {
      return NOT_COMPATIBLE;
    }

    StringTokenizer st = new StringTokenizer(clazzVersion, ".");

    try {
      int v;
      v = Integer.parseInt(st.nextToken());
      Version version = this.version;

      if (v != version.major()) {
	// Incompatible.
	return NOT_COMPATIBLE;
      }

      v = Integer.parseInt(st.nextToken());

      if (v != version.minor()) {
	// Not the best option, but will work if its the only one.
	return MINOR_NOT_COMPATIBLE;
      }
    }
    catch (NumberFormatException e) {
      return NOT_COMPATIBLE;
    }

    // Everything is way cool.
    return COMPATIBLE;
  }
}
