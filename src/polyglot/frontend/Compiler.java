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
  public static String OPT_DUMP             = "Dump AST (Boolean)";

  public static int VERSION_MAJOR           = 1;
  public static int VERSION_MINOR           = 0;
  public static int VERSION_PATCHLEVEL      = 0;

  private static int outputWidth;
  private static boolean useFqcn;
  private static boolean dumpAst;
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
    Boolean dump;

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

    dump = (Boolean)options.get( OPT_DUMP);
    if( dump == null) {
      dump = new Boolean( false);
    }
    dumpAst = dump.booleanValue();

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
    Node ast;
    ImportTable it;

    public Job( Target t, ErrorQueue eq) 
    {
      this.t = t;
      this.eq = eq;
      
      ast = null;
      it = null;
    }
    
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
    Job job = new Job( t,eqf.createQueue( t.getName(), t.getSourceReader()));

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

      if( dumpAst) {
        dump( job.ast);
      }

      job.it = readSymbols( job.ast, job.eq);

      /* At this point, if this is not the first thing in the workList
       * then stop and continue later. */
      if( workList.get( 0) != job) {
        job.eq.flush();
        return !(job.eq.hasErrors());
      }

      job.ast = removeAmbiguities( job.ast, job.it, job.eq);

      if( dumpAst) {
        dump( job.ast);
      }

      typeCheck( job.ast, job.it, job.eq);    
      
      if( dumpAst) {
        dump( job.ast);
      }

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

  protected ImportTable readSymbols( Node ast, ErrorQueue eq)
  {
    SymbolReader sr = new SymbolReader( parsedResolver, ts, eq);
    ast.visit( sr);

    return sr.getImportTable();
  }

  protected Node removeAmbiguities( Node ast, ImportTable it, ErrorQueue eq)
  {
    AmbiguityRemover ar = new AmbiguityRemover( ts, it, eq);
    return ast.visit( ar);
  }

  protected Node typeCheck( Node ast, ImportTable it, ErrorQueue eq)
  {
    TypeChecker tc = new TypeChecker( ts, it, eq);
    return ast.visit( tc);
  }

  protected void translate( Target t, Node ast) throws IOException
  {
    SourceFileNode sfn = (SourceFileNode)ast;
    CodeWriter cw = new CodeWriter( t.getOutputWriter( sfn.getPackageName()), 
                                    outputWidth);

    try
    {
      ast.translate( null, cw);
    }
    catch( TypeCheckException e)
    {
      throw new InternalCompilerError( "Caught TypeCheckError during "
                                     + "translation phase: " + e.getMessage());
    }
    cw.flush();
    System.out.flush();
  }

  public void dump( Node ast) throws IOException
  {
    CodeWriter cw = new CodeWriter( new UnicodeWriter( 
                                       new PrintWriter( System.out)), 
                                    outputWidth);
    DumpAst d = new DumpAst( cw);
    ast.visit( d);
    cw.flush();
  }
}
