package jltools.frontend;

import java.io.*;
import java.util.*;

import jltools.ast.Node;
import jltools.frontend.Compiler;
import jltools.util.UnicodeWriter;

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

  public static final void main(String args[])
  {
    Map options = new HashMap();
    Set source = new TreeSet();
    MainTargetFactory tf; 
    
    parseCommandLine(args, options, source);
    tf = new MainTargetFactory( (String)options.get( MAIN_OPT_SOURCE_EXT),
                                (Collection)options.get( MAIN_OPT_SOURCE_PATH),
                                (File)options.get( MAIN_OPT_OUTPUT_DIRECTORY),
                                (String)options.get( MAIN_OPT_OUTPUT_EXT),
                                (Boolean)options.get( MAIN_OPT_STDOUT));

    /* Must initialize before instantiating any compilers. */
    Compiler.initialize( options, tf, new MainErrorQueueFactory());
    
    /* Now compile each file. */
    Compiler compiler = new Compiler();
    Iterator iter = source.iterator();
    String targetName = null;
    boolean hasErrors = false;

    try
    {
      while( iter.hasNext()) {
        targetName = (String)iter.next();
        if( !compiler.compileFile( targetName)) {
          hasErrors = true;
        }
      }
    }
    catch( FileNotFoundException fnfe)
    {
      System.err.println( "Cannot find source file: " + targetName);
      System.exit( 1);
    }
    catch( IOException e)
    {
      System.err.println( "Caught IOException while compiling " 
                          + targetName + ": " + e.getMessage());
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
    if( options.get( MAIN_OPT_POST_COMPILER) != null) {
      Runtime runtime = Runtime.getRuntime();
      Process proc;
      MainTargetFactory.MainTarget t;
      String command;

      iter = completed.iterator();
      while( iter.hasNext()) {
        t = (MainTargetFactory.MainTarget)iter.next(); 

        command =  (String)options.get( MAIN_OPT_POST_COMPILER) 
                      + " -classpath " + options.get(MAIN_OPT_OUTPUT_DIRECTORY)
                        + File.pathSeparator + "." + File.pathSeparator 
                        + System.getProperty( "java.class.path") + " "
                        + t.outputFile.getPath();

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

  static final void parseCommandLine(String args[], Map options, Set source)
  {
    if(args.length < 1)
    {
      usage();
      System.exit( 1);
    }

    Collection sourcePath = new LinkedList();
    sourcePath.add( new File( "."));
    options.put( MAIN_OPT_SOURCE_PATH, sourcePath);
    options.put( MAIN_OPT_OUTPUT_DIRECTORY, new File( "."));
    
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
        options.put( Compiler.OPT_DUMP, new Boolean( true));
      }
      else if( args[i].equals( "-scramble"))
      {
        i++;
        options.put( Compiler.OPT_SCRAMBLE, new Boolean( true));
      }
      else if( args[i].equals( "-v") || args[i].equals( "-verbose"))
      {
        i++;
        options.put( Compiler.OPT_VERBOSE, new Boolean( true));
      }
      else
      {
        if( options.get( MAIN_OPT_SOURCE_EXT) == null) {
          options.put( MAIN_OPT_SOURCE_EXT, args[i].substring( 
                           args[i].lastIndexOf( '.')));
        }
        source.add( args[i]);
        sourcePath.add( new File( args[i]).getParentFile());
        i++;
      }
    }

    if( options.get( MAIN_OPT_OUTPUT_EXT) == null) {
      if( options.get( MAIN_OPT_SOURCE_EXT).equals( ".java")) {
        options.put( MAIN_OPT_OUTPUT_EXT, ".jlava");
      }
      else {
        options.put( MAIN_OPT_OUTPUT_EXT, ".java");
      }
    }
    else
    {
      if( options.get( MAIN_OPT_SOURCE_EXT).equals( 
                      options.get(MAIN_OPT_OUTPUT_EXT))) {
        System.err.println( "jltools.frontent.Main: Source and output"
                            + " extensions must differ.");
        System.exit( 1);
      } 
    }
  }

  private static void usage()
  {
    System.err.println( "usage: jltools.frontend.Main [options] " 
                        + "File.jl ...\n");
    System.err.println( "where [options] includes:");
    System.err.println( " -d <directory>          output directory");
    System.err.println( " -S <path list>          source path");
    System.err.println( " -fqcn                   print fully-qualified class"
                        + " names in comments");
    System.err.println( " -stdout                 print all source to stdout");
    System.err.println( " -sx <ext>               set source extension");
    System.err.println( " -ox <ext>               set output extension");
    System.err.println( " -dump                   dump the ast");
    System.err.println( " -scramble               scramble the ast");
    System.err.println( " -post <compiler>        run javac-like compiler" 
                        + " after translation");
    System.err.println( " -v -verbose             print verbose " 
                        + "debugging info");
    System.err.println( " -version                print version info");
    System.err.println( " -h                      print this message");
  }
}
