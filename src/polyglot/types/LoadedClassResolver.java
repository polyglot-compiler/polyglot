package jltools.types;

import jltools.frontend.*;
import jltools.frontend.Compiler;
import jltools.ast.Node;
import jltools.visit.ClassSerializer;
import jltools.util.*;
import jltools.main.Main;

import java.io.*;
import java.util.*;

/**
 * Loads class information from source files, class files, or serialized
 * class infomation from within class files. An outline of the steps is
 * given below.
 *
 * <ol>
 * <li> When the jltools translator looks for a class by the name
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

  Compiler compiler;
  TypeSystem ts;
  TypeEncoder te;
  boolean checkSource;

  public LoadedClassResolver(Compiler compiler, TypeSystem ts,
	                     boolean no_source_check)
  {
    this.compiler = compiler;
    this.ts = ts;
    this.te = new TypeEncoder(ts);
    this.checkSource = ! no_source_check;
  }

  public Type findType(String name) throws SemanticException
  {
    Types.report(1, "LoadedCR.findType(" + name + ")");

    Class clazz = null;
    Source source = null;

    // First, try and find the source file.
    try {
      source = compiler.sourceLoader().classSource(name);
    }
    catch (IOException e) {
      source = null;
    }

    // Now, try the class file.
    try {
      clazz = Class.forName(name);
    }
    catch (Exception e) {
    }

    boolean useClassFile = clazz != null;

    if (useClassFile && ! checkSource) {
	source = null;
    }

    if (useClassFile && source != null) {
      // Now we have both source and class files. We need to figure out
      // whether or not the class file is up to date and if it contains any
      // jlc information.  If we can use the class file, drop the source.
      // Otherwise drop the class file and use the source.
      try {
	java.lang.reflect.Field field;

	field = clazz.getDeclaredField("jlc$SourceLastModified");
	field.setAccessible(true);

	long classModTime = ((Long) field.get(null)).longValue();
	long sourceModTime = source.lastModified().getTime();

	if (classModTime < sourceModTime) {
	  Types.report(2, "Source file version is newer than compiled for " +
		       name + ".");
	  useClassFile = false;
	}
	else {
	  field = clazz.getDeclaredField("jlc$CompilerVersion");
	  field.setAccessible(true);

	  int comp = checkCompilerVersion((String) field.get(null));

	  if (comp != COMPATIBLE) {
	    // Incompatible or older version, so go with the source.
	    Types.report(2, "Incompatible source file version for " +
			 name + ".");
	    useClassFile = false;
	  }
	}
      }
      catch (Exception e) {
	// System.err.println( "Exception while checking fields: " + e);
	useClassFile = false;
      }
    }

    if (useClassFile) {
      return getTypeFromClass(clazz);
    }

    if (source != null) {
      return getTypeFromSource(source, name);
    }

    throw new NoClassException("Class " + name + " not found.");
  }

  protected Type getTypeFromSource(Source source, String name)
    throws SemanticException
  {
    // Compile the source file just enough to get the type information out.
    try {
      if (compiler.readSource(source)) {
	return compiler.parsedResolver().findType(name);
      }
    }
    catch (IOException e) {
      throw new SemanticException("I/O error while compiling " +
	  source.name() + ": " + e.getMessage());
    }

    throw new SemanticException("Error while compiling " + source.name() + ".");
  }

  protected Type getTypeFromClass(Class clazz)
    throws SemanticException
  {
    // At this point we've decided to go with the Class. So if something
    // goes wrong here, we have only one choice, to throw an exception. */
    try {
      // Check to see if it has serialized info. If so then check the
      // version.
      java.lang.reflect.Field field;

      field = clazz.getDeclaredField("jlc$CompilerVersion");
      field.setAccessible( true);

      int comp = checkCompilerVersion((String) field.get(null));

      if (comp == NOT_COMPATIBLE) {
        throw new SemanticException("Unable to find a suitable definition of "
                                    + clazz.getName()
                                    + ". Try recompiling or obtaining "
                                    + " a newer version of the class file.");
      }

      // Alright, go with it!
      field = clazz.getDeclaredField("jlc$ClassType");
      field.setAccessible( true);

      ClassType t = (ClassType) te.decode((String) field.get(null));

      Types.report(1, "Returning serialized ClassType for " +
		  clazz.getName() + ".");

      return (ClassType) t.restore();
    }
    catch (SemanticException e) {
      throw e;
    }
    catch (NoSuchFieldException e) {
      Types.report(1, "Returning LoadedClassType for " +
				clazz.getName() + ".");
      return ts.forClass(clazz);
    }
    catch( Exception e) {
      throw new SemanticException(e.getMessage());
      /*
      				"There was an error while reading type "
                                  + "information from the class file for \""
                                  + clazz.getName() + "\". Delete the class "
                                  + "file and recompile, or obtain a newer "
                                  + "version of the file.");
				  */
    }
  }

  protected int checkCompilerVersion(String clazzVersion) {
    StringTokenizer st = new StringTokenizer(clazzVersion, ".");

    try {
      int v;
      v = Integer.parseInt(st.nextToken());

      if (v != compiler.options().extension.version().major()) {
	// Incompatible.
	return NOT_COMPATIBLE;
      }

      v = Integer.parseInt(st.nextToken());

      if (v != compiler.options().extension.version().minor()) {
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
