package jltools.main;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import jltools.frontend.ExtensionInfo;

/** This object encapsulates various jltools options. Extensions to
    jltools must define their own objects for encapsulating options. */
public final class Options {
  /** The global object containing compiler options.  Access to compiler
      options via the Compiler object, rather than through this static
      variable, is encouraged for future extensibility. */
  public static Options global = new Options();

  public int error_count = 100;
  public Collection source_path; // List[String]
  public File output_directory;
  public String classpath = System.getProperty("java.class.path") +
      File.pathSeparator + System.getProperty("sun.boot.class.path");
  public String source_ext = null; // e.g., java, jl, pj
  public String output_ext = "java"; // java, by default
  public boolean output_stdout = false; // whether to output to stdout
  public boolean no_source_check = false;
    // If a class file is available for a type, use it, even if a newer
    // source is available.  This is not safe with some extensions
  public String post_compiler;
    // compiler to run on java output file

  public int output_width = 120;
  public boolean fully_qualified_names = false;

  /** Inject type information in serialized form into output file? */
  public boolean serialize_type_info = true;

  /** Dump the AST? */
  public boolean dump_ast = false;

  /** keep output files */
  public boolean keep_output_files = true;

  public ExtensionInfo extension = new jltools.ext.jl.ExtensionInfo();
     // The extension information

  public static Map report = new HashMap(); // Map[String, Integer]

  public int level(String name) {
    Object i = report.get(name);
    if (i == null) return 0;
    else return ((Integer)i).intValue();
  }

/** Initialization of defaults */
  {
    String java_home = System.getProperty("java.home");
    String current_dir = System.getProperty("user.dir");

    source_path = new LinkedList();
    source_path.add(new File(current_dir));

    output_directory = new File(current_dir);

    // First try: $JAVA_HOME/../bin/javac
    // This should work with JDK 1.2 and 1.3
    //
    // If not found, try: $JAVA_HOME/bin/javac
    // This should work for JDK 1.1.
    //
    // If neither found, assume "javac" is in the path.
    //
    post_compiler = java_home + File.separator + ".." + File.separator +
			"bin" + File.separator + "javac";

    if (! new File(post_compiler).exists()) {
      post_compiler = java_home + File.separator +
			  "bin" + File.separator + "javac";

      if (! new File(post_compiler).exists()) {
	post_compiler = "javac";
      }
    }
  }

  public void usage() {
    String fileext = extension.fileExtension();

    System.err.println("usage: " + extension.compilerName() + " [options] " +
                        "<source-file>." + fileext + " ...\n");
    System.err.println("where [options] includes:");
    System.err.println(" -d <directory>          output directory");
    System.err.println(" -sourcepath <path list> source path");
    System.err.println(" -fqcn                   use fully-qualified class"
                        + " names");
    System.err.println(" -sx <ext>               set source extension");
    System.err.println(" -ox <ext>               set output extension");
    System.err.println(" -dump                   dump the ast");
    System.err.println(" -scramble [seed]        scramble the ast");
    System.err.println(" -noserial               disable class"
                        + " serialization");
    System.err.println(" -nooutput               delete output files after" +
		       " compilation");
    System.err.println(" -ext <extension>        use language extension");
    System.err.println(" -c                      compile only to .java");
    System.err.println(" -post <compiler>        run javac-like compiler" 
                        + " after translation");
    System.err.println(" -v -verbose             print verbose " 
                        + "debugging information");
    System.err.println(" -report <topic>=<level> print verbose debugging" +
                        " information about topic\n" +
			"                         at specified verbosity");
    System.err.println("   (Allowed topics: " + Report.topics + ")");
    System.err.println(" -version                print version info");
    System.err.println(" -h                      print this message");
    System.err.println();
    System.err.println(extension.options());
  }
}
