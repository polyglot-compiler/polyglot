package jltools.frontend;

import jltools.util.*;

import java.io.*;
import java.util.*;


public class MainTargetFactory implements TargetFactory
{
  String sourceExtension;
  Collection sourcePath;
  File outputDirectory;
  boolean stdout;

  public MainTargetFactory( String sourceExtension, Collection sourcePath,
                            File outputDirectory, Boolean stdout)
  {
    this.sourceExtension = sourceExtension;
    this.sourcePath = sourcePath;
    this.outputDirectory = outputDirectory;
    this.stdout = (stdout != null) && (stdout.booleanValue());
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
                                                  sourceExtension)) + ".java");
    }
    else {
      /* Otherwise, we can't tell until we have the package name. */
      outputFile = null;
    }
  
    return new MainTarget( sourceFile.getName(), sourceFile, outputFile);
  }

  public Target createClassTarget( String className) throws IOException
  {
    /* Search the source path. */
    File sourceFile = null, directory;
    File outputFile = new File( outputDirectory, 
                                className.replace( '.', File.separatorChar)
                                + ".java");

    String fileName = className.replace( '.', File.separatorChar) 
                                         + sourceExtension;
    Iterator iter = sourcePath.iterator();

    // System.err.println( "Trying to find source for: " + fileName);

    while( iter.hasNext())
    {
      directory = (File)iter.next();

      sourceFile = new File( directory, fileName);

      // System.err.println( "Trying: " + sourceFile.toString());
      
      if( sourceFile.exists()) {
        // System.err.println( "Success!!");
        break;
      }
    }

    if( !sourceFile.exists()) {
      throw new FileNotFoundException( fileName);
    }    

    return new MainTarget( sourceFile.getName(), sourceFile, outputFile);
  }

  class MainTarget extends Target
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
          outputFile = new File( outputDirectory, 
                                 packageName.replace( '.', File.separatorChar)
                                 + File.separatorChar
                                 + name.substring( 0, name.lastIndexOf( 
                                     sourceExtension))
                                 + ".java");
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
