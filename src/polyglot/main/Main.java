package jltools.main;

import jltools.ast.Node;
import jltools.frontend.Compiler;
import jltools.frontend.ExtensionInfo;
import jltools.frontend.StandardExtensionInfo;
import jltools.types.TypeSystem;
import jltools.util.*;

import java.io.*;
import java.util.*;


public class Main
{
  private static final String MAIN_OPT_SOURCE_PATH 
                                        = "Source Path (Coll'n of File";
  private static final String MAIN_OPT_OUTPUT_DIRECTORY
                                        = "Output Directory (File)";
  private static final String MAIN_OPT_SOURCE_EXT 
                                        = "Source Extension (String)";
  private static final String MAIN_OPT_OUTPUT_EXT 
                                        = "Output Extension (String)";
  private static final String MAIN_OPT_STDOUT
                                        = "Output to stdout (Boolean)";
  private static final String MAIN_OPT_POST_COMPILER
                                        = "Name of Post Compiler (String)";
  private static final String MAIN_OPT_DUMP             
                                        = "Dump AST (Boolean)";
  private static final String MAIN_OPT_SCRAMBLE         
                                        = "Scramble AST (Boolean)";
  private static final String MAIN_OPT_SCRAMBLE_SEED  
                                        = "Scramble Random Seed (Long)";
  private static final String MAIN_OPT_THREADS
                                        = "Use multiple threads (Boolean)";
  private static final String MAIN_OPT_EXT
                                        = "Use language extension (String)";

  private static final int MAX_THREADS = 2;

  private static Map options;
  private static Set source;
  private static boolean hasErrors = false;

  public static final void main(String args[])
  {
    options = new HashMap();
    source = new TreeSet();
    
    parseCommandLine(args, options, source);

    MainTargetFactory tf = new MainTargetFactory( (String)options.get( MAIN_OPT_SOURCE_EXT),
                               (Collection)options.get( MAIN_OPT_SOURCE_PATH),
                                (File)options.get( MAIN_OPT_OUTPUT_DIRECTORY),
                                (String)options.get( MAIN_OPT_OUTPUT_EXT),
                                (Boolean)options.get( MAIN_OPT_STDOUT));

    String ext = (String) options.get(MAIN_OPT_EXT);
    Class extClass = null;
    ExtensionInfo extInfo = null;

    if (ext != null && ! ext.equals("")) {
      String extClassName = "jltools.ext." + ext + ".ExtensionInfo";

      try {
	extClass = Class.forName(extClassName);
      }
      catch (ClassNotFoundException e) {
	System.err.println( "Extension " + ext +
	  " not found: could not find class " + extClassName + ".");
	System.exit( 1);
      }

      try {
	extInfo = (ExtensionInfo) extClass.newInstance();
      }
      catch (Exception e) {
	System.err.println( "Extension " + ext +
	  " could not be loaded: could not instantiate " + extClassName + ".");
	System.exit( 1);
      }
    }

    if (extInfo == null) {
	extInfo = new StandardExtensionInfo();
    }

    /* Must initialize before instantiating any compilers. */

    Compiler.initialize( options, tf, extInfo);
    
    /* Now compile each file. */
    int totalThreads;

    if( ((Boolean)options.get( MAIN_OPT_THREADS)).booleanValue()) {
      totalThreads = Math.min( source.size(), MAX_THREADS);
    }
    else {
      totalThreads = 1;
    }

    TargetSet tl = new TargetSet( source, totalThreads);
    Compiler compiler = new Compiler();

    /* A note about threads: the multithreaded implementation is not entirely 
     * complete. It will translate any file given on the command-line, but
     * will not translate dependent class files. Also the -post options does
     * not work with the multithreaded implementation. (It could but was 
     * never fully implemented.) The rationale here is that the multithreaded
     * version really isn't any faster and so there was no reason to finish it.
     */

    if( totalThreads > 1) {
      /* Start the threads. */
      for( int i = 0; i < totalThreads; i++) {
        WorkThread wt = new WorkThread( tl, compiler);
        Thread thread = new Thread( wt);
	Compiler.verbose( Main.class, "Starting a thread.");
        thread.start();
      }
    }
    else {
      /* Single threaded mode. */
      Iterator iter;
      String targetName = null;

      try
      {
        Compiler.verbose( Main.class, "read all files from the command-line.");
        
        iter = source.iterator();
        while( iter.hasNext()) {
          targetName = (String)iter.next();
          if( !compiler.readFile( targetName)) {
            hasErrors = true;
          }
        }
        
        Compiler.verbose( Main.class, "done reading, now translating...");
        
        iter = source.iterator();
        while( iter.hasNext()) {
          targetName = (String)iter.next();
          if( !compiler.compileFile( targetName)) {
            hasErrors = true;
          }
        }
        
      }
      catch( FileNotFoundException fnfe)
      {
        System.err.println( Main.class.getName() 
                            + ": cannot find source file -- " + targetName);
        System.exit( 1);
      }
      catch( IOException e)
      {
        System.err.println( Main.class.getName() 
                            + ": caught IOException while compiling -- " 
                            + targetName + ": " + e.getMessage());
        System.exit( 1);
      }
      catch( ErrorLimitError ele)
      {
        System.exit( 1);
      }
      
      /* Make sure we do this before we exit. */
      Collection completed = new LinkedList();
      try
      {
        if( !compiler.cleanup( completed)) {
          System.exit( 1);
        }
      }
      catch( IOException e)
      {
        System.err.println( "Caught IOException while compiling: "
                            + e.getMessage());
        System.exit( 1);
      }
      
      if( hasErrors) {
        System.exit( 1);
      }
      
      /* Now call javac or jikes, if necessary. */
      if( options.get( MAIN_OPT_POST_COMPILER) != null
          && !((Boolean)options.get( MAIN_OPT_STDOUT)).booleanValue()) {
        Runtime runtime = Runtime.getRuntime();
        Process proc;
        MainTargetFactory.MainTarget t;
        String command;
        
        iter = completed.iterator();
        while( iter.hasNext()) {
          t = (MainTargetFactory.MainTarget)iter.next(); 
          
          if( t.outputFileName == null) {
            continue;
          }
          
          command =  (String)options.get( MAIN_OPT_POST_COMPILER) 
            + " -classpath " 
            + ( options.get(MAIN_OPT_OUTPUT_DIRECTORY) != null ?
                options.get(MAIN_OPT_OUTPUT_DIRECTORY) 
                + File.pathSeparator + "." + File.pathSeparator :
                "") 
            + System.getProperty( "java.class.path") + " "
            + t.outputFileName;
          
          Compiler.verbose( Main.class, "executing " + command);
          
          try 
          {
            proc = runtime.exec( command);
            
            InputStreamReader err = 
              new InputStreamReader( proc.getErrorStream());
            char[] c = new char[ 72];
            int len;
            while( (len = err.read( c)) > 0) {
              System.err.print( String.valueOf( c, 0, len));
            }
            
            proc.waitFor();
            if( proc.exitValue() > 0) {
              System.exit( proc.exitValue());
            }
          }
          catch( Exception e) 
          { 
            System.err.println( "Caught Exception while running compiler: "
                                + e.getMessage());
            System.exit( 1);
          }
        }
      }
    }
  }    public static File getOutputDirectory() {
      return (File)options.get( MAIN_OPT_OUTPUT_DIRECTORY);  }


  private static void setHasErrors( boolean b)
  {
    hasErrors = b;
  }
  
  private static class TargetSet 
  {
    private static final int READ = 0;
    private static final int COMPILE = 1;

    private Set source;
    private int totalThreads;
    private int phase = READ;

    private Iterator sourceIter;

    private Object countLock = new Object();
    private int threadsInNext = 0;
    
    private Object sourceLock = new Object();
    
    TargetSet( Set source, int totalThreads)
    {
      this.source = source;
      this.totalThreads = totalThreads;

      sourceIter = source.iterator();

      Compiler.verbose( Main.class, "read all files from the command-line.");
    }

    String nextTarget()
    {
      String result;
      
      synchronized( countLock) {
        threadsInNext++;
      }

      synchronized( sourceLock) {
        if( !sourceIter.hasNext()) {
          /* There is nothing to do, so figure out if there are still threads
           * reading something, if so, then wait, otherwise, just fall out
           * and return null.
           */
          if( threadsInNext == totalThreads && phase == READ) {
            Compiler.verbose( Main.class, "done reading, now translating...");

            phase = COMPILE;

            sourceIter = source.iterator();
            sourceLock.notifyAll();

            result = null;
          }
          else if( phase == COMPILE) {
            /* If we are compiling then there is no need to wait. */
            result = null;
          }
          else {
            /* Nope, still threads reading, we need to wait. */
            try
            {
              sourceLock.wait();
            }
            catch( InterruptedException e)
            {
              setHasErrors( true);
            }

            /* When we wake up we will to return null. */
            result = null;
          }

        }
        else {
          /* There is something to do... */
          result = (String)sourceIter.next();
        }
      }
      
      
      synchronized( countLock) {
        threadsInNext--;
      }
      
      return result;
    }
  }

  private static class WorkThread implements Runnable
  {
    TargetSet tl;
    Compiler compiler;

    WorkThread( TargetSet tl, Compiler compiler)
    {
      this.tl = tl;
      this.compiler = compiler;
    }
    
    public void run()
    {
      String targetName = null;
      try
      {
        while( (targetName = tl.nextTarget()) != null) {
          if( !compiler.readFile( targetName)) {
            setHasErrors( true);
          }
        }
          
        while( (targetName = tl.nextTarget()) != null) {
          if( !compiler.compileFile( targetName)) {
            setHasErrors( true);
          }
        }
      }  
      catch( FileNotFoundException fnfe)
      {
        System.err.println( Main.class.getName() 
                            + ": cannot find source file -- " + targetName);
          setHasErrors( true);
      }
      catch( IOException e)
      {
        System.err.println( Main.class.getName() 
                            + ": caught IOException while compiling -- " 
                            + targetName + ": " + e.getMessage());
        setHasErrors( true);
        }
      catch( ErrorLimitError ele)
      {
        setHasErrors( true);
      }
    }
  }

  static final void parseCommandLine(String args[], Map options, Set source)
  {
    if(args.length < 1)
    {
      usage();
      System.exit( 1);
    }

    boolean hasError = false;

    /* Set defaults. */
    Collection sourcePath = new LinkedList();
    sourcePath.add( new File( "."));
    options.put( MAIN_OPT_SOURCE_PATH, sourcePath);
    options.put( MAIN_OPT_DUMP, new Boolean( false));
    options.put( MAIN_OPT_STDOUT, new Boolean( false));
    options.put( MAIN_OPT_SCRAMBLE, new Boolean( false));

    options.put( MAIN_OPT_EXT, new String(""));

    options.put( MAIN_OPT_THREADS, new Boolean( false));
    
    options.put( Compiler.OPT_OUTPUT_WIDTH, new Integer(120));
    options.put( Compiler.OPT_VERBOSE, new Boolean( false));
    options.put( Compiler.OPT_FQCN, new Boolean( false));
    options.put( Compiler.OPT_SERIALIZE, new Boolean( true));

    for( int i = 0; i < args.length; )
    {
      if( args[i].equals( "-h")) {
        usage();
        System.exit( 0);
      }
      else if( args[i].equals( "-version")) {
        System.out.println( "jltools Compiler version "
                           + Compiler.VERSION_MAJOR + "."
                           + Compiler.VERSION_MINOR + "."
                           + Compiler.VERSION_PATCHLEVEL);
        System.exit( 0);
      }
      else if( args[i].equals( "-d"))
      {
        i++;
        options.put( MAIN_OPT_OUTPUT_DIRECTORY, new File( args[i]));
        i++;
      }
      else if( args[i].equals( "-S"))
      {
        i++;
        StringTokenizer st = new StringTokenizer( args[i], File.pathSeparator);
        while( st.hasMoreTokens())
        {
          sourcePath.add( new File( st.nextToken()));
        }
        i++;
      }
      else if( args[i].equals( "-fqcn")) 
      {
        i++;
        options.put( Compiler.OPT_FQCN, new Boolean( true));
      }
      else if( args[i].equals( "-post"))
      {
        i++;
        options.put( MAIN_OPT_POST_COMPILER, args[i]);
        i++;
      }
      else if( args[i].equals( "-stdout")) 
      {
        i++;
        options.put( MAIN_OPT_STDOUT, new Boolean( true));
      }
      else if( args[i].equals( "-ext")) 
      {
        i++;
        options.put( MAIN_OPT_EXT, args[i]);
        i++;
      }
      else if( args[i].equals( "-sx")) 
      {
        i++;
        options.put( MAIN_OPT_SOURCE_EXT, args[i]);
        i++;
      }
      else if( args[i].equals( "-ox")) 
      {
        i++;
        options.put( MAIN_OPT_OUTPUT_EXT, args[i]);
        i++;
      }
      else if( args[i].equals( "-dump"))
      {
        i++;
        options.put( MAIN_OPT_DUMP, new Boolean( true));
      }
      else if( args[i].equals( "-scramble"))
      {
        i++;
        options.put( MAIN_OPT_SCRAMBLE, new Boolean( true));
        try
        {
          long l = Long.parseLong( args[i]);
          options.put( MAIN_OPT_SCRAMBLE_SEED, new Long( l));
          i++;
        }
        catch( NumberFormatException e) {}
      }
      else if( args[i].equals( "-noserial")) 
      {
        i++;
        options.put( Compiler.OPT_SERIALIZE, new Boolean( false));
      }
      else if( args[i].equals( "-threads")) 
      {
        i++;
        options.put( MAIN_OPT_THREADS, new Boolean( true));
      }
      else if( args[i].equals( "-v") || args[i].equals( "-verbose"))
      {
        i++;
        options.put( Compiler.OPT_VERBOSE, new Boolean( true));
      }
      else if( args[i].startsWith( "-"))
      {
        System.err.println( Main.class.getName() + ": illegal option -- " 
                            + args[ i]);
        i++;
        hasError = true;
      }
      else
      {
        if( hasError) {
          usage();
          System.exit( 1);
        }

        if( options.get( MAIN_OPT_SOURCE_EXT) == null
            && args[i].indexOf( '.') != -1) {
          options.put( MAIN_OPT_SOURCE_EXT, args[i].substring( 
                           args[i].lastIndexOf( '.')));
        }
        source.add( args[i]);
        sourcePath.add( new File( args[i]).getParentFile());
        i++;
      }
    }

    if( hasError) {
      usage();
      System.exit( 1);
    }

    if( source.size() < 1) {
      System.err.println( Main.class.getName() 
                          + ": must specify at least one source file");
      usage();
      System.exit( 1);
    }

    /* Check first for a source extension. */
    if( options.get( MAIN_OPT_SOURCE_EXT) == null) {
      options.put( MAIN_OPT_SOURCE_EXT, ".java");
    }

    /* Now check for an output extension. */
    if( options.get( MAIN_OPT_OUTPUT_EXT) == null) {
      options.put( MAIN_OPT_OUTPUT_EXT, ".java");
    }
  }

  private static void usage()
  {
    System.err.println( "usage: " + Main.class.getName() + " [options] " 
                        + "File.jl ...\n");
    System.err.println( "where [options] includes:");
    System.err.println( " -d <directory>          output directory");
    System.err.println( " -S <path list>          source path");
    System.err.println( " -fqcn                   use fully-qualified class"
                        + " names");
    System.err.println( " -sx <ext>               set source extension");
    System.err.println( " -ox <ext>               set output extension");
    System.err.println( " -dump                   dump the ast");
    System.err.println( " -scramble [seed]        scramble the ast");
    System.err.println( " -noserial               disable class"
                        + " serialization");
    System.err.println( " -ext <extension>        use language extension");
    System.err.println( " -post <compiler>        run javac-like compiler" 
                        + " after translation");
    System.err.println( " -v -verbose             print verbose " 
                        + "debugging info");
    System.err.println( " -version                print version info");
    System.err.println( " -h                      print this message");
    System.err.println();
  }
}
