package jltools.main;

import jltools.ast.NodeVisitor;
import jltools.frontend.*;
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

  public MainTargetFactory( String sourceExtension, Collection sourcePath,
                            File outputDirectory, String outputExtension,
                            Boolean stdout)
  {
    this.sourceExtension = sourceExtension;
    this.sourcePath = sourcePath;
    this.outputDirectory = outputDirectory;
    this.outputExtension = outputExtension;
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
                                                  sourceExtension)) 
                             + outputExtension);
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
                                + outputExtension);

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

  public class MainTarget extends Target
  {
    File sourceFile;
    File outputFile;
    Iterator visitors;

    public MainTarget( String name, File sourceFile, File outputFile)
    {
      super( name, null, null);
      this.sourceFile = sourceFile;
      this.outputFile = outputFile;
      this.visitors = null;
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

    public NodeVisitor getNextNodeVisitor( int stage)
    {
      if( visitors == null) {
        visitors = Main.getNodeVisitors( stage);
      }
      
      if( visitors.hasNext()) {
        return (NodeVisitor)visitors.next();
      }
      else {
        visitors = null;
        return null;
      }
    }

    protected ErrorQueue createErrorQueue() throws IOException
    {
      return new MainErrorQueue( name, new FileReader( sourceFile), 
                                 System.err);
    }
  }
   
  class MainErrorQueue extends ErrorQueue
  {
    private static final int ERROR_COUNT_LIMIT = 99;
    
    private String filename;
    private Reader source;
    private PrintStream err;

    private int errorCount;
    private boolean flushed;
    
    public MainErrorQueue( String filename, Reader source, PrintStream err) 
    {
      this.filename = filename;
      this.source = source;
      this.err = err;

      this.errorCount = 0;
      this.flushed = true;
    }
    
    public void enqueue( ErrorInfo e)
    {
      if( e.getErrorKind() != ErrorInfo.WARNING) {
        hasErrors = true;
        errorCount++;
      }
      flushed = false;

      String message = ( e.getErrorKind() != ErrorInfo.WARNING ? e.getMessage()
                           : e.getErrorString() + " -- " + e.getMessage());

      if( e.getLineNumber() == -1) {
        err.println( filename + ": " + message);
      } 
      else {
        err.println( filename + ":" +  e.getLineNumber() + ": " + message);
      }

      if( errorCount > ERROR_COUNT_LIMIT) {
        err.println( filename + ": Too many errors. Aborting compilation.");
        flush();
        throw new ErrorLimitError();
      }
    }
    
    public void flush()
    {
      if( hasErrors && !flushed) {
        err.println( filename + ": " + errorCount + " error" 
                     + (errorCount > 1 ? "s." : "."));
        flushed = true;
      }
    }
  }
}
