package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.main.*;

import java.io.*;
import java.util.*;

/** <code>StandardExtensionInfo</code> is default the
 * <code>ExtensionInfo</code> for the Java language. */
public class StandardExtensionInfo implements ExtensionInfo {
    protected jltools.frontend.Compiler compiler;
    protected Options options;
    protected TypeSystem ts = null;
    protected ExtensionFactory ef = null;
    // protected NodeFactory nf = null;

    public void setOptions(Options options) throws UsageError {
	this.options = options;
    }

    public void initCompiler(jltools.frontend.Compiler compiler) {
	this.compiler = compiler;

	try {
	    ts.initializeTypeSystem(compiler.systemResolver());
	}
	catch (SemanticException e) {
	    throw new InternalCompilerError(
		"Unable to initialize type system: " + e.getMessage());
	}
    }

    public String fileExtension() {
	return "jl";
    }

    public String compilerName() {
	return "jlc";
    }

    public String options() {
	return "";
    }

    /** By default, don't parse anything */
    public int parseCommandLine(String args[], int index, Options options)
	throws UsageError {
	return index;
    }

    protected TypeSystem createTypeSystem() {
	return new StandardTypeSystem();
    }

    public TypeSystem typeSystem() {
	if (ts == null) {
	    ts = createTypeSystem();
	}
	return ts;
    }

    protected ExtensionFactory createExtensionFactory() {
	return new StandardExtensionFactory();
    }

    public ExtensionFactory extensionFactory() {
	if (ef == null) {
	    ef = createExtensionFactory();
	}
	return ef;
    }

    public SourceLoader sourceLoader() {
	String sx = options.source_ext;

	if (sx == null) {
	    sx = fileExtension();
	}

	return new SourceLoader(options.source_path, sx);
    }

    public Job createJob(Source s) {
      return new Job(s, compiler, this);
    }

    public TargetFactory targetFactory() {
	String sx = options.source_ext;

	if (sx == null) {
	    sx = fileExtension();
	}

	return new TargetFactory(options.output_directory,
				 options.output_ext,
				 sx,
				 options.output_stdout);
    }

  /*
    protected NodeFactory createNodeFactory() {
	return new StandardNodeFactory();
    }

    public NodeFactory nodeFactory() {
	if (nf == null) {
	    nf = createNodeFactory();
	}
	return nf;
    }
  */

    public Parser parser(Reader reader, Job job) {
	jltools.parse.Lexer lexer;
	java_cup.runtime.lr_parser grm;

	lexer = new jltools.parse.Lexer(reader, job.source().name(),
				      job.compiler().errorQueue());
	grm = new jltools.parse.Grm(lexer, ts, job.compiler().errorQueue());

	return new CupParser(grm, job);
    }

    public Scheduler scheduler() {
	Scheduler scheduler = new Scheduler(this);

	List l = new LinkedList();

	l.add(Scheduler.PARSE);
	l.add(Scheduler.READ);
	l.add(Scheduler.CLEAN);
	l.add(Scheduler.DISAMBIGUATE);
	l.add(Scheduler.CHECK);
	l.add(Scheduler.TRANSLATE);

	scheduler.addOrderedPasses(l);

	// Ensure all sources are read before any are cleaned.
	scheduler.barrier(Scheduler.READ, Scheduler.CLEAN);

	// Ensure all sources are cleaned before any are disambiguated.  All we
	// really need is to ensure that supertypes of the types in the source
	// files are clean.
	scheduler.barrier(Scheduler.CLEAN, Scheduler.DISAMBIGUATE);

	// Again, make sure all sources are cleaned.  We need to do this again
	// here since more classes may have been loaded while disambiguating.
	scheduler.barrier(Scheduler.CLEAN, Scheduler.CHECK);

	// Finally, make sure all classes finish checking before any are
	// written out.
	scheduler.barrier(Scheduler.CHECK, Scheduler.TRANSLATE);

	return scheduler;
    }

    public Pass getPass(Job job, PassID key) {
	Source source = job.source();
	jltools.frontend.Compiler compiler = job.compiler();

	ErrorQueue eq = compiler.errorQueue();
	TargetFactory tf = compiler.targetFactory();
	TableClassResolver cr = compiler.parsedResolver();
	ImportTable it = job.importTable();
	int outputWidth = compiler.outputWidth();
	Collection outputFiles = compiler.outputFiles();
	LinkedList worklist = compiler.cleanWorkList();

	if (key == Scheduler.PARSE) {
	    return new ParserPass(job, this);
	}
	else if (key == Scheduler.READ) {
	    return new VisitorPass(job, new SymbolReader(it, cr, ts, eq));
	}
	else if (key == Scheduler.CLEAN) {
	    VisitorPass v = new VisitorPass(job);
	    v.visitor(new SignatureCleaner(v, ef, ts, it, eq));
	    return v;
	}
	else if (key == Scheduler.DISAMBIGUATE) {
	    List l = new LinkedList();

	    VisitorPass v = new VisitorPass(job);
	    v.visitor(new AmbiguityRemover(v, ef, ts, it, eq));
	    l.add(v);

	    l.add(new VisitorPass(job, new ConstantFolder(ef)));

	    return new CompoundPass(l);
	}
	else if (key == Scheduler.CHECK) {
	    List l = new LinkedList();

	    VisitorPass v = new VisitorPass(job);
	    v.visitor(new TypeChecker(v, ef, ts, it, eq));
	    l.add(v);

	    l.add(new VisitorPass(job, new ExceptionChecker(ts, eq)));

	    return new CompoundPass(l);
	}
	else if (key == Scheduler.TRANSLATE) {
	    List l = new LinkedList();

	    if (compiler.serializeClassInfo()) {
		l.add(new VisitorPass(job,
			  new ClassSerializer(ts, source.lastModified(), eq)));
	    }

	    l.add(new Translator(job));

	    return new CompoundPass(l);
	}

	throw new InternalCompilerError("No pass with ID " + key);
    }

    static { Report.topics.add("verbose"); }
    static { Report.topics.add("types"); }
    static { Report.topics.add("frontend"); }
}
