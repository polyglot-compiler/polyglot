package jltools.frontend;

import jltools.lex.Lexer;
import jltools.ast.Node;
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

  private static Map options;

  static
  {
    Compiler.systemResolver = new CompoundClassResolver();
    Compiler.parsedResolver = new TableClassResolver();
    systemResolver.addResolver( parsedResolver);
    //systemResolver.addResolver( new FileClassResolver());

    Compiler.ts = new StandardTypeSystem( systemResolver);

    Compiler.options = new HashMap();
  }

  public static void setOptions( Map options)
  {
    Compiler.options = options;
  }

  public Compiler()
  {
  }

  public Node parse( Reader source) throws IOException
  {
    Lexer lexer;
    Grm grm;
    java_cup.runtime.Symbol sym;

    lexer = new Lexer( source);
    grm = new Grm(lexer, null);
               
    try
    {
      sym = grm.parse();
    }
    catch( Exception e)
    {
      throw new IOException( e.getMessage());
    }

    return (Node)sym.value; 
  }

  public void readSymbols( Node ast)
  {
    SymbolReader sr = new SymbolReader( ts, parsedResolver);
    ast.visit( sr);
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
