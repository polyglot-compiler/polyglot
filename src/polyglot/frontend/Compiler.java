package jltools.frontend;

import jltools.lex.Lexer;
import jltools.ast.Node;
import jltools.ast.SourceFileNode;
import jltools.parse.Grm;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;

import java.io.*;
import java.util.*;

public class Compiler
{
  private static TypeSystem ts;
  private static CompoundClassResolver systemResolver;
  private static TableClassResolver parsedResolver;
  private static SourceFileClassResolver sourceResolver;
  private static LoadedClassResolver loadedResolver;

  private static Map options;
  public static String OPT_OUTPUT_WIDTH     = "Output Width (Integer)";
  public static String OPT_VERBOSE          = "Verbose (Boolean)";
  public static String OPT_FQCN             = "FQCN (Boolean)";

  public static int VERSION_MAJOR           = 1;
  public static int VERSION_MINOR           = 0;
  public static int VERSION_PATCHLEVEL      = 0;

  private static int outputWidth;
  private static boolean useFqcn;
  private static Collection compilers;
  protected static TargetFactory tf;
  protected static ErrorQueueFactory eqf;
  protected static List workList;

  private static boolean initialized = false;


  public static void initialize( Map options, TargetFactory tf, 
                                 ErrorQueueFactory eqf)
  {
    Compiler.options = options;
    Compiler.tf = tf;
    Compiler.eqf = eqf;
    Integer width;
    Boolean fqcn;

    /* Read the options. */
    width = (Integer)options.get( OPT_OUTPUT_WIDTH);
    if( width == null) {
      width = new Integer( 72);
    }
    outputWidth = width.intValue();

    fqcn = (Boolean)options.get( OPT_FQCN);
    if( fqcn == null) {
      fqcn = new Boolean( false);
    }
    useFqcn = fqcn.booleanValue();

    /* Set up the resolvers. */
    systemResolver = new CompoundClassResolver();
    
    parsedResolver = new TableClassResolver();
    systemResolver.addClassResolver( parsedResolver);

    sourceResolver = new SourceFileClassResolver( tf);
    systemResolver.addClassResolver( sourceResolver);

    loadedResolver = new LoadedClassResolver();
    systemResolver.addClassResolver( loadedResolver);

    ts = new StandardTypeSystem( systemResolver);

    loadedResolver.setTypeSystem( Compiler.ts);

    System.out.println("Going to init type system");
    try
    {
      ts.initializeTypeSystem();
    }
    catch( TypeCheckException e)
    {
      throw new InternalCompilerError( "Unable to initialize compiler. " + 
                                       "Failed to initialize type system: " +
                                       e.getMessage());
    }


    /* Other setup. */
    compilers = new LinkedList();
    workList = Collections.synchronizedList( new LinkedList());

    initialized = true;

    System.out.println("Setup done.");
  }

  public static boolean cleanup() throws IOException
  {
    Compiler compiler = new Compiler();
    boolean okay = true;

    for( int i = 0; i < workList.size(); i++)
    {
      Job job = (Job)workList.get( i);
      okay = okay && compiler.compile( job.t);
    }
    return okay;
  }

  public static ClassResolver getSystemClassResolver()
  {
    return systemResolver;
  }
  
  public static ClassResolver getParsedClassResolver()
  {
    return parsedResolver;
  }

  public static boolean useFullyQualifiedNames()
  {
    return useFqcn;
  }

  static class Job
  {
    Target t;
    ErrorQueue eq;
    Node ast = null;
    
    boolean hasErrors = false;

    public boolean equals( Object o) {
      if( o instanceof Job) {
        return t.equals( ((Job)o).t);
      }
      else {
        return false;
      }
    }
  }



  public Compiler()
  {
    if( !initialized) {
      throw new Error( "Unable to construct compiler instance before static " 
                       + "initialization.");
    }
  }

  public boolean compileFile( String filename) throws IOException 
  {
    return compile( tf.createFileTarget( filename)) ;
  }

  public boolean compileClass( String classname) throws IOException
  {
    return compile( tf.createClassTarget( classname));
  }

  public boolean compile( Target t) throws IOException
  {
    Job job = new Job();
    job.t = t;
    job.eq = eqf.createQueue( t.getName(), t.getSourceReader());

    if( workList.contains( job)) {
      job = (Job)workList.get( workList.indexOf( job));
      if( job.eq.hasErrors()) {
        return false;
      }
    }
    else {
      workList.add( job);
    }
     
    try
    {
      if( job.ast == null) {
        job.ast = parse( job.t, job.eq);

        if( job.eq.hasErrors()) {
          job.eq.flush();
          return false;
        }
      }

      //dump( job.ast);

      readSymbols( job.ast, job.eq);

      /* At this point, if this is not the first thing in the workList
       * then stop and continue later. */
      if( workList.get( 0) != job) {
        job.eq.flush();
        return !(job.eq.hasErrors());
      }

      removeAmbiguities( job.ast, job.eq);

      // typeCheck( job.ast, job.eq);    

      if( !job.eq.hasErrors()) {
        translate( job.t, job.ast);
      }
    }
    catch( IOException e)
    {
      job.eq.enqueue( ErrorInfo.IO_ERROR, 
                      "Encounted an I/O error while compiling.");
      job.eq.flush();
      throw e;
    }

    job.eq.flush();

    if( job.eq.hasErrors()) {
      return false;
    }
    else {
      workList.remove( job);
      return true;
    }
  }

  protected Node parse( Target t, ErrorQueue eq) throws IOException
  {
    Lexer lexer;
    Grm grm;
    java_cup.runtime.Symbol sym = null;

    lexer = new Lexer( t.getSourceReader(), eq);
    grm = new Grm( lexer, ts, eq);
               
    try
    {
      sym = grm.parse();
    }
    catch( IOException e)
    {
      eq.enqueue( ErrorInfo.IO_ERROR, e.getMessage());
      throw e;
    }
    catch( Exception e)
    {
      eq.enqueue( ErrorInfo.INTERNAL_ERROR, e.getMessage());
      return null;
    }

    /* Try and figure out whether or not the parser was successful. */
    if( sym == null) {
      eq.enqueue( ErrorInfo.SYNTAX_ERROR, "Unable to parse source file.");
      return null;
    }

    if( sym.value instanceof SourceFileNode) {
      ((SourceFileNode)sym.value).setFilename( t.getName());
    }

    if( !(sym.value instanceof Node)) {
      eq.enqueue( ErrorInfo.SYNTAX_ERROR, "Unable to parse source file.");
      return null;
    }
    else {
      return (Node)sym.value; 
    }
  }

  protected Node readSymbols( Node ast, ErrorQueue eq)
  {
    SymbolReader sr = new SymbolReader( parsedResolver, ts, eq);
    return ast.visit( sr);
  }

  protected Node removeAmbiguities( Node ast, ErrorQueue eq)
  {
    AmbiguityRemover ar = new AmbiguityRemover( ts, eq);
    return ast.visit( ar);
  }

  protected Node typeCheck( Node ast, ErrorQueue eq)
  {
    TypeChecker tc = new TypeChecker( null, eq);
    return ast.visit( tc);
  }

  protected void translate( Target t, Node ast) throws IOException
  {
    SourceFileNode sfn = (SourceFileNode)ast;
    CodeWriter cw = new CodeWriter( t.getOutputWriter( sfn.getPackageName()), 
                                    outputWidth);

    ast.translate( null, cw);
    cw.flush();
    System.out.flush();
  }

  public void dump( Node ast) throws IOException
  {
    CodeWriter cw = new CodeWriter( new UnicodeWriter( 
                                       new PrintWriter( System.err)), 
                                    outputWidth);
    DumpAst d = new DumpAst( cw);
    ast.visit( d);
    cw.flush();
  }
}
