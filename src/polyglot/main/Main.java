package jltools.main;

import jltools.ast.Node;
import jltools.frontend.Compiler;
import jltools.frontend.ExtensionInfo;
import jltools.frontend.StandardExtensionInfo;
import jltools.types.TypeSystem;
import jltools.util.*;

import java.io.*;
import java.util.*;

/** Main is the main program of the extensible compiler. It should not
 * need to be replaced.
 */
public class Main
{
  /** A collection of string names of topics which can be used with the
      -report command-line switch */
  public static Collection report_topics = new HashSet();

  /** Compiler options. Access to compiler options via the Compiler object,
      rather than through this static variable, is encouraged for future
      extensibility. */
  public static Options options = new Options();

  /** Source files specified on the command line */
  private static Set source;

  /** Whether any errors seen yet */
  private boolean hasErrors = false;

  public static final void main(String args[])
  {
    source = new HashSet();
    
    parseCommandLine(args, options, source);

    Compiler compiler = new Compiler(options);

    String targetName = null;
    if (!compiler.compile(source)) System.exit(1);

    Main.report(null, 1, "Output files: " + compiler.outputFiles());

    /* Now call javac or jikes, if necessary. */
    if (options.post_compiler != null && !options.output_stdout) {
      Runtime runtime = Runtime.getRuntime();
      
      Iterator iter = compiler.outputFiles().iterator();
      while(iter.hasNext()) {
	String outfile = (String)iter.next(); 
	
	String command = options.post_compiler + " -classpath " 
	  + options.output_directory + File.pathSeparator
	  + "." + File.pathSeparator
	  + System.getProperty("java.class.path") + " "
	  + outfile;
	
	report(null, 1, "Executing post-compiler " + command);
	
	try 
	{
	  Process proc = runtime.exec(command);
	  
	  InputStreamReader err = 
	    new InputStreamReader(proc.getErrorStream());
	  char[] c = new char[72];
	  int len;
	  while((len = err.read(c)) > 0) {
	    System.err.print(String.valueOf(c, 0, len));
	  }
	  
	  proc.waitFor();
	  if (proc.exitValue() > 0) {
	    System.exit(proc.exitValue());
	  }
	}
	catch(Exception e) 
	{ 
	  System.err.println("Caught Exception while running compiler: "
			      + e.getMessage());
	  System.exit(1);
	}
      }
    }
  }

  /**
   * Return whether a message on <code>topics</code> of obscurity
   * <code>level</code> should be reported, based on the command-line
   * switches given by the user. This method is occasionally useful
   * when the computation of the message to be reported is expensive.
   */
  public static boolean should_report(Collection topics, int level) {
    if (topics == null) {
      Object lvo = options.report.get("verbose");
      if (lvo != null && ((Integer) lvo).intValue() >= level) {
	return true;
      }
    } else {
	for (Iterator i = topics.iterator(); i.hasNext();) {
	    String topic = (String) i.next();
	    Object lvo = options.report.get(topic);
	    if (lvo != null && ((Integer) lvo).intValue() >= level) {
		return true;
	    }
	}
    }
    return false;
  }

  /** This is the standard way to report debugging information in the
   *  compiler.  It conditionally reports a message if it is related to
   *  one of the specified topics. The variable <code>topics</code> is a
   *  collection of strings.  The obscurity of the message is indicated
   *  by <code>level</code>.  The message is reported only if the user
   *  has requested (via the -report command-line option) that messages
   *  of that obscurity be reported for one of the specified topics.
   */
  public static void report(Collection topics, int level, String message) {
    if (should_report(topics,level)) {
	for (int j = 1; j < level; j++) System.err.print("  ");
	System.err.println(message);
    }
  }

  static final void loadExtension(String ext) {
    if (ext != null && ! ext.equals("")) {
      String extClassName = "jltools.ext." + ext + ".ExtensionInfo";

      Class extClass;
      try {
	extClass = Class.forName(extClassName);
      }
      catch (ClassNotFoundException e) {
	System.err.println("Extension " + ext +
	  " not found: could not find class " + extClassName + "." +
	  e.getMessage());
	System.exit(1);
	return;
      }

      try {
	options.extension = (ExtensionInfo) extClass.newInstance();
      }
      catch (ClassCastException e) {
	System.err.println(ext + " is not a valid jltools extension:" +
	    " extension class " + extClassName +
	    " exists but is not a subclass of ExtensionInfo");
	System.exit(1);
      }
      catch (Exception e) {
	System.err.println("Extension " + ext +
	  " could not be loaded: could not instantiate " + extClassName + ".");
	System.exit(1);
      }
    }
  }

  static final void parseCommandLine(String args[], Options options, Set source)
  {
    if(args.length < 1) {
      usage();
      System.exit(1);
    }

    for(int i = 0; i < args.length; )
    {
      if (args[i].equals("-h")) {
        usage();
        System.exit(0);
      }
      else if (args[i].equals("-version")) {
        System.out.println("jltools Compiler version "
                           + Compiler.VERSION_MAJOR + "."
                           + Compiler.VERSION_MINOR + "."
                           + Compiler.VERSION_PATCHLEVEL);
        System.exit(0);
      }
      else if (args[i].equals("-d"))
      {
        i++;
        options.output_directory = new File(args[i]);
        i++;
      }
      else if (args[i].equals("-classpath") ||
               args[i].equals("-cp")) {
	i++;
	String current_path = System.getProperty("java.class.path");
	current_path = args[i] + System.getProperty("path.separator") +
			current_path;
	System.setProperty("java.class.path", current_path);
	System.err.println("Warning: -classpath not implemented\n");
	// Does the system class loader really keep looking at this?
	// No -- this doesn't work
      }
      else if (args[i].equals("-sourcepath"))
      {
        i++;
        StringTokenizer st = new StringTokenizer(args[i], File.pathSeparator);
        while(st.hasMoreTokens())
        {
          options.source_path.add(new File(st.nextToken()));
        }
        i++;
      }
      else if (args[i].equals("-fqcn")) 
      {
        i++;
        options.fully_qualified_names = true;
      }
      else if (args[i].equals("-c"))
      {
        options.post_compiler = null;
        i++;
      }
      else if (args[i].equals("-post"))
      {
        i++;
        options.post_compiler = args[i];
        i++;
      }
      else if (args[i].equals("-stdout")) 
      {
        i++;
        options.output_stdout = true;
      }
      else if (args[i].equals("-ext") || args[i].equals("-extension")) 
      {
        i++;
	loadExtension(args[i]);
	i++;
      }
      else if (args[i].equals("-sx")) 
      {
        i++;
        options.source_ext = args[i];
        i++;
      }
      else if (args[i].equals("-ox")) 
      {
        i++;
        options.output_ext = args[i];
        i++;
      }
      else if (args[i].equals("-noserial")) 
      {
        i++;
	options.serialize_type_info = false;
      }
      else if (args[i].equals("-v") || args[i].equals("-verbose"))
      {
        i++;
	Integer level = (Integer) options.report.get("verbose");
	if (level == null) options.report.put("verbose", new Integer(1));
      }
      else if (args[i].equals("-report")) {
        i++;
	String report_option = args[i];
        StringTokenizer st = new StringTokenizer(args[i], "=");
	String topic = ""; int level = 0;
	if (st.hasMoreTokens()) topic = st.nextToken();
	if (st.hasMoreTokens())
	  try {
	    level = Integer.parseInt(st.nextToken());
	  } catch (NumberFormatException e) {}
	options.report.put(topic, new Integer(level));
	i++;
      }
      else if (args[i].startsWith("-")) {
	int i2 = i;
	if (options.extension != null) {
	    try  {
		i2 = options.extension.parseCommandLine(args, i, options);
	    }
	    catch (UsageError u) {
		System.err.println(u.getMessage());
		usage();
		System.exit(1);
	    }
	} 
	if (i2 == i) {
	    System.err.println(compilerName() + ": illegal option -- " 
				+ args[i]);
	    i++;
	    System.exit(1);
	}
	//System.err.println("Extension: " + i + " to " + i2);
	i = i2;
      } else {
        source.add(args[i]);
        options.source_path.add(new File(args[i]).getParentFile());
        i++;
      }
    }

    if (source.size() < 1) {
      System.err.println(compilerName()
                          + ": must specify at least one source file");
      usage();
      System.exit(1);
    }

    if (options.extension != null) {
	try {
	    options.extension.setOptions(options);
	}
	catch (UsageError u) {
	    System.err.println(u.getMessage());
	    usage();
	    System.exit(1);
	}
    }
  }

  private static String compilerName() {
    if (options.extension == null) return "jlc";
      else return options.extension.compilerName();
  }

  private static void usage()
  {
    String fileext, compilerName;
    fileext = options.extension.fileExtension();

    System.err.println("usage: " + compilerName() + " [options] " +
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
    System.err.println(" -ext <extension>        use language extension");
    System.err.println(" -c                      compile only to .java");
    System.err.println(" -post <compiler>        run javac-like compiler" 
                        + " after translation");
    System.err.println(" -v -verbose             print verbose " 
                        + "debugging information");
    System.err.println(" -report <topic>=<level> print verbose debugging" +
                        " information about topic\n" +
			"                         at specified verbosity");
    System.err.println("   (Allowed topics: "+report_topics+")");
    System.err.println(" -version                print version info");
    System.err.println(" -h                      print this message");
    System.err.println();
    System.err.println(options.extension.options());
  }
}
