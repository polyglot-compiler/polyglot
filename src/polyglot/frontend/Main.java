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

  public static final void main(String args[]) throws Exception
  {
    Map options = new HashMap();
    Set source = new TreeSet();
    MainTargetFactory tf; 
    
    parseCommandLine(args, options, source);
    tf = new MainTargetFactory( (String)options.get( MAIN_OPT_SOURCE_EXT),
                                (Collection)options.get( MAIN_OPT_SOURCE_PATH),
                                (File)options.get( MAIN_OPT_OUTPUT_DIRECTORY));

    /* Must initialize before instantiating any compilers. */
    Compiler.initialize( options, tf, new MainErrorQueueFactory());
    
    /* Now compile each file. */
    Compiler compiler = new Compiler();
    Iterator i = source.iterator();
    String targetName = null;

    try
    {
      while( i.hasNext()) {
        targetName = (String)i.next();
        if( !compiler.compileFile( targetName)) {
          System.exit( 1);
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
    if( !Compiler.cleanup()) {
      System.exit( 1);
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
    options.put( MAIN_OPT_SOURCE_EXT, ".jl");
    
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
        options.put( Compiler.OPT_FQCN, new Boolean( true));
      }
      else if( args[i].equals( "-v") || args[i].equals( "-verbose"))
      {
        options.put( Compiler.OPT_VERBOSE, new Boolean( true));
      }
      else
      {
        source.add( args[i]);
        sourcePath.add( new File( args[i]).getParentFile());
        i++;
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
    System.err.println( " -v -verbose             print verbose " 
                        + "debugging info");
    System.err.println( " -version                print version info");
    System.err.println( " -h                      print this message");
  }
}
