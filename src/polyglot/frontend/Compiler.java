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
  private static LoadedClassResolver loadedResolver;

  private static Map options;

  static
  {
    Compiler.systemResolver = new CompoundClassResolver();
    Compiler.parsedResolver = new TableClassResolver();
    systemResolver.addClassResolver( parsedResolver);

    //systemResolver.addClassResolver( new FileClassResolver());

    Compiler.loadedResolver = new LoadedClassResolver();
    systemResolver.addClassResolver( loadedResolver);

    Compiler.ts = new StandardTypeSystem( systemResolver);
    Compiler.loadedResolver.setTypeSystem( Compiler.ts);
    
    Compiler.options = new HashMap();
  }

  public static void setOptions( Map options)
  {
    Compiler.options = options;
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

  public void readSymbols( Node ast)
  {
    SymbolReader sr = new SymbolReader( ts, parsedResolver);
    ast.visit( sr);
  }

  public void removeAmbiguities( Node ast)
  {
    AmbiguityRemover ar = new AmbiguityRemover( ts);
    ast.visit( ar);
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
