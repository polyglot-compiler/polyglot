package jltools.frontend;

import jltools.ast.*;
import jltools.parse.*;
import jltools.lex.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;

import java.io.*;
import java.util.*;

/**
 * This is the main entry point for the compiler. It contains a work list that
 * contains entries for all classes that must be compiled (or otherwise worked
 * on).
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

  private static boolean initialized = false;

  /* Static fields. */
  private static TypeSystem ts;
  private static ExtensionInfo extInfo;
  private static Compiler compiler;

  private static CachingClassResolver systemResolver;
  private static TableClassResolver parsedResolver;
  private static LoadedClassResolver sourceResolver;

  protected static TargetFactory tf;

  /** What's done and what needs work. */  
  protected static List workList;
  protected static Map workListMap;

  /**
   * Initialize the compiler. Must be called before any instaniation of
   * objects in this class. Behavior for multiple invocations of this method
   * are undefined.
   *
   * @param options Should contain options defined be the OPT_* constants.
   * @param tf Allows the compiler to draw in new source as necessary.
   * @param extInfo Specifies which parser and visitors to use.
   */
  public static void initialize( Map options, 
                                 TargetFactory tf, ExtensionInfo extInfo )
  {
    Compiler.options = options;
    Compiler.ts = extInfo.getTypeSystem();
    Compiler.tf = tf;
    Compiler.extInfo = extInfo;
 
    // Read the options.
    outputWidth = ((Integer)options.get( OPT_OUTPUT_WIDTH)).intValue();
    useFqcn = ((Boolean)options.get( OPT_FQCN)).booleanValue();
    verbose = ((Boolean)options.get( OPT_VERBOSE)).booleanValue();
    serialize = ((Boolean)options.get( OPT_SERIALIZE)).booleanValue();

    // Create the compiler and set up the resolvers.
    compiler = new Compiler( null);

    CompoundClassResolver compoundResolver = new CompoundClassResolver();
        
    parsedResolver = new TableClassResolver( compiler);
    compoundResolver.addClassResolver( parsedResolver);
    
    sourceResolver = new LoadedClassResolver( tf, compiler, ts);
    compoundResolver.addClassResolver( sourceResolver);

    systemResolver = new CachingClassResolver( compoundResolver);

    /* Other setup. */
    workList = Collections.synchronizedList( new LinkedList());
    workListMap = Collections.synchronizedMap( new HashMap());
    
    try {
      ts.initializeTypeSystem( systemResolver, compiler);
    }
    catch (SemanticException e) {
      throw new InternalCompilerError( "Unable to initialize compiler. " + 
                                       "Failed to initialize type system: " +
                                       e.getMessage());
    }
    
    initialized = true;
  }

  public static boolean useFullyQualifiedNames()
  {
    return useFqcn;
  }

  public static TargetFactory getTargetFactory()
  {
    return tf;
  }

  public static TypeSystem getTypeSystem()
  {
    return ts;
  }

  public static ExtensionInfo getExtensionInfo()
  {
    return extInfo;
  }

  public static int getOutputWidth()
  {
    return outputWidth;
  }

  public static boolean serializeClassInfo()
  {
    return serialize;
  }

  public static Compiler getCompiler()
  {
    return compiler;
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
    return compile( tf.createFileTarget( filename), Job.READ);
  }
  
  public boolean compileFile( String filename) throws IOException 
  {
    return compile( tf.createFileTarget( filename), Job.TRANSLATED);
  }

  public boolean compileClass( String classname) throws IOException
  {
    return compile( lookupJob( classname), Job.TRANSLATED);
  }

  public ClassResolver getResolver( Target t) throws IOException
  {
    Job job = lookupJob( t);
    boolean success = compile( job, Job.READ);

    if( success) {
      return job.getClassResolver();
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
    return compile( job, Job.CLEANED);
  } 

  public boolean compile( Target t) throws IOException
  {
    return compile( t, Job.TRANSLATED);
  }
 
  public boolean cleanup( Collection completed) throws IOException
  {
    boolean okay = true;
    boolean success;
    Job job;
    
    //cannot use an iterator here, compiling may introduce additional
    //jobs, leading to ConcurrentModificationExceptions
    for( int i = 0; i < workList.size(); i++) {
      job = (Job)workList.get(i);
      success = compile( job, Job.TRANSLATED);
      if( success) {
        completed.add( job.getTarget() );
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
    // it will throw an exception. but, we won't be able to do anything with it
    // since the exception handlers will want to load jltools.util.CodeWriter
    // and jltools.util.ErrorInfo to print and enqueue the error; but the
    // classes must be in memory since the io can't open any files; thus, we
    // force the classloader to load the class file.
    try {
      this.getClass().getClassLoader().loadClass( "jltools.util.CodeWriter");
      this.getClass().getClassLoader().loadClass( "jltools.util.ErrorInfo");
    }
    catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
    
    if( hasErrors( job)) {
      return false;
    }

    ErrorQueue eq = job.getTarget().getErrorQueue();

    try
    {
      /* PARSE. */
      if (! job.isParsed()) {
        acquireJob( job);
	if (! job.isParsed()) {
          verbose( this, "parsing " + job.getTarget().getName() + "...");
	  job.parse();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.PARSED) { return true; }

      /* READ. */
      if (! job.isRead()) {
        acquireJob( job);
	if (! job.isRead()) {
          verbose( this, "reading " + job.getTarget().getName() + "...");
	  job.read();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.READ) { return true; }

      /* CLEAN. */
      if (! job.isCleaned()) {
        acquireJob( job);
	if (! job.isCleaned()) {
          verbose( this, "cleaning " + job.getTarget().getName() + "...");
	  job.clean();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.CLEANED) { return true; }

      /* DISAMBIGUATE. */
      if (! job.isDisambiguated()) {
        acquireJob( job);
	if (! job.isDisambiguated()) {
          verbose( this, "disambiguating " + job.getTarget().getName() + "...");
	  job.disambiguate();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.DISAMBIGUATED) { return true; } 

      // Okay. Before we can type check, we need to make sure that everyone
      // else in the worklist is at least CLEANED.  Note that the worklist
      // can grow while compiling a member of the worklist, so we cannot use
      // an iterator.
      boolean okay = true; 
      for (int i = 0; i < workList.size(); i++) {
        okay &= compile( (Job) workList.get(i), Job.CLEANED);
      }
      if( !okay) {
        return false;
      }
     
      /* CHECK. */
      if (! job.isChecked()) {
        acquireJob( job);
	if (! job.isChecked()) {
          verbose( this, "checking " + job.getTarget().getName() + "...");
	  job.check();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.CHECKED) { return true; }

      /* TRANSLATE. */
      if (! job.isTranslated()) {
        acquireJob( job);
	if (! job.isTranslated()) {
          verbose( this, "translating " + job.getTarget().getName() + "...");
          job.translate();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
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
      CodeWriter cw = new CodeWriter(new UnicodeWriter( 
				     new FileWriter( "ast.dump")), 
				     outputWidth);
      job.dump(cw);
      throw rte;
    }
    catch( Error err)
    {
      CodeWriter cw = new CodeWriter(new UnicodeWriter( 
				     new FileWriter( "ast.dump")), 
				     outputWidth);
      job.dump(cw);
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
    synchronized( workList) 
    {
      for( Iterator iter = workList.iterator(); iter.hasNext(); ) {
        Job job = (Job)iter.next();
        try {
          job.getClassResolver().findClass( clazz.getFullName());
          return job;
        }
        catch( SemanticException e) {}
      }
    }

    Target t = tf.createClassTarget( clazz);
    Job job = new ClassTypeJob(t, clazz, t.getErrorQueue(), systemResolver);

    synchronized (workList)
    {
      workList.add(job);
      workListMap.put(t, job);
    }

    parsedResolver.include(job.getClassResolver());

    return job;
  }

  protected Job lookupJob(String classname) throws IOException
  {
    synchronized (workList) 
    {
      for (Iterator iter = workList.iterator(); iter.hasNext(); ) {
        Job job = (Job)iter.next();
        try {
          job.getClassResolver().findClass(classname);
          return job;
        }
        catch( SemanticException e) {}
      }
    }

    return lookupJob(tf.createClassTarget( classname)); 
  }

  protected Job lookupJob( Target t) throws IOException
  {
    /* Now check the worklist. */
    ErrorQueue eq = t.getErrorQueue();

    Job job;
    
    synchronized( workList) {
      job = (Job) workListMap.get(t);

      if (job == null) {
	job = new SourceJob( t, eq, systemResolver, parsedResolver);
        workList.add( job);
        workListMap.put(t, job);
      }
    }
    return job;
  }

  protected void acquireJob( Job job)
  {
    synchronized( job) {
      if (job.isInUse()) {
        try
        {
          job.wait();
        }
        catch( InterruptedException e)
        {
          throw new InternalCompilerError( e.getMessage());
        }
      }
      job.setInUse();
    }
  }

  protected void releaseJob( Job job) 
  {
    synchronized( job)
    {
      job.clearInUse();
      job.notify();
    }
  }

  protected boolean hasErrors( Job job) throws IOException
  {
    ErrorQueue eq = job.getTarget().getErrorQueue();
    if( eq.hasErrors()) {
      eq.flush();
      try {
	CodeWriter cw = new CodeWriter(new UnicodeWriter(new FileWriter("onError.dump")), 76);
	job.dump(cw);
      } catch (IOException exn) {}
      return true;
    }
    else {
      return false;
    }
  }
}
