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
  public Collection source_path; // Set[String]
  public File output_directory;
  public String source_ext = "java"; // e.g., java, jl, pj
  public String output_ext = "java"; // java, by default
  public boolean output_stdout = false; // whether to output to stdout
  public String post_compiler = "javac";
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
    source_path = new LinkedList();
    source_path.add(new File("."));
  }
}
