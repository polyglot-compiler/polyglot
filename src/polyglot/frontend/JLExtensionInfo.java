package jltools.ext.jl;

import jltools.ast.*;
import jltools.types.*;
import jltools.ext.jl.ast.*;
import jltools.ext.jl.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;
import jltools.main.UsageError;
import jltools.main.Options;
import jltools.main.Report;
import jltools.frontend.Compiler;

import java.io.*;
import java.util.*;

/** 
 * This is the default <code>ExtensionInfo</code> for the Java language. 
 * 
 * Compiling Passes:
 * 1. parse
 * 2. build types (TypeBuilder)
 * 3. disambiguate types (TypeAmbiguityRemover)
 * 4. disambiguate (AmbiguityRemover)
 * 5. constant folding (ConstantFolder)
 * ------------------barrier
 * 6. type checking (TypeChecker)
 * 7. exception checking (ExceptionChecker)
 * ------------------barrier
 * 8. serialization (ClassSerializer), optional
 * 9. translation (Translator)
 */
public class ExtensionInfo implements jltools.frontend.ExtensionInfo {
    protected jltools.frontend.Compiler compiler;
    protected Options options;
    protected TypeSystem ts = null;
    protected NodeFactory nf = null;

    public void setOptions(Options options) throws UsageError {
	this.options = options;
    }

    public void initCompiler(Compiler compiler) {
	this.compiler = compiler;

	try {
	    ts.initialize(compiler.systemResolver());
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
    public jltools.main.Version version() {
	return new Version();
    }

    /** By default, don't parse anything */
    public int parseCommandLine(String args[], int index, Options options)
	throws UsageError {
	return index;
    }

    protected TypeSystem createTypeSystem() {
	return new TypeSystem_c();
    }

    public TypeSystem typeSystem() {
	if (ts == null) {
	    ts = createTypeSystem();
	}
	return ts;
    }

    public SourceLoader sourceLoader() {
	String sx = options.source_ext;

	if (sx == null) {
	    sx = fileExtension();
	}

	return new SourceLoader(options.source_path, sx);
    }

    public Job createJob(Source s) {
      return new JLJob(s, compiler);
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

    protected NodeFactory createNodeFactory() {
	return new NodeFactory_c();
    }

    public NodeFactory nodeFactory() {
	if (nf == null) {
	    nf = createNodeFactory();
	}
	return nf;
    }

    public Parser parser(Reader reader, Job job) {
	jltools.ext.jl.parse.Lexer lexer;
	java_cup.runtime.lr_parser grm;

	ErrorQueue eq = job.compiler().errorQueue();

	lexer = new jltools.ext.jl.parse.Lexer(reader, job.source().name(), eq);
	grm = new jltools.ext.jl.parse.Grm(lexer, ts, nf, eq);

	return new CupParser(grm, job);
    }

    protected static class JLJob extends Job {
	protected JLJob(Source s, Compiler c) {
	    super(s, c);
	}

	protected Pass parse;

	public Pass parsePass() {
	    if (parse == null) {
		parse = new ParserPass(this, compiler().extensionInfo());
	    }

	    return parse;
	}

	protected Pass build;

	public Pass buildPass() {
	    if (build == null) {
		build = new VisitorPass(this, new TypeBuilder(this));
		build.runAfter(parsePass());
	    }

	    return build;
	}

	protected Pass dt;

	public Pass disambTypesPass() {
	    if (dt == null) {
		dt = new VisitorPass(this, new TypeAmbiguityRemover(this));
		dt.runAfter(buildPass());
	    }

	    return dt;
	}

	protected Pass de;

	public Pass disambPass() {
	    if (de == null) {
		de = new VisitorPass(this, new AmbiguityRemover(this));
		de.runAfter(disambTypesPass());
	    }

	    return de;
	}

	protected Pass cf;

	public Pass foldPass() {
	    if (cf == null) {
		cf = new VisitorPass(this, new ConstantFolder(this));
		cf.runAfter(disambPass());
	    }

	    return cf;
	}

	protected Pass beforeCheck;

	public Pass beforeCheckPass() {
	    if (beforeCheck == null) {
		beforeCheck = new BarrierPass(compiler()) {
		    public Pass pass(Job job) {
			return ((JLJob) job).foldPass();
		    }
		};
	    }

	    return beforeCheck;
	}

	protected Pass tc;

	public Pass checkPass() {
	    if (tc == null) {
		tc = new VisitorPass(this, new TypeChecker(this));
		tc.runAfter(beforeCheckPass());
	    }

	    return tc;
	}

	protected Pass ec;

	public Pass excCheckPass() {
	    if (ec == null) {
		ec = new VisitorPass(this, new ExceptionChecker(
			    compiler().typeSystem(), compiler().errorQueue()));
		ec.runAfter(checkPass());
	    }

	    return ec;
	}

	protected Pass beforeTranslate;

	public Pass beforeTranslatePass() {
	    if (beforeTranslate == null) {
		beforeTranslate = new BarrierPass(compiler()) {
		    public Pass pass(Job job) {
			return ((JLJob) job).excCheckPass();
		    }
		};
	    }

	    return beforeTranslate;
	}

	protected Pass serialize;

	public Pass serializePass() {
	    if (serialize == null) {
		Source source = source();
		Compiler compiler = compiler();

		TypeSystem ts = compiler.typeSystem();
		NodeFactory nf = compiler.nodeFactory();
		ErrorQueue eq = compiler.errorQueue();

		if (compiler.serializeClassInfo()) {
		    serialize = new VisitorPass(this,
			new ClassSerializer(ts, nf, source.lastModified(), eq));
		    serialize.runAfter(beforeTranslatePass());
		}
		else {
		    serialize = beforeTranslatePass();
		}
	    }

	    return serialize;
	}

	protected Pass tr;

	public Pass translatePass() {
	    if (tr == null) {
		tr = new Translator(this);
		tr.runAfter(serializePass());
	    }

	    return tr;
	}
    }

    static { Report.topics.add("jl"); }
    static { Report.topics.add("verbose"); }
    static { Report.topics.add("types"); }
    static { Report.topics.add("frontend"); }
}
