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
public class LoadedClassResolver extends ClassResolver implements TopLevelResolver
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
    
    try {
        dt = te.decode(clazz.encodedClassType(version.name()));
        
        if (dt == null) {
            // Deserialization failed because one or more types could not
            // be resolved.  Abort this pass.  Dependencies have already
            // been set up so that this goal will be reattempted after
            // the types are resolved.
            throw new SchedulerException("Could not decode " + name);
        }
    }
    catch (InternalCompilerError e) {
	System.err.println("Failed decoding " + clazz.name());
	throw e;
    }
    catch (InvalidClassException e) {
        throw new BadSerializationException(clazz.name());
    }

    if (dt instanceof ClassType) {
        // Put the decoded type into the resolver to avoid circular resolving.
        ts.systemResolver().addNamed(name, (ClassType) dt);
    
        if (Report.should_report(report_topics, 2))
            Report.report(2, "Returning serialized ClassType for " +
                          clazz.name() + ".");
        
        return (ClassType) dt;
    }
    else {
        throw new SemanticException("Class " + name + " not found in " + clazz.name() + ".");
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
