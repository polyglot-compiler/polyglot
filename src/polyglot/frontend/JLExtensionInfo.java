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
 * Compilation passes and visitors:
 * <ol>
 * <li> parse </li>
 * <li> build types (TypeBuilder) </li>
 * <li> disambiguate types (TypeAmbiguityRemover) </li>
 * <li> disambiguate (AmbiguityRemover) </li>
 * <li> constant folding (ConstantFolder)
 * <hr>
 * <center>BARRIER</center>
 * <hr>
 * <li> type checking (TypeChecker) </li>
 * <li> exception checking (ExceptionChecker)
 * <hr>
 * <center>BARRIER</center>
 * <hr>
 * <li> serialization (ClassSerializer), optional </li>
 * <li> translation (Translator) </li>
 * </ol>
 */
public class ExtensionInfo implements jltools.frontend.ExtensionInfo {
    protected Compiler compiler;
    protected Options options;
    protected TypeSystem ts = null;
    protected NodeFactory nf = null;

    public void setOptions(Options options) throws UsageError {
	this.options = options;
    }

    public void initCompiler(Compiler compiler) {
	this.compiler = compiler;

	try {
	    ts.initialize(compiler);
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

    /** Create the type system for this extension. */
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

    /** Create the node factory for this extension. */
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

    /** An implementation of the Job class.   We make it an inner class to avoid creating a ext/jl/frontend directory. */
    protected static class JLJob extends Job {
	protected JLJob(Source s, Compiler c) {
	    super(s, c);
	}

	/** The parse pass. */
	protected Pass parse;

	/** The parse pass. */
	public Pass parsePass() {
	    if (parse == null) {
		parse = new ParserPass(this, compiler().extensionInfo());
	    }

	    return parse;
	}

	/** The build pass. */
	protected Pass build;

	/** The build pass. */
	public Pass buildPass() {
	    if (build == null) {
		build = new VisitorPass(this, new TypeBuilder(this));
		build.runAfter(parsePass());
	    }

	    return build;
	}

	/** The disambiguate types pass. */
	protected Pass disambTypes;

	/** The disambiguate types pass. */
	public Pass disambTypesPass() {
	    if (disambTypes == null) {
		disambTypes = new VisitorPass(this, new TypeAmbiguityRemover(this));
		disambTypes.runAfter(buildPass());
	    }

	    return disambTypes;
	}

	/** The disambiguate pass. */
	protected Pass disamb;

	/** The disambiguate pass. */
	public Pass disambPass() {
	    if (disamb == null) {
		disamb = new VisitorPass(this, new AmbiguityRemover(this));
		disamb.runAfter(disambTypesPass());
	    }

	    return disamb;
	}

	/** The constant fold pass. */
	protected Pass fold;

	/** The constant fold pass. */
	public Pass foldPass() {
	    if (fold == null) {
		fold = new VisitorPass(this, new ConstantFolder(this));
		fold.runAfter(disambPass());
	    }

	    return fold;
	}

	/** A barrier pass that runs before type checking. */
	protected Pass beforeCheck;

	/** A barrier pass that runs before type checking. */
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

	/** The type check pass. */
	protected Pass typeCheck;

	/** The type check pass. */
	public Pass checkPass() {
	    if (typeCheck == null) {
		typeCheck = new VisitorPass(this, new TypeChecker(this));
		typeCheck.runAfter(beforeCheckPass());
	    }

	    return typeCheck;
	}

	/** The exception check pass. */
	protected Pass excCheck;

	/** The exception check pass. */
	public Pass excCheckPass() {
	    if (excCheck == null) {
		excCheck = new VisitorPass(this, new ExceptionChecker(
			    compiler().typeSystem(), compiler().errorQueue()));
		excCheck.runAfter(checkPass());
	    }

	    return excCheck;
	}

	/** A barrier pass that runs before translation. */
	protected Pass beforeTranslate;

	/** A barrier pass that runs before translation. */
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

    /** The AST dump pass. */
    protected Pass dump;

    /** The AST dump pass. */
    public Pass dumpPass() {
		if (dump == null) {
 			if (compiler().dumpAst()) {
 				dump = new VisitorPass(this,
	                       new DumpAst(new CodeWriter(System.err, 78)));
				dump.runAfter(beforeTranslatePass());
	        } else {
                dump = beforeTranslatePass();
            }
        }

        return dump;
    }

	/** The class serialization pass. */
	protected Pass serialize;

	/** The class serialization pass. */
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
		    serialize.runAfter(dumpPass());
		}
		else {
		    serialize = dumpPass();
		}
	    }

	    return serialize;
	}

	/** The translation pass. */
	protected Pass translate;

	/** The translation pass. */
	public Pass translatePass() {
	    if (translate == null) {
		translate = new Translator(this);
		translate.runAfter(serializePass());
	    }

	    return translate;
	}
    }

    static { Report.topics.add("jl"); }
    static { Report.topics.add("verbose"); }
    static { Report.topics.add("types"); }
    static { Report.topics.add("frontend"); }
}
