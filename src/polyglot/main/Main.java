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
  private Set source;

  final static String verbose = "verbose";

  /* modifies args */
  protected ExtensionInfo getExtensionInfo(List args) {
      ExtensionInfo ext = null;
      
      for (Iterator i = args.iterator(); i.hasNext(); ) {
          String s = (String)i.next();
          if (s.equals("-ext") || s.equals("-extension")) 
          {
              if (ext != null) {
                  System.err.println("only one extension can be specified");
                  System.exit(1);                  
              }
              
              i.remove();
              if (!i.hasNext()) {
                  System.err.println("missing argument");
                  System.exit(1);
              }
              String extName = (String)i.next();
              i.remove();
              ext = loadExtension("polyglot.ext." + extName + ".ExtensionInfo");
          }
          else if (s.equals("-extclass"))
          {
              if (ext != null) {
                  System.err.println("only one extension can be specified");
                  System.exit(1);                  
              }

              i.remove();
              if (!i.hasNext()) {
                  System.err.println("missing argument");
                  System.exit(1);
              }
              String extClass = (String)i.next();
              i.remove();
              ext = loadExtension(extClass);
          }      
      }
      if (ext != null) {
          return ext;
      }
      return loadExtension("polyglot.ext.jl.ExtensionInfo");
  }
  
  protected void start(String[] argv) {
      source = new HashSet();  
      List args = explodeOptions(argv);
      ExtensionInfo ext = getExtensionInfo(args);
      Options options = ext.getOptions();
      
      // Allow all objects to get access to the Options object. This hack should
      // be fixed somehow. XXX###@@@
      Options.global = options;
      try {
            argv = (String[]) args.toArray(new String[0]);
          options.parseCommandLine(argv, source);
      }
      catch (UsageError ue) {
          PrintStream out = (ue.exitCode==0 ? System.out : System.err);
          if (ue.getMessage() != null && ue.getMessage().length() > 0) {
              out.println(ext.compilerName() +": " + ue.getMessage());
          }
          options.usage(out);
          System.exit(ue.exitCode);
      }
      
      Compiler compiler = new Compiler(ext);
  
      long time0 = System.currentTimeMillis();
  
      String targetName = null;
      if (!compiler.compile(source)) System.exit(1);
  
      if (Report.should_report(verbose, 1))
          Report.report(1, "Output files: " + compiler.outputFiles());
  
      long start_time = System.currentTimeMillis();
  
      /* Now call javac or jikes, if necessary. */
      if (options.post_compiler != null && !options.output_stdout) {
        Runtime runtime = Runtime.getRuntime();
  
        Iterator iter = compiler.outputFiles().iterator();
        String outputFiles = "";
        while(iter.hasNext()) {
          outputFiles += (String)iter.next() + " ";
        }
  
          String command = options.post_compiler + " -classpath " +
                        options.constructPostCompilerClasspath() + " "
                         + outputFiles;
  
          if (Report.should_report(verbose, 1))
              Report.report(1, "Executing post-compiler " + command);
  
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
  
      if (Report.should_report(verbose, 1)) {
          reportTime("Finished compiling Java output files. time=" + 
                  (System.currentTimeMillis() - start_time), 1);
  
          reportTime("Total time=" + (System.currentTimeMillis() - time0), 1);
      }
  }
 
  private List explodeOptions(String[] args) {
      LinkedList ll = new LinkedList();

      for (int i = 0; i < args.length; i++) {
          // special case for the @ command-line parameter
          if (args[i].startsWith("@")) {
              String fn = args[i].substring(1);
              try {
                  BufferedReader lr = new BufferedReader(new FileReader(fn));
                  LinkedList newArgs = new LinkedList();

                  while (true) {
                      String l = lr.readLine();
                      if (l == null)
                          break;

                      StringTokenizer st = new StringTokenizer(l, " ");
                      while (st.hasMoreTokens())
                          newArgs.add(st.nextToken());
                  }

                  lr.close();
                  ll.addAll(newArgs);
              }
              catch (java.io.IOException e) {
                  System.err.println("cmdline parser: couldn't read args file "+fn);
                  System.exit(1);
              }
              continue;
          }

          ll.add(args[i]);
      }

      return ll;
  }

  public static final void main(String args[]) {      
      new Main().start(args);
  }

  static final ExtensionInfo loadExtension(String ext) {
    if (ext != null && ! ext.equals("")) {
      Class extClass = null;

      try {
        extClass = Class.forName(ext);
      }
      catch (ClassNotFoundException e) {
        System.err.println("Extension " + ext +
          " not found: could not find class " + ext + "." +
          e.getMessage());
        System.exit(1);
        return null;
      }

      try {
        return (ExtensionInfo) extClass.newInstance();
      }
      catch (ClassCastException e) {
	System.err.println(ext + " is not a valid polyglot extension:" +
	    " extension class " + ext +
	    " exists but is not a subclass of ExtensionInfo");
	System.exit(1);
      }
      catch (Exception e) {
	System.err.println("Extension " + ext +
	  " could not be loaded: could not instantiate " + ext + ".");
	System.exit(1);
      }
    }
    return null;
  }

  static private Collection timeTopics = new ArrayList(1);
  static {
      timeTopics.add("time");
  }

  static private void reportTime(String msg, int level) {
      Report.report(level, msg);
  }
}
