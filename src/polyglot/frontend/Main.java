package jltools.frontend;


import java.io.*;
import java.util.*;

public class Main
{
   public static final void main(String args[])
   {
      Map options = new HashMap();

      parseCommandLine(args, options);

      new Compiler(options);
   }

   static final void parseCommandLine(String args[], Map options)
   {
      if(args.length < 1)
      {
         System.err.println("usage: Main File.java\n");
         System.exit(1);
      }

      String[] target_files = new String[1];
      target_files[0] = args[0];
      options.put(Compiler.TARGETS, target_files);
   }
}
