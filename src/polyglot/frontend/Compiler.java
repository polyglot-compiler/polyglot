package jltools.frontend;

import jltools.lex.Lexer;
import jltools.ast.Node;
import jltools.parse.Grm;
import jltools.types.Context;
import jltools.util.CodeWriter;

import java.io.Reader;
import java.io.FileReader;
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
            grm = new Grm(lexer);
               
            java_cup.runtime.Symbol sym = grm.parse();
            cw = new CodeWriter(System.out, 72);
            
            ((Node)sym.value).translate(new Context(), cw);
            cw.flush();
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }
}
