package jltools.types;

import jltools.frontend.*;
import jltools.ast.Node;

import jltools.types.*;

import java.io.*;
import java.util.*;

public class SourceFileClassResolver implements ClassResolver
{
  TargetFactory tf;
  TargetTable tt;

  public SourceFileClassResolver( TargetFactory tf, TargetTable tt)
  {
    this.tf = tf;
    this.tt = tt;
  }

  public ClassType findClass( String name) throws TypeCheckException
  {
    Target t;
    ClassResolver cr;
    
    try
    {
      t = tf.createClassTarget( name);
    }
    catch( IOException e1)
    {
      throw new NoClassException( "Class " + name + " not found.");
    }

    try
    {
      cr = tt.getResolver( t);
      if( cr == null) {
        throw new NoClassException( "Errors while parsing " + t.getName());
      }
    }
    catch( IOException e2)
    {
      throw new NoClassException( "IOException while reading "
                                  + t.getName() + ": " + e2.getMessage());
    }

    return cr.findClass( name);
  }

  public void findPackage( String name) throws NoClassException {}
}
