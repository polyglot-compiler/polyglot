package jltools.main;

import jltools.ast.NodeVisitor;
import jltools.frontend.*;
import jltools.types.ClassType;
import jltools.util.*;

import java.io.*;
import java.util.*;


public class MainTargetFactory implements TargetFactory
{
  Options options;
  String outputExtension;
  String sourceExtension;
  public MainTargetFactory(Options options_) {
    options = options_;
    outputExtension = options.output_ext;
    sourceExtension = options.extension.fileExtension();
  }

  private String fixExt(String name) {
      return name.substring(0, name.lastIndexOf(sourceExtension)) +
	      outputExtension;
  }

  public Target createFileTarget( String fileName) throws IOException
  {
    File sourceFile = new File( fileName);
    File outputFile;

    if( !sourceFile.exists()) {
      throw new FileNotFoundException( fileName);
    }

    if (fileName.indexOf(sourceExtension) == -1) {
      throw new IOException( "All source files must have the same extension.");
    }

    if (options.output_directory == null) {
      /* Then it goes with the source. */
      File parentDirectory = sourceFile.getParentFile();
      String name = sourceFile.getName();
      outputFile = new File(parentDirectory, fixExt(name));
      if (sourceFile.equals(outputFile)) {
        outputFile = new File( parentDirectory, fixExt(name) + "$");
      }
    }
    else {
      /* Otherwise, we can't tell until we have the package name. */
      outputFile = null;
    }
  
    return new MainTarget( sourceFile.getName(), sourceFile.getPath(), 
                           sourceFile, outputFile);
  }

  public Target createClassTarget( String className) throws IOException
  {
    /* Search the source path. */
    File sourceFile = null;
    File directory;
    File outputFile = new File( options.output_directory,
                                className.replace( '.', File.separatorChar)
                                + outputExtension);

    String fileName = className.replace('.', File.separatorChar) 
                                         + sourceExtension;
    Iterator iter = options.source_path.iterator();

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
        outputFile = new File( options.output_directory, 
                               className.replace( '.', File.separatorChar)
                               + outputExtension + "$");
    }

    return new MainTarget( sourceFile.getName(), sourceFile.getPath(), 
                           sourceFile, outputFile);
  }

  public Target createClassTarget( ClassType classType) throws IOException
  {
    return new MainTarget( classType.getShortName() + ".class", 
                           classType.getFullName() + ".class", null, null);
  }

  public class MainTarget extends Target
  {
    String fullName;
    File sourceFile;
    File outputFile;
    FileReader sourceFileReader;
    Writer outputWriter;
    Iterator visitors;

    Date lastModified;
    String outputFileName;


    public MainTarget( String name, String fullName,
                       File sourceFile, File outputFile)
    {
      super( name, null, null);
      this.fullName = fullName;
      this.sourceFile = sourceFile;
      this.outputFile = outputFile;
      this.visitors = null;
      sourceFileReader = null;
      outputWriter = null;

      if( sourceFile != null) {
        lastModified = new Date( sourceFile.lastModified());
      }
    }

    public Collection outputFiles() {
	LinkedList ret = new LinkedList();
	ret.add(outputFile);
	return ret;
    }

    public Reader getSourceReader() throws IOException
    {
      if (sourceFileReader != null) return sourceFileReader;
      return (sourceFileReader = new FileReader( sourceFile));
    }

    public Writer getOutputWriter( String packageName) throws IOException
    {
      if (outputWriter != null) return outputWriter;

      if( options.output_stdout) {
        return (outputWriter = new UnicodeWriter( 
                                     new PrintWriter( System.out)));
      }
      else {
        if( outputFile == null) {
          if( packageName == null) {
            outputFile = new File(options.output_directory, 
                                  File.separatorChar + fixExt(name));
          }
          else {
            outputFile = new File( options.output_directory, 
                                   packageName.replace( '.', 
                                                        File.separatorChar) +
                                     File.separatorChar + fixExt(name));
          }
        }
        if( !outputFile.getParentFile().exists()) {
          File parent = outputFile.getParentFile();
          parent.mkdirs();
        }

        outputFileName = outputFile.getPath();

        return (outputWriter = new UnicodeWriter( 
                                 new FileWriter( outputFile)));
      }
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
    protected ErrorQueue createErrorQueue() throws IOException
    {
      return new MainErrorQueue( name, System.err);
    }

    public Date getLastModifiedDate()
    {
      return lastModified;
    }

    public boolean equals( Object o)
    {
      if( o instanceof MainTarget) {
        return fullName.equals( ((MainTarget)o).fullName);
      }
      else {
        return false;
      }
    }
  }
   
  static class MainErrorQueue extends ErrorQueue
  {
    private static final int ERROR_COUNT_LIMIT = 99;
    
    private String filename;
    private PrintStream err;

    private int errorCount;
    private boolean flushed;
    
    public MainErrorQueue( String filename, PrintStream err) 
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
