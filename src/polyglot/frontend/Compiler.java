package jltools.frontend;

import jltools.ast.*;
import jltools.parse.*;
import jltools.lex.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;

import jltools.ext.jif.visit.*;
import jltools.ext.jif.types.*;

import splitter.config.*;
import splitter.*;

import java.io.*;
import java.util.*;

/**
 * This is the main entry point for the compiler. It contains a work list that
 * contains entries for all classes that must be compiled. 
 */
public class Compiler implements TargetTable, ClassCleaner
{
  /* Global constants. */
  public static String OPT_OUTPUT_WIDTH     = "Output Width (Integer)";
  public static String OPT_VERBOSE          = "Verbose (Boolean)";
  public static String OPT_FQCN             = "FQCN (Boolean)";
  public static String OPT_SERIALIZE        = "Class Serialization (Boolean)";

  /** 
   * Marks major changes in the output format of the files produced by the
   * compiler. Files produced be different major versions are considered 
   * incompatible and will not be used as source of class information.
   */
  public static int VERSION_MAJOR           = 1;
  
  /** 
   * Indicates a change in the compiler that does not affect the output format.
   * Source files will be prefered over class files build by compilers with
   * different minor versions, but if no source file is available, then the
   * class file will be used.
   */
  public static int VERSION_MINOR           = 0;

  /**
   * Denote minor changes and bugfixes to the compiler. Class files compiled
   * with versions of the compiler that only differ in patchlevel (from the
   * current instantiation) will always be preferred over source files (unless
   * the source files have newer modification dates).
   */
  public static int VERSION_PATCHLEVEL      = 0;

  /* Global options and state. */
  private static Map options;
  private static int outputWidth;
  private static boolean useFqcn;
  private static boolean serialize;
  private static boolean verbose;
  private static boolean jif;
  private static Config jifSplitConfig;

  private static boolean initialized = false;

  /* Stage defining constants. */
  /**
   * The first stage of the compiler. During this stage, lexical and syntatic
   * analysis are performed on the input source. Successful completion of this
   * stage indicates valid lexical and syntactic structure. The AST will be
   * well formed, but may contain ambiguities.
   */
  public static final int PARSED           = 0x01;

  /**
   * The second stage of the compiler. Here, the visible interface of all
   * classes (including inner classes) are read into a table. This includes
   * all fields and methods (including return, argument, and exception types).
   * A ClassResolver is available (after completion of this stage) which maps 
   * the name of each class found in this file to a ClassType. These ClassType 
   * objects, however, may contain ambiguous types. The result of "getResolver"
   * is a ClassResolver that is in this very state. (The AST will still contain
   * ambiguities after this stage.)
   */
  public static final int READ             = 0x02;

  /**
   * The third stage of a the compiler. In this stage, ambiguities are removed
   * from the ClassResolver for this source file. That is, for each class
   * defined in this file, the types associated with fields and methods 
   * (include return, argument and exception types) will be disambiguated,
   * and replaced with actual ClassType definitions. In addition, upon 
   * successful completion of this stage, ALL super classes of any class that 
   * is defined in this file will also be in this state. (The AST of this 
   * source file will continue to contain ambiguities.) 
   */
  public static final int CLEANED          = 0x04;

  /**
   * The fourth stage of the compiler. During this stage, ambiguities are 
   * removed from the AST. Ambiguous dotted expressions, such as "a.b.c", are
   * resolved and replaced with the appropriate nodes. Also, after completion
   * of this stage any AmbiguousTypes referenced in the AST will be replaced 
   * with concrete ClassTypes. Note, however, that these ClassTypes themselves 
   * may contain ambiguities. (The source files cooresponding to these
   * ClassTypes may only be at the READ stage.)
   */
  public static final int DISAMBIGUATED    = 0x08;

  /**
   * The fifth stage of the compiler. This stage represents the type and flow 
   * checking of a source file. Note that all dependencies of this file must be
   * in the CLEANED state before this file can be type checked. To ensure this,
   * the compiler will attempt to bring ALL source in the work list up to (and 
   * through) the CLEANED stage. If the compiler is unable to do so, then
   * it will exits with errors. All sources files which successfully complete
   * this stage are semantically valid.
   */
  public static final int CHECKED          = 0x10;

  /**
   * The sixth (and final) stage of the compiler. During this stage, the 
   * translated version of the source file is written out to the output file. 
   */
  public static final int TRANSLATED       = 0x20;

  /**
   * This is not a stage of translation, but is used to keep track of which
   * targets are currently being used. Notice that is has 
   * <code>protected</code> visibility.
   */
  protected static final int IN_USE        = 0x40;


  /* Static fields. */
  private static TypeSystem ts;

  private static CachingClassResolver systemResolver;
  private static TableClassResolver parsedResolver;
  private static LoadedClassResolver sourceResolver;

  protected static TargetFactory tf;

  /** What's done and what needs work. */  
  protected static List workList;

  /**
   * Initialize the compiler. Must be called before any instaniation of
   * objects in this class. Behavior for multiple invocations of this method
   * are undefined.
   *
   * @param options Should contain options defined be the OPT_* constants.
   * @param tf Allows the compiler to draw in new source as necessary.
   */
  public static void initialize( Map options, TypeSystem ts,
                                 TargetFactory tf)
  {
    Compiler.options = options;
    Compiler.ts = ts;
    Compiler.tf = tf;
 
    /* Read the options. */
    outputWidth = ((Integer)options.get( OPT_OUTPUT_WIDTH)).intValue();

    useFqcn = ((Boolean)options.get( OPT_FQCN)).booleanValue();

    verbose = ((Boolean)options.get( OPT_VERBOSE)).booleanValue();
    
    serialize = ((Boolean)options.get( OPT_SERIALIZE)).booleanValue();

    jif = ((Boolean)options.get( Main.MAIN_OPT_EXT_JIF)).booleanValue();

    if (jif) {
	serialize = false;
	/* setup the splitter configuration */
	jifSplitConfig = new Config("config");
    }

    /* Set up the resolvers. */
    Compiler compiler = new Compiler( null);

    CompoundClassResolver compoundResolver = new CompoundClassResolver();
        
    parsedResolver = new TableClassResolver( compiler);
    compoundResolver.addClassResolver( parsedResolver);
    
    sourceResolver = new LoadedClassResolver( tf, compiler, ts);

    compoundResolver.addClassResolver( sourceResolver);

    systemResolver = new CachingClassResolver( compoundResolver);
    try
    {
      ts.initializeTypeSystem( systemResolver, compiler);
    }
    catch( SemanticException e)
    {
      throw new InternalCompilerError( "Unable to initialize compiler. " + 
                                       "Failed to initialize type system: " +
                                       e.getMessage());
    }
    
    /* Other setup. */
    workList = Collections.synchronizedList( new LinkedList());
    
    initialized = true;
  }

  public static boolean useFullyQualifiedNames()
  {
    return useFqcn;
  }

  public static TypeSystem getTypeSystem()
  {
    return ts;
  }

  public static void verbose( Object o, String s)
  {
    if( verbose) {
      int thread = Thread.currentThread().hashCode();

      if( o instanceof Class) {
        System.err.println( ((Class)o).getName() + ":" + thread + ": " + s);
      }
      else {
        System.err.println( o.getClass().getName() + ":" + thread + ": " + s);
      }
      System.err.flush();
    }
  }
 
  /* Public constructor. */
  public Compiler()
  {
    if( !initialized) {
      throw new InternalCompilerError( "Unable to construct compiler "
                       + "instance before static initialization.");
    }
  }

  /**
   * This constructor is used by the static initializer to create new
   * instances before initialization is comlete.
   */
  private Compiler( Object dummy)
  {
  }
 
 
  /* Public Methods. */
  public boolean readFile( String filename) throws IOException
  {
    return compile( tf.createFileTarget( filename), READ);
  }
  
  public boolean compileFile( String filename) throws IOException 
  {
    return compile( tf.createFileTarget( filename), TRANSLATED);
  }

  public boolean compileClass( String classname) throws IOException
  {
    return compile( lookupJob( classname), TRANSLATED);
  }

  public ClassResolver getResolver( Target t) throws IOException
  {
    Job job = lookupJob( t);
    boolean success = compile( job, READ);

    if( success) {
      return job.cr;
    }
    else {
      return null;
    }
  }

  public void addTarget( ClassType clazz) throws IOException
  {
    /* All we need to do here is "look" for a job that contains this class.
     * If no job is found, then a new one will automatically created. */
    lookupJob( clazz);
  }

  public boolean cleanClass( ClassType clazz) throws IOException
  {
    Job job = lookupJob( clazz);
    return compile( job, CLEANED);
  } 

  public boolean compile( Target t) throws IOException
  {
    return compile( t, TRANSLATED);
  }
 
  public boolean cleanup( Collection completed) throws IOException
  {
    boolean okay = true, success;
    Job job;
    
    for( int i = 0; i < workList.size(); i++)
    {
      synchronized( workList) {
        job = (Job)workList.get( i);
      }
      success = compile( job, TRANSLATED);
      if( success) {
        completed.add( job.t);
      }
      else {
        okay = false;
      }
    }
    return okay;
  }


  /* Protected methods. */
  protected boolean compile( Target t, int goal) throws IOException
  {
    Job job = lookupJob( t);
    return compile( job, goal);
  }

  /**
   * This is the main loop of the compiler. All synchronization is done with
   * <code>lookupJob, <code>acquireJob</code>, and <code>releaseJob</code>.
   *
   * @param job The job information (including a target) to be compiled.
   * @param goal The required stage to which this job should be taken
   * @return <code>true</code> iff <code>job</code> has completed stage
   *  <code>goal</code> without errors.
   */
  protected boolean compile( Job job, int goal) throws IOException
  {
    // FIXME: if we get an io error (due to too many files open, for example)
    // it will throw an exception. but, we won't be able to do anything with it since
    // the exception handlers will want to load jltools.util.CodeWriter and 
    // jltools.util.ErrorInfo to print and enqueue the error; but the classes must 
    // be in memory since the io can't open any files; thus, we force the classloader
    // to load the class file.
    try
    {
      this.getClass().getClassLoader().loadClass( "jltools.util.CodeWriter");
      this.getClass().getClassLoader().loadClass( "jltools.util.ErrorInfo");
    }
    catch (ClassNotFoundException cnfe)
    {
      cnfe.printStackTrace();
    }
    
    if( hasErrors( job)) {
      return false;
    }

    ErrorQueue eq = job.t.getErrorQueue();

    try
    {
      /* PARSE. */
      if( (job.status & PARSED) == 0) {
        acquireJob( job);
        if( (job.status & PARSED) == 0) {
          verbose( this, "parsing " + job.t.getName() + "...");
          job.ast = parse( job.t);
          
          if( hasErrors( job)) { releaseJob( job); return false; }
          
          job.status |= PARSED;
          
          job.ast = runVisitors( job.t, job.ast, PARSED);
        }
        releaseJob( job);
      }

      if( goal == PARSED) { return true; }

      /* READ. */
      if( (job.status & READ) == 0) 
      { 
        acquireJob( job);
        if( (job.status & READ) == 0) {
          verbose( this, "reading " + job.t.getName() + "...");
          job.cr = new TableClassResolver( this);
          
          job.it = readSymbols( job.ast, job.cr, job.t);
          parsedResolver.include( job.cr);

          if( hasErrors( job)) { releaseJob( job); return false; }
          
          job.status |= READ;
          
          job.ast = runVisitors( job.t, job.ast, READ);
        }
        releaseJob( job);
      }

      if( goal == READ) { return true; }

      /* CLEAN. */
      if( (job.status & CLEANED) == 0) {
        acquireJob( job);
        if( (job.status & CLEANED) == 0) {
          verbose( this, "cleaning " + job.t.getName() + "...");
          job.cr.cleanupSignatures( ts, job.it, eq);
          
          if( hasErrors( job)) { releaseJob( job); return false; }
          
          job.status |= CLEANED;
          
          if( job.ast != null) {
            job.ast = runVisitors( job.t, job.ast, CLEANED);
          }
        }
        releaseJob( job);
      }

      if( goal == CLEANED) { return true; }

      /* DISAMBIGUATE. */
      if( (job.status & DISAMBIGUATED) == 0) {
        acquireJob( job);
        if( (job.status & DISAMBIGUATED) == 0) {
          verbose( this, "disambiguating " + job.t.getName() + "...");
          job.ast = removeAmbiguities( job.ast, job.cr, job.it, eq);
          
          verbose (this, "folding constants " + job.t.getName() + "...");
          job.ast = foldConstants ( job.ast );
          
          if( hasErrors( job)) { releaseJob( job); return false; }
        
          job.status |= DISAMBIGUATED;

          job.ast = runVisitors( job.t, job.ast, DISAMBIGUATED);
        }
        releaseJob( job);
      }

      if( goal == DISAMBIGUATED) { return true; } 

      /* Okay. Before we can type check, we need to make sure that everyone
       * else in the worklist is at least CLEAN. */
      boolean okay = true; 
      for( int i = 0; i < workList.size(); i++) {
        okay &= compile( (Job)workList.get( i), CLEANED);
      }
      if( !okay) {
        return false;
      }
     
      /* CHECK. */
      if( (job.status & CHECKED) == 0) {
        acquireJob( job);
        if( (job.status & CHECKED) == 0) {
          verbose( this, "checking " + job.t.getName() + "...");

          job.ast = typeCheck( job.ast, job.it, eq);    
          verbose( this, "exception checking " + job.t.getName() + "...");
          job.ast = exceptionCheck ( job.ast, eq);

	  if (jif) {
	      verbose( this, "label checking " + job.t.getName() + "...");
	      job.ast = labelCheck( job.ast, job.it, eq);
	  }
	      

          if( hasErrors( job)) { releaseJob( job); return false; }
        
          job.status |= CHECKED;

          job.ast = runVisitors( job.t, job.ast, CHECKED);
        }
        releaseJob( job);
      }

      if( goal == CHECKED) { return true; }

      /* TRANSLATE. */
      if( (job.status & TRANSLATED) == 0) {
        acquireJob( job);
        if( (job.status & TRANSLATED) == 0) {
          if( serialize) {
            verbose( this, "serializing class info for " 
                     + job.t.getName() + "...");
            job.ast = serializeClassInfo( job.t, job.ast, eq);
          }

          verbose( this, "translating " + job.t.getName() + "...");
          translate( job.t, job.ast, job.it);

          /* NB: The AST is no longer needed, so throw it out. */
          job.ast = null;

          job.status |= TRANSLATED;
        }
        releaseJob( job);
      }
    }
    catch( IOException e)
    {
      eq.enqueue( ErrorInfo.IO_ERROR, 
                      "Encountered an I/O error while compiling:" + e.getMessage());
      eq.flush();
      throw e;
    }
    catch( RuntimeException rte)
    {
      if( job.ast != null) {
        CodeWriter w = new CodeWriter( new UnicodeWriter( 
                                          new FileWriter( "ast.dump")), 
                                        outputWidth);
        DumpAst d = new DumpAst( w);
        job.ast.visit( d);
        w.flush();
      }
      throw rte;
    }
    catch( Error err)
    {
      if( job.ast != null) {
        CodeWriter cw = new CodeWriter( new UnicodeWriter( 
                                          new FileWriter( "ast.dump")), 
                                        outputWidth);
        DumpAst d = new DumpAst( cw);
        job.ast.visit( d);
        cw.flush();
      }
      throw err;
    }

    eq.flush();

    if( hasErrors( job)) {
      return false;
    }
    else {
      return true;
    }
  }

  protected Job lookupJob( ClassType clazz) throws IOException
  {
    Job job;

    synchronized( workList) 
    {
      for( Iterator iter = workList.iterator(); iter.hasNext(); ) {
        job = (Job)iter.next();
        try {
          job.cr.findClass( clazz.getFullName());
          return job;
        }
        catch( SemanticException e) {}
      }
    }

    // System.err.println( "Creating new job for class: " + clazz.getFullName());

    job = new Job( tf.createClassTarget( clazz));
    job.it = new ImportTable( systemResolver, true, job.t.getErrorQueue());
    job.cr = new TableClassResolver( this);
    job.cr.addClass( clazz.getFullName(), clazz);

    job.status = PARSED | READ | DISAMBIGUATED | CHECKED | TRANSLATED;

    synchronized( workList)
    {
      workList.add( job);
    }

    parsedResolver.include( job.cr);

    return job;
  }

  protected Job lookupJob( String classname) throws IOException
  {
    Job job;

    synchronized( workList) 
    {
      for( Iterator iter = workList.iterator(); iter.hasNext(); ) {
        job = (Job)iter.next();
        try {
          job.cr.findClass( classname);
          return job;
        }
        catch( SemanticException e) {}
      }
    }
    return lookupJob( tf.createClassTarget( classname)); 
  }

  protected Job lookupJob( Target t) throws IOException
  {
    /* Now check the worklist. */
    Job job = new Job( t);
    
    synchronized( workList) {
      if( workList.contains( job)) {
        /* Found it. */
        job = (Job)workList.get( workList.indexOf( job));
      }
      else {
        /* No sign of it, Add the new job. */
        workList.add( job);
      }
    }
    return job;
  }

  protected void acquireJob( Job job)
  {
    synchronized( job) {
      if( (job.status & IN_USE) > 0) {
        try
        {
          job.wait();
        }
        catch( InterruptedException e)
        {
          throw new InternalCompilerError( e.getMessage());
        }
      }
      job.status |= IN_USE;
    }
  }

  protected void releaseJob( Job job) 
  {
    synchronized( job)
    {
      job.status ^= IN_USE;
      job.notify();
    }
  }

  protected boolean hasErrors( Job job) throws IOException
  {
    ErrorQueue eq = job.t.getErrorQueue();
    if( eq.hasErrors()) {
      eq.flush();
      return true;
    }
    else {
      return false;
    }
  }

  protected Node parse( Target t) throws IOException
  {
    java_cup.runtime.lr_parser grm = t.getParser();
    java_cup.runtime.Symbol sym = null;

    ErrorQueue eq = t.getErrorQueue();

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
      e.printStackTrace();
      eq.enqueue( ErrorInfo.INTERNAL_ERROR, e.getMessage());
      return null;
    }
    // done with the input
    t.closeSource();

    /* Try and figure out whether or not the parser was successful. */
    if( sym == null) {
      eq.enqueue( ErrorInfo.SYNTAX_ERROR, "Unable to parse source file.");
      return null;
    }

    if( !(sym.value instanceof Node)) {
      eq.enqueue( ErrorInfo.SYNTAX_ERROR, "Unable to parse source file.");
      return null;
    }
    else {
      return (Node)sym.value; 
    }
  }

  protected ImportTable readSymbols( Node ast, TableClassResolver cr,
                                     Target t) throws IOException
  {
    SymbolReader sr;
    if (jif) {
	verbose(this, "Using Jif Symbol Reader");
	sr = new JifSymbolReader(  systemResolver, cr, t, tf, ts, 
                                        t.getErrorQueue());
    } else {
	sr = new SymbolReader( systemResolver, cr, t, tf, ts, 
			       t.getErrorQueue());
    }
    ast.visit( sr);

    return sr.getImportTable();
  }

  protected Node removeAmbiguities( Node ast, TableClassResolver cr,
                                    ImportTable it, ErrorQueue eq)
  {
    AmbiguityRemover ar;
    if (jif) {
	verbose(this, "Not Using Jif Ambiguity Remover");
	ar = new AmbiguityRemover( ts, it, eq);
    } else {
	ar = new AmbiguityRemover( ts, it, eq);
    }
    ast =  ast.visit( ar);
    //    verbose(this, "Adding THIS references...");
    //JifThisVisitor jtv = new JifThisVisitor();
    //ast = ast.visit(jtv);
    return ast;
  }

  protected Node foldConstants ( Node ast )
  {
    ConstantFolder cf = new ConstantFolder(ts);
    return ast.visit( cf);
  }

  protected Node typeCheck( Node ast, ImportTable it, ErrorQueue eq)
  {
    TypeChecker tc = new TypeChecker( ts, it, eq);
    return ast.visit( tc);
  }

  protected Node labelCheck( Node ast, ImportTable it, ErrorQueue eq)
  {
      JifTypeSystem jts;
      try {
	  jts = (JifTypeSystem)ts;
      } catch (ClassCastException ce) {
	  throw new InternalCompilerError("Can't labelcheck without JifTypeSystem");
      }
      JifPrincipalHeirarchy PH = new JifPrincipalHeirarchy();
      JifLabelChecker lc = new JifLabelChecker( jts, it, eq, PH);
      ast = ast.visit( lc);
      try {
	  JifVarMap soln = lc.solver.solve(PH);
	  CodeWriter cw = new CodeWriter(System.out, outputWidth);
	  soln.dump(cw);
	  cw.flush();
      } catch (SemanticException e) {
	  eq.enqueue( ErrorInfo.SEMANTIC_ERROR, e.getMessage(), -1);
      } catch (IOException e) {
      }
      return ast;
  }


  protected Node exceptionCheck( Node ast, ErrorQueue eq)
  {
    ExceptionChecker ec = new ExceptionChecker( eq );
    return ast.visit ( ec);
  }

  protected void translate( Target t, Node ast, ImportTable it) 
    throws IOException
  {
    if (jif) {
	verbose(this, "Splitting the file...");
	/*
	{CodeWriter w = new CodeWriter(new UnicodeWriter( 
                                          new FileWriter( "ast001.dump")),
								  outputWidth);
	DumpAst d = new DumpAst(w);
	ast.visit(d);
	w.flush(); }

	Splitter splitter = new Splitter(jifSplitConfig, ts);
	Node instAst = ast.visit(splitter);
	CodeWriter w = new CodeWriter(new UnicodeWriter( 
                                          new FileWriter( "instAst.dump" )),
								  outputWidth);
	DumpAst d = new DumpAst(w);
	instAst.visit(d);
	w.flush();
	verbose(this, "Splitting end");
	*/
    } else {
	SourceFileNode sfn = (SourceFileNode)ast;
	Writer ofw = t.getOutputWriter( sfn.getPackageName());
	CodeWriter w = new CodeWriter( t.getOutputWriter(sfn.getPackageName()), 
				       outputWidth);
    
	ast.translate( new LocalContext(it, ts, null), w);
	w.flush();
	System.out.flush();
	t.closeDestination();
    }
  }

  protected Node serializeClassInfo( Target t, Node ast, ErrorQueue eq)
  {
    ClassSerializer sc = new ClassSerializer( ts, t.getLastModifiedDate(), eq);
    return ast.visit( sc);
  }

  protected Node runVisitors( Target t, Node ast, int stage)
  {
    NodeVisitor v;
    Node result = ast;

    while( (v = t.getNextNodeVisitor( stage)) != null) {
      verbose( this, "running visitor " + v.getClass().getName() + "...");
      result = result.visit( v);
      v.finish();
    }

    return result;
  }
  
  
  static class Job
  {
    Target t;
    Node ast;
    ImportTable it;
    TableClassResolver cr;

    int status;

    public Job( Target t) 
    {
      this.t = t;
          
      ast = null;
      it = null;
      cr = null;

      status = 0;
    }
    
    public boolean equals( Object o) {
      if( o instanceof Job) {
        return t.equals( ((Job)o).t);
      }
      else {
        return false;
      }
    }
  }
}
