package jltools.types;

import jltools.frontend.Compiler;
import jltools.frontend.*;
import jltools.ast.Node;

import jltools.types.*;

import java.io.*;
import java.util.*;

public class SourceFileClassResolver implements ClassResolver
{
  TargetFactory tf;

  public SourceFileClassResolver( TargetFactory tf)
  {
    this.tf = tf;
  }

  public ClassType findClass( String name) throws NoClassException
  {
    Target t;
    
    try
    {
      t = tf.createClassTarget( name);
    }
    catch( IOException e1)
    {
      // e1.printStackTrace();
      throw new NoClassException( "Class " + name + " not found.");
    }

    Compiler compiler = new Compiler();
    try
    {
      if( !compiler.compile( t)) {
        throw new NoClassException( "Errors while parsing " + t.getName());
      }
    }
    catch( IOException e2)
    {
      throw new NoClassException( "IOException while reading "
                                  + t.getName() + ": " + e2.getMessage());
    }

    return Compiler.getParsedClassResolver().findClass( name);
  }

  public void findPackage( String name) throws NoClassException {}
}
