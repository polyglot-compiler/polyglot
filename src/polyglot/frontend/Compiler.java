package jltools.frontend;

import jltools.ast.*;
import jltools.parse.*;
import jltools.lex.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.Main;
import jltools.main.Options;

import java.io.*;
import java.util.*;

/**
 * This is the main entry point for the compiler. It contains a work list that
 * contains entries for all classes that must be compiled (or otherwise worked
 * on).
 */
public class Compiler implements TargetTable, ClassCleaner
{
  /** 
   * Marks major changes in the output format of the files produced by the
   * compiler. Files produced be different major versions are considered 
   * incompatible and will not be used as source of class information.
   */
  public static int VERSION_MAJOR           = 0;
  
  /** 
   * Indicates a change in the compiler that does not affect the output format.
   * Source files will be prefered over class files build by compilers with
   * different minor versions, but if no source file is available, then the
   * class file will be used.
   */
  public static int VERSION_MINOR           = 3;

  /**
   * Denote minor changes and bugfixes to the compiler. Class files compiled
   * with versions of the compiler that only differ in patchlevel (from the
   * current instantiation) will always be preferred over source files (unless
   * the source files have newer modification dates).
   */
  public static int VERSION_PATCHLEVEL      = 0;

  /* Global options and state. */

  /** Whether any errors have been seen yet in this compilation request. */
  private boolean hasErrors = false;
  private Options options;

  private CachingClassResolver systemResolver;
  private TableClassResolver parsedResolver;
  private LoadedClassResolver sourceResolver;

  protected TypeSystem type_system;
  protected ExtensionFactory extension_factory;
  protected TargetFactory target_factory;

  /** What's done and what needs work. */  
  protected List workList;
  protected Map workListMap;

  private Collection outputFiles = new HashSet();

  /**
   * Initialize the compiler. Must be called before any instantiation of
   * objects in this class. Behavior for multiple invocations of this method
   * are undefined.
   *
   * @param options Contains jltools options
   * @param tf Allows the compiler to pull in new source files as necessary.
   */
  public Compiler(Options options_, 
		  TargetFactory tf_) {
    options = options_;
    type_system = options.extension.getTypeSystem();
    extension_factory = options.extension.getExtensionFactory();
    target_factory = tf_;
 
    // Create the compiler and set up the resolvers.
    CompoundClassResolver compoundResolver = new CompoundClassResolver();
        
    parsedResolver = new TableClassResolver(this);
    compoundResolver.addClassResolver(parsedResolver);
    
    sourceResolver = new LoadedClassResolver(target_factory, this, type_system);
    compoundResolver.addClassResolver(sourceResolver);

    systemResolver = new CachingClassResolver(compoundResolver);

    /* Other setup. */
    workList = Collections.synchronizedList( new LinkedList());
    workListMap = Collections.synchronizedMap( new HashMap());
    
    try {
      type_system.initializeTypeSystem(systemResolver, this);
    }
    catch (SemanticException e) {
      throw new InternalCompilerError( "Unable to initialize compiler. " + 
                                       "Failed to initialize type system: " +
                                       e.getMessage());
    }
  }

  /** Return a set of output filenames resulting from a successful compilation.
    */
  public Collection outputFiles() {
    return outputFiles;
  }

  /** Compile all the files listed in the set of strings <code>source</code>.
      Return true on success. The method <code>outputFiles</code> can be
      used to obtain the output of the compilation. */
  public boolean compile(Collection source) {
    String targetName = null;
    try {
      Main.report(null, 1, "Read all files from the command-line.");
      
      Iterator iter = source.iterator();
      while (iter.hasNext()) {
	targetName = (String)iter.next();
	if (!readFile(targetName))
	    hasErrors = true;
      }
      
      Main.report(null, 1, "Done reading, now translating...");
      
      iter = source.iterator();
      while(iter.hasNext()) {
	targetName = (String)iter.next();
	if (!compileFile(targetName))
	  hasErrors = true;
      }
      
    }
    catch (FileNotFoundException fnfe)
    {
      System.err.println(options.extension.compilerName() +
			 ": cannot find source file -- " + targetName);
      return false;
    }
    catch(IOException e)
    {
      System.err.println(options.extension.compilerName() +
			 ": caught IOException while compiling -- " +
			 e.getMessage());
      return false;
    }
    catch(ErrorLimitError ele)
    {
      return false;
    }
    
    /* Make sure we do this before we exit. */
    Collection completed = new LinkedList();
    try {
      if (!cleanup(completed)) {
	return false;
      }
    }
    catch(IOException e)
    {
      System.err.println("Caught IOException while compiling: "
			  + e.getMessage());
      System.exit(1);
    }
    return true;
  }
    
  public boolean useFullyQualifiedNames()
  {
    return options.fully_qualified_names;
  }

  public TargetFactory getTargetFactory()
  {
    return target_factory;
  }

  public ExtensionFactory getExtensionFactory()
  {
    return extension_factory;
  }

  public TypeSystem getTypeSystem()
  {
    return type_system;
  }

  public ExtensionInfo getExtensionInfo()
  {
    return options.extension;
  }

  public ClassResolver getSystemResolver()
  {
    return systemResolver;
  }

  public TableClassResolver getParsedResolver()
  {
    return parsedResolver;
  }

  public int getOutputWidth()
  {
    return options.output_width;
  }

  public boolean serializeClassInfo()
  {
    return options.serialize_type_info;
  }

/*
  public static Compiler getCompiler()
  {
    System.err.println("Warning: getting static compiler variable");
    throw new NullPointerException();
    //return the_compiler;
  }
*/

  /* Public constructor. */

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
    return compile(target_factory.createFileTarget( filename), Job.READ);
  }
  
  public boolean compileFile( String filename) throws IOException 
  {
    return compile(target_factory.createFileTarget( filename), Job.TRANSLATED);
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

  protected boolean bringAllToStage(int stage) throws IOException {
      boolean okay = true; 
      for (int i = 0; i < workList.size(); i++) {
	  okay &= compile( (Job) workList.get(i), stage);
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
          Main.report(null, 2, "Parsing " + job.getTarget().getName() + "...");
	  job.parse();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.PARSED) { return true; }

      if( options.extension.compileAllToStage(Job.PARSED) ) {
	  boolean okay = bringAllToStage(Job.PARSED);
	  if (! okay) return false;
      }

      /* READ. */
      if (! job.isRead()) {
        acquireJob( job);
	if (! job.isRead()) {
          Main.report(null, 2, "Reading " + job.getTarget().getName() + "...");
	  job.read();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.READ) { return true; }

      if( options.extension.compileAllToStage(Job.READ) ) {
	  boolean okay = bringAllToStage(Job.READ);
	  if (! okay) return false;
      }


      /* CLEAN. */
      if (! job.isCleaned()) {
        acquireJob( job);
	if (! job.isCleaned()) {
          Main.report(null, 2, "Cleaning " + job.getTarget().getName() + "...");
	  job.clean();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.CLEANED) { return true; }

      if( options.extension.compileAllToStage(Job.CLEANED) ) {
	  boolean okay = bringAllToStage(Job.CLEANED);
	  if (! okay) return false;
      }

      // Okay. Before we can type check, we need to make sure that everyone
      // else in the worklist is at least CLEANED.  Note that the worklist
      // can grow while compiling a member of the worklist, so we cannot use
      // an iterator.

      /* DISAMBIGUATE. */
      if (! job.isDisambiguated()) {
        acquireJob( job);
	if (! job.isDisambiguated()) {
          Main.report( null, 2,
	    "Disambiguating " + job.getTarget().getName() + "...");
	  job.disambiguate();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.DISAMBIGUATED) { return true; } 

      if( options.extension.compileAllToStage(Job.DISAMBIGUATED) ) {
	  boolean okay = bringAllToStage(Job.DISAMBIGUATED);
	  if (! okay) return false;
      }

      /* CHECK. */
      if (! job.isChecked()) {
        acquireJob( job);
	if (! job.isChecked()) {
          Main.report(null, 2, "Checking " + job.getTarget().getName() + "...");
	  job.check();
        }
        releaseJob( job);
	if( hasErrors( job)) {
	  return false;
	}
      }

      if( goal == Job.CHECKED) { return true; }

      if( options.extension.compileAllToStage(Job.CHECKED) ) {
	  boolean okay = bringAllToStage(Job.CHECKED);
	  if (! okay) return false;
      }

      /* TRANSLATE. */
      if (! job.isTranslated()) {
        acquireJob( job);
	if (! job.isTranslated()) {
          Main.report(null, 2, "Translating " + job.getTarget().getName() + "...");
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
				     options.output_width);
      job.dump(cw);
      throw rte;
    }
    catch( Error err)
    {
      CodeWriter cw = new CodeWriter(new UnicodeWriter( 
				     new FileWriter( "ast.dump")), 
				     options.output_width);
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

    Target t = target_factory.createClassTarget( clazz);
    Job job = new ClassTypeJob(this, t, clazz, t.getErrorQueue(),
				systemResolver);

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

    return lookupJob(target_factory.createClassTarget( classname)); 
  }

  protected Job lookupJob( Target t) throws IOException
  {
    /* Now check the worklist. */
    ErrorQueue eq = t.getErrorQueue();

    Job job;
    
    synchronized( workList) {
      job = (Job) workListMap.get(t);

      if (job == null) {
	job = new SourceJob( t, eq, this);
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
