package jltools.types;

import jltools.frontend.*;
import jltools.ast.Node;
import jltools.visit.ClassSerializer;
import jltools.util.*;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public class LoadedClassResolver implements ClassResolver
{
  TargetFactory tf;
  TargetTable tt;
  TypeSystem ts;
  TypeEncoder te;

  public LoadedClassResolver( TargetFactory tf, TargetTable tt,
                              TypeSystem ts)
  {
    this.tf = tf;
    this.tt = tt;
    this.ts = ts;
    this.te = new TypeEncoder( ts);
  }

  public ClassType findClass( String name) throws SemanticException
  {
    Target t;
    Class clazz;
    ClassType ct;
    
    /* First try and find the source file. */
    try
    {
      t = tf.createClassTarget( name);
    }
    catch( IOException e1)
    {
      /* No source file, look for the class file. */
      try
      {
        clazz = Class.forName( name);
      }
      catch( Exception e)
      {
        throw new NoClassException( "Class " + name + " not found.");
      }
      return getTypeFromClass( clazz);
    }

    /* Now look for the class file. */
    try
    {
      clazz = Class.forName( name);
    }
    catch( Exception e)
    {
      /* No class file, so we must compile from the source. */
      // System.err.println( "No class file for " + name + ".");
      return getTypeFromTarget( t, name);
    }

    /* Now we have both source and class files. That is, we have a target
     * for the source file and a java.lang.Class for the class. We need to 
     * figure out whether or not the class file is up to date and if
     * it contains any jlc information. */
    try
    {
      Field field = clazz.getDeclaredField( "jlc$CompilerVersion");
      int i = checkCompilerVersion( (String)field.get( null));
      if( i != 0) {
        /* Incompatible or older version, so go with the source. */
        // System.err.println( "Incompatible version for " + name + ".");
        return getTypeFromTarget( t, name);
      }

      field = clazz.getDeclaredField( "jlc$SourceLastModified");
      if( !checkSourceModificationDate( (Long)field.get( null), 
                                    t.getLastModifiedDate())) {
        // System.err.println( "More recent source for " + name + ".");
        return getTypeFromTarget( t, name);
      }

      /* Okay, the class file is good enough. De-serialize the ClassType. */
      return getTypeFromClass( clazz);
    }
    catch( Exception e)
    {
      return getTypeFromTarget( t, name);
    } 
  }

  protected ClassType getTypeFromTarget( Target t, String name) 
    throws SemanticException
  {
    ClassResolver cr;

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

    // System.err.println( "Returning ParsedClassType for " + name + "...");
    return cr.findClass( name);
  }

  protected ClassType getTypeFromClass( Class clazz)
    throws SemanticException
  {
    /* At this point we've decided to go with the Class. So if something
     * goes wrong here, we have only one choice, to throw an exception. */
    try
    {
      /* Check to see if it has serialized info. If so then check the
       * version. */
      Field field = clazz.getDeclaredField( "jlc$CompilerVersion");
      int i = checkCompilerVersion( (String)field.get( null));
      if( i < 0) {
        // System.err.println( "Throwing exception for " + clazz.getName() + " (Bad Version)...");
        throw new SemanticException( "Unable to find a suitable definition of "
                                     + clazz.getName() 
                                     + ". Try recompiling or obtaining "
                                     + " a newer version of the class file.");
      }
      
      /* Alright, go with it! */
      field = clazz.getDeclaredField( "jlc$ClassType");
      
      ClassType ct = (ClassType)te.decode( (String)field.get( null));
      
      // System.err.println( "Returning serialized ClassType for " + clazz.getName() + "...");

      /* Add the class to the target list so that it will be cleaned later. */
      tt.addTarget( ct);
      // ((ClassTypeImpl)ct).dump();
      return ct;
    }
    catch( SemanticException e)
    {
      throw e;
    }
    catch( NoSuchFieldException e)
    {
      // System.err.println( "Returning LoadedClassType for " + clazz.getName() + " (Not Serialized)...");
      return new LoadedClassType( ts, clazz);
    }
    catch( Exception e)
    {
      e.printStackTrace();
      // System.err.println( "Returning LoadedClassType for " + clazz.getName() + " (Error While Deserializing)...");
      return new LoadedClassType( ts, clazz);
    }
  }

  protected int checkCompilerVersion( String clazzVersion)
  {
    StringTokenizer st = new StringTokenizer( clazzVersion, ".");
    int v = Integer.parseInt( st.nextToken());
    if( v != jltools.frontend.Compiler.VERSION_MAJOR) {
      /* Incompatible. */
      return -1;
    }
    v = Integer.parseInt( st.nextToken());
    if( v != jltools.frontend.Compiler.VERSION_MINOR) {
      /* Not the best option, but will work if its the only one. */
      return 1;
    }

    /* Everything is way cool. */
    return 0;
  }

  protected boolean checkSourceModificationDate( Long time, Date target)
  {
    return time.longValue() >= target.getTime();
  }

  public void findPackage( String name) throws NoClassException {}
}
