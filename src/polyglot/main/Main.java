package polyglot.main;

import polyglot.ast.Node;
import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.types.TypeSystem;
import polyglot.util.*;

import java.io.*;
import java.util.*;

/** Main is the main program of the extensible compiler. It should not
 * need to be replaced.
 */
public class Main
{

  /** Source files specified on the command line */
  private static Set source;

  /** Whether any errors seen yet */
  private boolean hasErrors = false;

  public static final void main(String args[])
  {      
    source = new HashSet();
    Options options = Options.global;
    
    parseCommandLine(args, options, source);

    Compiler compiler = new Compiler(options);

    long time0 = System.currentTimeMillis();
    
    String targetName = null;
    if (!compiler.compile(source)) System.exit(1);

    Report.report(null, 1, "Output files: " + compiler.outputFiles());

    long start_time = System.currentTimeMillis();
    
    /* Now call javac or jikes, if necessary. */
    if (options.post_compiler != null && !options.output_stdout) {
      Runtime runtime = Runtime.getRuntime();
      
      Iterator iter = compiler.outputFiles().iterator();
      String outputFiles = "";
      while(iter.hasNext()) {
	outputFiles += (String)iter.next() + " ";
      }
	
	String command = options.post_compiler + " -classpath " 
	  + options.output_directory + File.pathSeparator
	  + "." + File.pathSeparator
	  + System.getProperty("java.class.path") + " "
	  + outputFiles;

	Report.report(null, 1, "Executing post-compiler " + command);
	
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
	  
	  if (!options.keep_output_files) {
	    String command2 = "rm " + outputFiles;
	    runtime.exec(command2);
	  }
	  
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

    reportTime(1, "Finished compiling Java output files. time=" + 
	    (System.currentTimeMillis() - start_time));
    
    reportTime(1, "Total time=" + (System.currentTimeMillis() - time0));
    
  }

  static final void loadExtension(String ext) {
    if (ext != null && ! ext.equals("")) {
      String extClassName = "polyglot.ext." + ext + ".ExtensionInfo";

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
	Options.global.extension = (ExtensionInfo) extClass.newInstance();
      }
      catch (ClassCastException e) {
	System.err.println(ext + " is not a valid polyglot extension:" +
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
      options.usage();
      System.exit(1);
    }

    try {
      for(int i = 0; i < args.length; )
      {
        if (args[i].equals("-h")) {
          options.usage();
          System.exit(0);
        }
        else if (args[i].equals("-version")) {
          if (options.extension != null)
              System.out.println(options.extension.compilerName() +
                  " version " + options.extension.version());
          System.out.println("Polyglot compiler toolkit version " +
              new polyglot.ext.jl.Version());
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
          options.classpath = args[i] + System.getProperty("path.separator") +
                          options.default_classpath;
          i++;
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
        else if (args[i].equals("-errors"))
        {
          i++;
          try {
            options.error_count = Integer.parseInt(args[i]);
          } catch (NumberFormatException e) {}
          i++;
        }
        else if (args[i].equals("-w"))
        {
          i++;
          try {
            options.output_width = Integer.parseInt(args[i]);
          } catch (NumberFormatException e) {}
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
        else if (args[i].equals("-dump"))
        {
          i++;
          options.dump_ast = true;
        }

        else if (args[i].equals("-nooutput"))
        {
          i++;
          options.keep_output_files = false;
        }
        else if (args[i].equals("-nosourcecheck")) 
        {
          i++;
          options.no_source_check = true;
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
                  options.usage();
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
    }
    catch (ArrayIndexOutOfBoundsException e) {
      System.err.println(compilerName() + ": missing argument");
      options.usage();
      System.exit(1);
    }

    if (source.size() < 1) {
      System.err.println(compilerName()
                          + ": must specify at least one source file");
      options.usage();
      System.exit(1);
    }

    if (options.extension != null) {
	try {
	    options.extension.setOptions(options);
	}
	catch (UsageError u) {
	    System.err.println(u.getMessage());
	    options.usage();
	    System.exit(1);
	}
    }
  }

  static String compilerName() {
    return Options.global.extension.compilerName();
  }

  static private Collection timeTopics = new ArrayList(1);
  static {
      timeTopics.add("time");
  }

  static private void reportTime(int level, String msg) {
      Report.report(timeTopics, level, msg);
  }
}
