package jltools.frontend;

import jltools.util.*;

import java.io.*;
import java.util.*;


public class MainTargetFactory implements TargetFactory
{
  String sourceExtension;
  Collection sourcePath;
  File outputDirectory;
  String outputExtension;
  boolean stdout;
  Set commandLineSource;

  public MainTargetFactory( String sourceExtension, Collection sourcePath,
                            File outputDirectory, String outputExtension,
                            Boolean stdout, Set commandLineSource)
  {
    this.sourceExtension = sourceExtension;
    this.sourcePath = sourcePath;
    this.outputDirectory = outputDirectory;
    this.outputExtension = outputExtension;
    this.stdout = (stdout != null) && (stdout.booleanValue());
    this.commandLineSource = commandLineSource;
  }

  public void addSourceDirectory( File directory)
  {
    sourcePath.add( directory);
  }

  public void addSourceDirectory( Target t, String packageName) 
    throws IOException
  {
    if (packageName == null) 
      return;
    MainTarget mt = (MainTarget)t;
    String path = mt.sourceFile.getAbsolutePath();
    
    int index = path.indexOf( packageName.replace( '.', File.separatorChar));
    if( index < 0) {
      throw new IOException( "Target not found in appropriate directory.");
    }

    File directory = new File( path.substring( 0, index));
    if( !directory.exists()) {
      throw new FileNotFoundException( 
                          "Unable to find base package directory.");
    }

    addSourceDirectory( directory);
  }


  public Target createFileTarget( String fileName) throws IOException
  {
    File sourceFile = new File( fileName);
    File outputFile;

    if( !sourceFile.exists()) {
      throw new FileNotFoundException( fileName);
    }

    if( outputDirectory == null) {
      /* Then it goes with the source. */
      File parentDirectory = sourceFile.getParentFile();
      String name = sourceFile.getName();
      outputFile = new File( parentDirectory, 
                             name.substring( 0, name.lastIndexOf(
                                                  sourceExtension)) 
                             + outputExtension);
    }
    else {
      /* Otherwise, we can't tell until we have the package name. */
      outputFile = null;
    }
  
    return new MainTarget( sourceFile.getName(), sourceFile, outputFile);
  }

  protected String findSourceName( Set source, String fileName)
  {
    Iterator iter = source.iterator();
    String name;

    while( iter.hasNext()) {
      name = (String)iter.next();
      if( name.endsWith( fileName)) {
        return name;
      }
    }
    return null;
  }

  public Target createClassTarget( String className) throws IOException
  {
    /* Search the source path. */
    File sourceFile = null, directory;
    File outputFile = new File( outputDirectory, 
                                className.replace( '.', File.separatorChar)
                                + outputExtension);
    
    /* First check in the source files on the command line. */
    int index = className.lastIndexOf( '.');
    String fileName = (index == -1 ? className :
                       className.substring( index + 1)) + sourceExtension;

    if( (fileName = findSourceName( commandLineSource, fileName)) != null) {
      if( index == -1) {
        /* It's in the default package, so there's nothing else we can
         * check. */
        sourceFile = new File( fileName);
        if( sourceFile.exists()) {
          return new MainTarget( sourceFile.getName(), sourceFile, outputFile);
        }
        /* Otherwise, continue below with the source path. */
      }
      else {
        /* Confirm that the file sits in the proper directory structure. */
        sourceFile = new File( fileName);

        String packages = className.substring( 0, index);
        String lastPackage;
        directory = sourceFile.getParentFile();

        while( true) {
          index = packages.lastIndexOf( '.');
          lastPackage = (index == -1 ? packages :
                                  packages.substring( index + 1));

          if( !directory.getName().equals( lastPackage)) {
            break;
          }
          
          if( index < 0) {
            return new MainTarget( sourceFile.getName(), sourceFile, 
                                   outputFile);
          }

          packages = packages.substring( 0, index);
          directory = directory.getParentFile();
        }
      }
    }


    /* Now search the source path. */
    fileName = className.replace( '.', File.separatorChar) 
                                         + sourceExtension;
    Iterator iter = sourcePath.iterator();

    while( iter.hasNext())
    {
      directory = (File)iter.next();

      sourceFile = new File( directory, fileName);

      if( sourceFile.exists()) {
        break;
      }
    }

    if( !sourceFile.exists()) {
      throw new FileNotFoundException( fileName);
    }    

    return new MainTarget( sourceFile.getName(), sourceFile, outputFile);
  }

  public class MainTarget extends Target
  {
    File sourceFile;
    File outputFile;

    public MainTarget( String name, File sourceFile, File outputFile)
    {
      super( name, null, null);
      this.sourceFile = sourceFile;
      this.outputFile = outputFile;
    }

    public Reader getSourceReader() throws IOException
    {
      return new FileReader( sourceFile);
    }

    public Writer getOutputWriter( String packageName) throws IOException
    {
      if( stdout) {
        return new UnicodeWriter( new PrintWriter( System.out));
      }
      else {
        if( outputFile == null) {
          if( packageName == null) {
            outputFile = new File( outputDirectory, 
                                   File.separatorChar
                                   + name.substring( 0, name.lastIndexOf( 
                                       sourceExtension))
                                   + outputExtension);
          }
          else {
            outputFile = new File( outputDirectory, 
                                   packageName.replace( '.', 
                                       File.separatorChar)
                                   + File.separatorChar
                                   + name.substring( 0, name.lastIndexOf( 
                                       sourceExtension))
                                   + outputExtension);
          }
        }
        if( !outputFile.getParentFile().exists()) {
          File parent = outputFile.getParentFile();
          parent.mkdirs();
        }
        return new UnicodeWriter( new FileWriter( outputFile));
      }
    }

  }
}
