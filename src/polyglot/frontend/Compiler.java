package jltools.frontend;

import jltools.lex.Lexer;
import jltools.parse.Grm;

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
      
      targets = (String[])options.get(TARGETS);

      for(int i = 0; i < targets.length; i++)
      {
         try
         {
            reader = new FileReader(targets[i]);
            lexer = new Lexer(reader);
            grm = new Grm(lexer);
               
            java_cup.runtime.Symbol sym = grm.parse();
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }
}
