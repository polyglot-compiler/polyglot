package jltools.frontend;

import jltools.lex.Lexer;
import jltools.ast.Node;
import jltools.ast.SourceFileNode;
import jltools.parse.Grm;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.io.*;
import java.util.*;

public class Compiler
{
  private static TypeSystem ts;
  private static CompoundClassResolver systemResolver;
  private static TableClassResolver parsedResolver;
  private static SourceFileClassResolver sourceResolver;
  private static LoadedClassResolver loadedResolver;

  private static Map options;

  static
  {
    systemResolver = new CompoundClassResolver();
    
    parsedResolver = new TableClassResolver();
    systemResolver.addClassResolver( parsedResolver);

    sourceResolver = new SourceFileClassResolver( ".jl");
    systemResolver.addClassResolver( sourceResolver);

    loadedResolver = new LoadedClassResolver();
    systemResolver.addClassResolver( loadedResolver);

    ts = new StandardTypeSystem( systemResolver);
    loadedResolver.setTypeSystem( Compiler.ts);
    
    options = new HashMap();
  }

  public static String OPT_SOURCE_PATH             = "Source Path";

  public static void setOptions( Map options)
  {
    Compiler.options = options;

    File sourcePath = (File)options.get( OPT_SOURCE_PATH);
    if( sourcePath != null) {
      try
      {
        //System.err.println( "Adding source path: " + sourcePath.toString());
        sourceResolver.addSourceDirectory( sourcePath);
        sourceResolver.addSourceDirectory( ".");
      }
      catch( IOException e) { e.printStackTrace(); System.exit( 1); }
    }
  }

  public static ClassResolver getSystemClassResolver()
  {
    return Compiler.systemResolver;
  }

  public Compiler()
  {
  }

  public Node parse( String filename, Reader source) throws IOException
  {
    Lexer lexer;
    Grm grm;
    java_cup.runtime.Symbol sym;

    lexer = new Lexer( source);
    grm = new Grm(lexer, ts);
               
    try
    {
      sym = grm.parse();
    }
    catch( Exception e)
    {
      throw new IOException( e.getMessage());
    }

    SourceFileNode sfn = (SourceFileNode)sym.value;
    sfn.setFilename( filename);

    return sfn; 
  }

  public ClassResolver readSymbols( Node ast)
  {
    SymbolReader sr = new SymbolReader( ts, parsedResolver);
    ast.visit( sr);

    return sr.getClassResolver();
  }

  public void removeAmbiguities( Node ast)
  {
    AmbiguityRemover ar = new AmbiguityRemover( ts);
    try
    {
      ast.visit( ar);
    }
    catch( TypeCheckError e)
    {
      System.err.println( "Type check error: " + e.getMessage());
      System.exit(1);
    }
  }

  public Node typeCheck( Node ast)
  {
    TypeChecker tc = new TypeChecker( null);
    return ast.visit( tc);
  }

  public void translate( Node ast, Writer output) throws IOException
  {
    CodeWriter cw = new CodeWriter( output, 72);
    ast.translate( null, cw);
    cw.flush();
  }

  // A hack, but only here to get type checking working...
  public static void enqueueError(int line, ErrorInfo e)
  {
    System.err.println( "Error on line " + line + ": " + e.getMessage());
  }
}
