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

    if( fileName.indexOf( sourceExtension) == -1) {
      throw new IOException( "All source files must have the same extension.");
    }

    if( outputDirectory == null) {
      /* Then it goes with the source. */
      File parentDirectory = sourceFile.getParentFile();
      String name = sourceFile.getName();
      outputFile = new File( parentDirectory, 
                             name.substring( 0, name.lastIndexOf(
                                                  sourceExtension)) 
                             + outputExtension);
      if( sourceFile.equals( outputFile)) {
        outputFile = new File( parentDirectory, 
                               name.substring( 0, name.lastIndexOf(
                                                    sourceExtension)) 
                               + outputExtension + "$");
      }
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

    if( sourceFile.equals( outputFile)) {
        outputFile = new File( outputDirectory, 
                               className.replace( '.', File.separatorChar)
                               + outputExtension + "$");
    }

    return new MainTarget( sourceFile.getName(), sourceFile, outputFile);
  }

  public class MainTarget extends Target
  {
    File sourceFile;
    File outputFile;
    FileReader sourceFileReader;
    Writer outputWriter;
    Iterator visitors;

    public MainTarget( String name, File sourceFile, File outputFile)
    {
      super( name, null, null);
      this.sourceFile = sourceFile;
      this.outputFile = outputFile;
      this.visitors = null;
      sourceFileReader = null;
      outputWriter = null;
    }

    public Reader getSourceReader() throws IOException
    {
      if (sourceFileReader != null) return sourceFileReader;
      return (sourceFileReader = new FileReader( sourceFile));
    }

    public Writer getOutputWriter( String packageName) throws IOException
    {
      if (outputWriter != null) return outputWriter;
      if( stdout) {
        return (outputWriter = new UnicodeWriter( new PrintWriter( System.out)));
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
        return (outputWriter = new UnicodeWriter( new FileWriter( outputFile)));
      }
    }

    public java_cup.runtime.lr_parser getParser() throws IOException
    {
      jltools.lex.Lexer lexer = new jltools.lex.Lexer( getSourceReader(), 
                                                       getErrorQueue());
      return Main.getParser( lexer, getErrorQueue());
    }

    public void closeSource() throws IOException
    {
      if ( sourceFileReader != null) sourceFileReader.close();
      sourceFileReader =null;
      sourceFile = null;
      
    }
    public void closeDestination() throws IOException
    {
      if ( outputWriter != null) outputWriter.close();
      outputWriter = null;
      outputFile = null;

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
      return new MainErrorQueue( name, System.err);
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
    
    public MainErrorQueue( String filename,  PrintStream err) 
    {
      this.filename = filename;
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
