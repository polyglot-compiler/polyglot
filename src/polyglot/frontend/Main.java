package jltools.frontend;

import java.io.*;
import java.util.*;

import jltools.ast.Node;
import jltools.frontend.Compiler;
import jltools.util.UnicodeWriter;

public class Main
{
   public static final void main(String args[])
   {
      Map options = new HashMap();
      Set source = new TreeSet();

      parseCommandLine(args, options, source);

      Compiler.setOptions( options);

      Compiler compiler = new Compiler();
      Iterator i = source.iterator();
      while( i.hasNext()) {
        compile( compiler, (String)i.next());
      }
   }

  public static void compile( Compiler compiler, String source)
  {
    try
    {
      Reader reader = new FileReader( source);
      Writer writer = new UnicodeWriter( new PrintWriter( System.out));

      Node ast = compiler.parse( source, reader);
      compiler.readSymbols( ast);
      compiler.removeAmbiguities( ast);
      compiler.translate( ast, writer);
    }
    catch( IOException e)
    {
      e.printStackTrace();
    }
  }

  static final void parseCommandLine(String args[], Map options,
                                      Set source)
  {
    if(args.length < 1)
    {
      System.err.println("usage: Main File.java\n");
      System.exit(1);
    }
    
    source.add( args[0]);
  }
}
