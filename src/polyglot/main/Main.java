package jltools.main;

import jltools.ast.Node;
import jltools.frontend.Compiler;
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
  private static final String MAIN_OPT_EXT_OP
                                        = "Use ObjectPrimitive Ext (Boolean)";

  private static final int MAX_THREADS = 1;

  private static Map options;
  private static Set source;
  private static TypeSystem ts;

  public static final void main(String args[])
  {
    options = new HashMap();
    source = new TreeSet();
    MainTargetFactory tf; 
    
    parseCommandLine(args, options, source);

    tf = new MainTargetFactory( (String)options.get( MAIN_OPT_SOURCE_EXT),
                               (Collection)options.get( MAIN_OPT_SOURCE_PATH),
                                (File)options.get( MAIN_OPT_OUTPUT_DIRECTORY),
                                (String)options.get( MAIN_OPT_OUTPUT_EXT),
                                (Boolean)options.get( MAIN_OPT_STDOUT));

    /* Must initialize before instantiating any compilers. */
    if( ((Boolean)options.get( MAIN_OPT_EXT_OP)).booleanValue()) {
      ts = new jltools.ext.op.ObjectPrimitiveTypeSystem();
    }
    else {
      ts = new jltools.types.StandardTypeSystem();
    }
    Compiler.initialize( options, ts, tf);
    
    /* Now compile each file. */
    Iterator iter;
    String targetName = null;
    boolean hasErrors = false;

    /*
    int numberOfThreads = Math.min( source.size(), MAX_THREADS);

    Thread thread;
    MainCompilerThread[] cthreads = new MainCompilerThread[ numberOfThreads];
    for( int i = 0; i < numberOfThreads) {
      cthreads[ i] = new MainCompilerThread( compiler, source);
      thread = new Thread( cthreads[ i]);
      thread.start();
    }
    */

    Compiler compiler = new Compiler();

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
          System.err.println( "Caught Exception while running post compiler: "
                              + e.getMessage());
          System.exit( 1);
        }
      }
    }
  }

  static class MainCompilerThread implements Runnable
  {
    MainCompilerThread()
    {
    }
    
    public void run()
    {
      while( true) {
        



      }
    }
  }

  /**
   * Returns an instance of the parser that should be used during this
   * compilation session.
   */
  static java_cup.runtime.lr_parser getParser( jltools.lex.Lexer lexer,
                                               ErrorQueue eq)
  {
    if( ((Boolean)options.get( MAIN_OPT_EXT_OP)).booleanValue()) {
      return new jltools.ext.op.Grm( lexer, ts, eq);
    }
    else {
      return new jltools.parse.Grm( lexer, ts, eq);
    }
  }

  /**
   * Returns a iterator which contains the visitors that should be run in the
   * current stage of the compiler.
   */
  static Iterator getNodeVisitors( int stage)
  {
    List l = new LinkedList();

    if( ((Boolean)options.get( MAIN_OPT_SCRAMBLE)).booleanValue()
        && stage == Compiler.DISAMBIGUATED) {
      
      if( ((Boolean)options.get( MAIN_OPT_DUMP)).booleanValue()) {
        CodeWriter cw = new CodeWriter( new UnicodeWriter( 
                                          new PrintWriter( System.out)), 
               ((Integer)options.get( Compiler.OPT_OUTPUT_WIDTH)).intValue()); 

        l.add( new jltools.visit.DumpAst( cw));
      }

      jltools.visit.NodeScrambler ns;
      Long seed = (Long)options.get( MAIN_OPT_SCRAMBLE_SEED);
      if( seed == null) {
        ns = new jltools.visit.NodeScrambler();
      }
      else {
        ns = new jltools.visit.NodeScrambler( seed.longValue());
      }

      l.add( ns.fp);
      l.add( ns);
    }

    if( ((Boolean)options.get( MAIN_OPT_EXT_OP)).booleanValue()
        && stage == Compiler.CHECKED) {
      
      if( ((Boolean)options.get( MAIN_OPT_DUMP)).booleanValue()) {
        CodeWriter cw = new CodeWriter( new UnicodeWriter( 
                                          new PrintWriter( System.out)), 
               ((Integer)options.get( Compiler.OPT_OUTPUT_WIDTH)).intValue()); 

        l.add( new jltools.visit.DumpAst( cw));
      }

      l.add( new jltools.ext.op.ObjectPrimitiveCastRewriter( ts));

    }

    if( ((Boolean)options.get( MAIN_OPT_DUMP)).booleanValue()) {
      CodeWriter cw = new CodeWriter( new UnicodeWriter( 
                                        new PrintWriter( System.out)), 
            ((Integer)options.get( Compiler.OPT_OUTPUT_WIDTH)).intValue()); 

      l.add( new jltools.visit.DumpAst( cw));
    }
    return l.iterator();
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
    options.put( MAIN_OPT_EXT_OP, new Boolean( false));
    
    options.put( Compiler.OPT_OUTPUT_WIDTH, new Integer(80));
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
      else if( args[i].equals( "-op"))
      {
        i++;
        options.put( MAIN_OPT_EXT_OP, new Boolean( true));
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
    System.err.println( " -op                     use op extension");
    System.err.println( " -post <compiler>        run javac-like compiler" 
                        + " after translation");
    System.err.println( " -v -verbose             print verbose " 
                        + "debugging info");
    System.err.println( " -version                print version info");
    System.err.println( " -h                      print this message");
    System.err.println();
  }
}
