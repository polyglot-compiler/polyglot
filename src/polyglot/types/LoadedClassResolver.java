package polyglot.types;

import polyglot.frontend.*;
import polyglot.frontend.Compiler;
import polyglot.ast.Node;
import polyglot.visit.ClassSerializer;
import polyglot.util.*;
import polyglot.main.Main;
import polyglot.main.Version;
import polyglot.types.reflect.*;

import java.io.*;
import java.util.*;

/**
 * Loads class information from class files, or serialized class infomation
 * from within class files.  It does not load from source files.
 */
public class LoadedClassResolver extends ClassResolver
{
  protected final static int NOT_COMPATIBLE = -1;
  protected final static int MINOR_NOT_COMPATIBLE = 1;
  protected final static int COMPATIBLE = 0;

  TypeSystem ts;
  TypeEncoder te;
  ClassPathLoader loader;
  Version version;
  Set nocache;

  /**
   * Create a loaded class resolver.
   * @param ts The type system
   * @param classpath The class path
   * @param loader The class file loader to use.
   * @param version The version of classes to load.
   */
  public LoadedClassResolver(TypeSystem ts, String classpath, ClassFileLoader loader, Version version)
  {
    this.ts = ts;
    this.te = new TypeEncoder(ts);
    this.loader = new ClassPathLoader(classpath, loader);
    this.version = version;
    this.nocache = new HashSet();
  }

  /**
   * Load a class file for class <code>name</code>.
   */
  protected ClassFile loadFile(String name) throws SemanticException {
    if (! nocache.contains(name)) {
      try {
        ClassFile clazz = loader.loadClass(name);

        if (Types.should_report(4))
	    Types.report(4, "Class " + name + " found in classpath " +
                    loader.classpath());

        return clazz;
      }
      catch (ClassNotFoundException e) {
        if (Types.should_report(4))
	    Types.report(4, "Class " + name + " not found in classpath " +
                    loader.classpath());
      }
      catch (ClassFormatError e) {
        if (Types.should_report(4))
	    Types.report(4, "Class " + name + " format error");
      }
    }

    nocache.add(name);

    throw new NoClassException("Class " + name + " not found.");
  }

  /**
   * Find a type by name.
   */
  public Type findType(String name) throws SemanticException {
    if (Types.should_report(3))
	Types.report(3, "LoadedCR.findType(" + name + ")");

    // First try the class file.
    ClassFile clazz = loadFile(name);

    // Check for encoded type information.
    if (clazz.encodedClassType(version.name()) != null) {
      if (Types.should_report(4))
	Types.report(4, "Using encoded class type for " + name);
      return getEncodedType(clazz, name);
    }
    else {
      if (Types.should_report(4))
	Types.report(4, "Using raw class file for " + name);
      return clazz.type(ts);
    }
  }

  /**
   * Extract an encoded type from a class file.
   */
  protected Type getEncodedType(ClassFile clazz, String name)
    throws SemanticException
  {
    // At this point we've decided to go with the Class. So if something
    // goes wrong here, we have only one choice, to throw an exception.
    try {
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
      ClassType dt = (ClassType) te.decode(clazz.encodedClassType(version.name()));

      //HACK: storing median result to avoid circular resolving

      ((CachingResolver) ts.systemResolver()).medianResult(name, dt);

      if (Types.should_report(2))
	Types.report(2, "Returning serialized ClassType for " +
		  clazz.name() + ".");

      return (ClassType) dt.restore();
    }
    catch (SemanticException e) {
      e.printStackTrace();
      throw e;
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new SemanticException("Could not get type information for " +
                                  "class \"" + clazz.name() + "\"; " +
                                  e.getClass().getName() + ": " +
                                  e.getMessage());
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
