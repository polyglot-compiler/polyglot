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
 * Loads class information from source files, class files, or serialized
 * class infomation from within class files. An outline of the steps is
 * given below.
 *
 * <ol>
 * <li> When the polyglot translator looks for a class by the name
 * "foo.bar.Quux" it first searches for that class in any file given
 * on the command line. If the class is found one of these files, then
 * this definition is used and the remainder of the steps are
 * skipped.
 *
 * <li>If none of these files contain the desired class, then the source
 * path is searched  next. For example, if the source extension is
 * ".jl" and the source path is "mydir:." then the translator looks
 * for files "mydir/foo/bar/Quux.jl" and "./foo/bar/Quux.jl". (The
 * source path may be set using the -S options, see above.)
 *
 * <li> Regardless of whether or not a source file is found, the translator
 * searches the classpath (defined as normal through the environment
 * and command-line options to the interpreter) for the desired class.
 *
 * <li>If no source file exists, and no class is found then an error is
 * reported (skipping the rest of the steps below).
 *
 * <li>If a source file is found, but no class, then the source file is
 * parsed. If it contains the desired class definition (which it
 * should) then that definition is used and the remainder of the steps
 * are skipped. (If it does not contain this definition, an error is
 * reported and the remainder of the steps are skipped.
 *
 * <li>If a class is found but no source file, then the class is examined
 * for jlc class type information. If the class contains no class type
 * information (this is the case if the class file was compiled from
 * raw Java source rather than jlc translated output) then this class
 * is used as the desired class definition (skipping all steps below).
 *
 * <li>(class, but no still no source) If the class does contain jlc class
 * type information, then the version number of translator used to
 * translate the source which created the given class file is compared
 * against the version of the current instantiation of the translator.
 * If the versions are compatible, then the jlc class type information
 * is used as the desired definiton. If the versions are incompatible
 * (see the documentation in Compiler.java) then an error is reported.
 * In either case, all remaining steps are skipped.
 *
 * <li>If both a suitable source file and class are found then we have a
 * choice. If the class definition does not contain jlc class type
 * information then the source file is parsed as the definition found
 * in this file is used as desired definiton and we stop here. If the
 * class does contain jlc class type information, then continue.
 *
 * <li>(source and class with jlc info) Next the last modification date of
 * the source file is compared to the last modification date of the
 * source file used to generate the class file. If the source file is
 * more recent, the it is parsed as used as the desired definition and
 * all remaining steps are skipped.
 *
 * <li>(source and class with jlc info) Next the jlc version of the class
 * and of the current translator are compared (as in 7.). If the
 * verisions are incompatible, then we use the definition from the
 * parsed source file. If the versions are compatible, then we use
 * the definition given by the jlc class type information.
 * </ol>
 * Finally, if at any point an error occurs while reading jlc class type
 * information (e.g. if this information exists but is corrupted), then
 * an error is reported.
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

  public LoadedClassResolver(TypeSystem ts, String classpath, ClassFileLoader loader, Version version)
  {
    this.ts = ts;
    this.te = new TypeEncoder(ts);
    this.loader = new ClassPathLoader(classpath, loader);
    this.version = version;
    this.nocache = new HashSet();
  }

  protected ClassFile loadFile(String name) throws SemanticException {
    if (! nocache.contains(name)) {
      try {
        ClassFile clazz = loader.loadClass(name);

        Types.report(4, "Class " + name + " found in classpath " +
                    loader.classpath());

        return clazz;
      }
      catch (ClassNotFoundException e) {
        Types.report(4, "Class " + name + " not found in classpath " +
                    loader.classpath());
      }
      catch (ClassFormatError e) {
        Types.report(4, "Class " + name + " format error");
      }
    }

    nocache.add(name);

    throw new NoClassException("Class " + name + " not found.");
  }

  public Type findType(String name) throws SemanticException {
    Types.report(3, "LoadedCR.findType(" + name + ")");

    // First try the class file.
    ClassFile clazz = loadFile(name);

    // Check for encoded type information.
    if (clazz.encodedClassType(version.name()) != null) {
      Types.report(4, "Using encoded class type for " + name);
      return getEncodedType(clazz, name);
    }
    else {
      Types.report(4, "Using raw class file for " + name);
      return clazz.type(ts);
    }
  }

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
