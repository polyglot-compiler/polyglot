package jltools.main;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import jltools.frontend.ExtensionInfo;
import jltools.frontend.StandardExtensionInfo;

/** This object encapsulates various jltools options. Extensions to
    jltools must define their own objects for encapsulating options. */
public final class Options {
  public Collection source_path; // List[String]
  public File output_directory;
  public String source_ext = null; // e.g., java, jl, pj
  public String output_ext = "java"; // java, by default
  public boolean output_stdout = false; // whether to output to stdout
  public String post_compiler;
    // compiler to run on java output file

  public int output_width = 120;
  public boolean fully_qualified_names = false;

  /** Inject type information in serialized form into output file? */
  public boolean serialize_type_info = true;

  public ExtensionInfo extension = new StandardExtensionInfo();
     // The extension information

  public static Map report = new HashMap(); // Map[String, Integer]

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
}
