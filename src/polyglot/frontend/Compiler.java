package jltools.frontend;

import jltools.lex.Lexer;
import jltools.ast.Node;
import jltools.parse.Grm;
import jltools.util.UnicodeWriter;
import jltools.util.CodeWriter;

import java.io.Reader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Map;

public class Compiler
{
   public static final String TARGETS = "Target Files";

   public Compiler(Map options)
   {
      String[] targets;
      Reader reader;
      Lexer lexer;
      Grm grm;
      CodeWriter cw;
      
      targets = (String[])options.get(TARGETS);

      for(int i = 0; i < targets.length; i++)
      {
         try
         {
            reader = new FileReader(targets[i]);
            lexer = new Lexer(reader);
            grm = new Grm(lexer, null);
               
            java_cup.runtime.Symbol sym = grm.parse();
            cw = new CodeWriter(new UnicodeWriter( 
                                  new PrintWriter(System.out)), 72);
            
            ((Node)sym.value).translate(null, cw);
            cw.flush();
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }
}
