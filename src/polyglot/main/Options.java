package polyglot.main;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.PrintStream;
import polyglot.frontend.ExtensionInfo;

/** 
 * This object encapsulates various polyglot options. 
 */
public class Options {
    /**
     * An annoying hack to allow objects to get their hands on the Options
     * object. This should be fixed. XXX###@@@
     */
    public static Options global;
    
    /**
     * Back pointer to the extension that owns this options
     */
    protected ExtensionInfo extension = null;
    
    /*
     * Fields for storing values for options.
     */
    public int error_count = 100;
    public Collection source_path; // List[String]
    public File output_directory;
    public String default_classpath;
    public String classpath;
    public String bootclasspath = null;
    public boolean assertions = false;

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
  
    /** Dump the AST after the following passes? */
    public Set dump_ast = new HashSet();
  
    /** Disable the following passes? */
    public Set disable_passes = new HashSet();
  
    /** keep output files */
    public boolean keep_output_files = true;
  

    public Map report = new HashMap(); // Map[String, Integer]
  

    /**
     * Constructor
     */
    public Options(ExtensionInfo extension) {
        this.extension = extension;
        setDefaultValues();
    }
    
    /**
     * Set default values for options
     */
    public void setDefaultValues() {
        String default_bootpath = System.getProperty("sun.boot.class.path");
        if (default_bootpath == null) {
          default_bootpath = System.getProperty("java.home") +
                     File.separator + "jre" +
                     File.separator + "lib" +
                     File.separator + "rt.jar";
        }
    
        default_classpath = System.getProperty("java.class.path") +
                            File.pathSeparator + default_bootpath;
        classpath = default_classpath;        


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
    
    /**
     * Parse the command line
     * 
     * @throws UsageError if the usage is incorrect.
     */
    public void parseCommandLine(String args[], Set source) throws UsageError {
        if(args.length < 1) {
            throw new UsageError("No command line arguments given");
        }
    
        for(int i = 0; i < args.length; ) {
            try {
                int ni = parseCommand(args, i, source);                
                if (ni == i) {
                    throw new UsageError("illegal option -- " + args[i]);
                }
                
                i = ni;

            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new UsageError("missing argument");
            }
        }
                    
        if (source.size() < 1) {
          throw new UsageError("must specify at least one source file");
        }
    }
    
    /**
     * Parse a command
     * @return the next index to process. i.e., if calling this method
     *         processes two commands, then the return value should be index+2
     */
    protected int parseCommand(String args[], int index, Set source) throws UsageError {
        int i = index;
        if (args[i].equals("-h") || 
            args[i].equals("-help") || 
            args[i].equals("--help")) {
            throw new UsageError("", 0);
        }
        else if (args[i].equals("-version")) {
            if (extension != null)
                System.out.println(extension.compilerName() +
                                   " version " + extension.version());
            System.out.println("Polyglot compiler toolkit version " +
                               new polyglot.ext.jl.Version());
            System.exit(0);
        }
        else if (args[i].equals("-d"))
        {
            i++;
            output_directory = new File(args[i]);
            i++;
        }
        else if (args[i].equals("-classpath") ||
                 args[i].equals("-cp")) {
            i++;
            classpath = args[i] + System.getProperty("path.separator") +
                        default_classpath;
            i++;
        }
        else if (args[i].equals("-bootclasspath")) {
            i++;
            bootclasspath = args[i];
            i++;
        }
        else if (args[i].equals("-sourcepath"))
        {
            i++;
            StringTokenizer st = new StringTokenizer(args[i], File.pathSeparator);
            while(st.hasMoreTokens())
            {
                source_path.add(new File(st.nextToken()));
            }
            i++;
        }
        else if (args[i].equals("-assert")) 
        {
            i++;
            assertions = true;
        }
        else if (args[i].equals("-fqcn")) 
        {
            i++;
            fully_qualified_names = true;
        }
        else if (args[i].equals("-c"))
        {
            post_compiler = null;
            i++;
        }
        else if (args[i].equals("-errors"))
        {
            i++;
            try {
                error_count = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {}
                i++;
        }
        else if (args[i].equals("-w"))
        {
            i++;
            try {
                output_width = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {}
                i++;
        }
        else if (args[i].equals("-post"))
        {
            i++;
            post_compiler = args[i];
            i++;
        }
        else if (args[i].equals("-stdout")) 
        {
            i++;
            output_stdout = true;
        }
        else if (args[i].equals("-sx")) 
        {
            i++;
            source_ext = args[i];
            i++;
        }
        else if (args[i].equals("-ox"))
        {
            i++;
            output_ext = args[i];
            i++;
        }
        else if (args[i].equals("-noserial"))
        {
            i++;
            serialize_type_info = false;
        }
        else if (args[i].equals("-dump"))
        {
            i++;
            String pass_name = args[i];
            dump_ast.add(pass_name);
            i++;
        }
        else if (args[i].equals("-disable"))
        {
            i++;
            String pass_name = args[i];
            disable_passes.add(pass_name);
            i++;
        }
        else if (args[i].equals("-nooutput"))
        {
            i++;
            keep_output_files = false;
        }
        else if (args[i].equals("-nosourcecheck")) 
        {
            i++;
            no_source_check = true;
        }
        else if (args[i].equals("-v") || args[i].equals("-verbose"))
        {
            i++;
            Integer level = (Integer) report.get("verbose");
            if (level == null) report.put("verbose", new Integer(1));
        }
        else if (args[i].equals("-report")) {
            i++;
            String report_option = args[i];
            StringTokenizer st = new StringTokenizer(args[i], "=");
            String topic = ""; int level = 0;
            if (st.hasMoreTokens()) topic = st.nextToken();
            if (st.hasMoreTokens()) {
                try {
                    level = Integer.parseInt(st.nextToken());
                } 
                catch (NumberFormatException e) {}
            }
            report.put(topic, new Integer(level));
            i++;
        }        
        else if (!args[i].startsWith("-")) {
            source.add(args[i]);
            source_path.add(new File(args[i]).getParentFile());
            i++;
        }
        
        return i;
    }
    
    /**
     * Print usage information
     */
    public void usage(PrintStream out) {
        out.println("usage: " + extension.compilerName() + " [options] " +
                           "<source-file>." + extension.fileExtension() + " ...\n");
        out.println("where [options] includes:");
        out.println(" @<file>                 read options from <file>");
        out.println(" -d <directory>          output directory");
        out.println(" -assert                 recognize the assert keyword");
        out.println(" -sourcepath <path>      source path");
        out.println(" -bootclasspath <path>   path for bootstrap class files");
        out.println(" -ext <extension>        use language extension");
        out.println(" -extclass <ext-class>   use language extension");
        out.println(" -fqcn                   use fully-qualified class"
                           + " names");
        out.println(" -sx <ext>               set source extension");
        out.println(" -ox <ext>               set output extension");
        out.println(" -dump <pass>            dump the ast after " +
                           "pass <pass>");
        out.println(" -disable <pass>         disable pass <pass>");
        out.println(" -scramble [seed]        scramble the ast " +
                       "(for testing)");
        out.println(" -noserial               disable class"
                           + " serialization");
        out.println(" -nooutput               delete output files after" +
                           " compilation");
        out.println(" -c                      compile only to .java");
        out.println(" -post <compiler>        run javac-like compiler" 
                           + " after translation");
        out.println(" -v -verbose             print verbose " 
                           + "debugging information");
        out.println(" -report <topic>=<level> print verbose debugging" +
                           " information about topic\n" +
                           "                         at specified verbosity");
        out.println("   (Allowed topics: " + Report.topics + ")");
        out.println(" -version                print version info");
        out.println(" -h                      print this message");
        out.println();
    }


  public String constructFullClasspath() {
      StringBuffer fullcp = new StringBuffer();
      if (bootclasspath != null) {
	  fullcp.append(bootclasspath);
      }
      fullcp.append(classpath);
      return fullcp.toString();
  }

  public String constructPostCompilerClasspath() {
      return output_directory + File.pathSeparator
              + "." + File.pathSeparator
              + System.getProperty("java.class.path");
  }

  public int level(String name) {
    Object i = report.get(name);
    if (i == null) return 0;
    else return ((Integer)i).intValue();
  }

}
