package jltools.types;

import jltools.frontend.Compiler;
import jltools.ast.Node;

import java.io.*;
import java.util.*;

public class SourceFileClassResolver implements ClassResolver
{
  String sourceExtension;
  Collection sourcePath;

  public SourceFileClassResolver()
  {
    this( "");
  }

  public SourceFileClassResolver( String extension)
  {
    this( extension, new LinkedList());
  }

  public SourceFileClassResolver( Collection path)
  {
    this( "", path);
  }

  public SourceFileClassResolver( String extension, Collection path)
  {
    sourceExtension = extension;
    sourcePath = path;
  }

  public void setExtension( String extension)
  {
    sourceExtension = extension;
  }

  public void addSourceDirectory( String directory) 
       throws FileNotFoundException, IOException
  {
    File dir = new File( directory);
    addSourceDirectory( dir);
  }

  public void addSourceDirectory( File dir)
    throws FileNotFoundException, IOException
  {
    String directory = dir.toString();
    if( !dir.exists()) {
      throw new FileNotFoundException( "Can't add directory " + directory 
                                       + " to source path.");
    }
    if( !dir.isDirectory()) {
      throw new IOException( "Can't read directory " + directory 
                             + " which was added to source path.");
    }
    sourcePath.add( dir);
  }

  

  public JavaClass findClass( String name) throws NoClassException
  {
    String filename = name.replace( '.', File.separatorChar) + sourceExtension;
    Iterator iter = sourcePath.iterator();
    File dir, classfile;
    //System.err.println( "Trying to find source for: " + name);

    while( iter.hasNext())
    {
      dir = (File)iter.next();

      classfile = new File( dir, filename);

      //System.err.println( "Trying: " + classfile.toString());
      
      if( !classfile.exists()) {
        continue;
      }
      
      //System.err.println( "Success!!");
      Compiler compiler;
      Node ast;
      ClassResolver resolver;
      
      compiler = new Compiler();
      try
      {
        ast = compiler.parse( filename, new FileReader( classfile));
      }
      catch( IOException e)
      {
        throw new NoClassException( "IOException while reading source file "
                                    + filename + ": " + e.getMessage());
      }
      resolver = compiler.readSymbols( ast);
      //((TableClassResolver)resolver).dump();
      return resolver.findClass( name);
    }
    throw new NoClassException( "Class " + name + " not found.");

  }

  public void findPackage( String name) throws NoClassException {}
}
