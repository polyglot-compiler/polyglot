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
 * <li> build-types (TypeBuilder) </li>
 * <hr>
 * <center>BARRIER</center>
 * <hr>
 * <li> clean-super (AmbiguityRemover) </li>
 * <hr>
 * <center>BARRIER</center>
 * <hr>
 * <li> clean-sigs (AmbiguityRemover) </li>
 * <li> add-members (AddMemberVisitor) </li>
 * <hr>
 * <center>BARRIER</center>
 * <hr>
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
 * <li> dump (DumpAst), optional </li>
 * <li> serialization (ClassSerializer), optional </li>
 * <li> translation (Translator) </li>
 * </ol>
 */
public class ExtensionInfo implements jltools.frontend.ExtensionInfo {
    protected Compiler compiler;
    protected Options options;
    protected TypeSystem ts = null;
    protected NodeFactory nf = null;
    protected SourceLoader source_loader = null;
    protected TargetFactory target_factory = null;

    public void setOptions(Options options) throws UsageError {
	this.options = options;
    }

    public void initCompiler(Compiler compiler) {
	this.compiler = compiler;

	try {
            // Create the type system and node factory.
            typeSystem();
            nodeFactory();

            LoadedClassResolver lr = new SourceClassResolver(compiler, this,
                                                             options.classpath);
            ts.initialize(lr);
	}
	catch (SemanticException e) {
	    throw new InternalCompilerError(
		"Unable to initialize type system: " + e.getMessage());
	}
    }

    public String fileExtension() {
	String sx = options == null ? null : options.source_ext;

	if (sx == null) {
	    sx = defaultFileExtension();
        }

        return sx;
    }

    public String defaultFileExtension() {
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
        if (source_loader == null) {
            source_loader = new SourceLoader(this, options.source_path);
        }

        return source_loader;
    }

    public JobExt jobExt() {
      return null;
    }

    public SourceJob createJob(Job parent, Source source) {
	return new SourceJob(compiler, jobExt(), parent, source);
    }

    public Job createJob(Node ast, Context context, Job outer, Pass.ID begin, Pass.ID end) {
	return new InnerJob(compiler, jobExt(), ast, context, outer, begin, end);
    }

    public TargetFactory targetFactory() {
        if (target_factory == null) {
            target_factory = new TargetFactory(options.output_directory,
                                               options.output_ext,
                                               options.output_stdout);
        }
        
        return target_factory;
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

    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
	jltools.ext.jl.parse.Lexer lexer;
	java_cup.runtime.lr_parser grm;

	lexer = new jltools.ext.jl.parse.Lexer(reader, source.name(), eq);
	grm = new jltools.ext.jl.parse.Grm(lexer, ts, nf, eq);

	return new CupParser(grm, source, eq);
    }

    public void replacePass(List passes, Pass.ID id, List newPasses) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            if (p instanceof BarrierPass) {
              throw new InternalCompilerError("Cannot replace a barrier pass.");
            }

            i.remove();

            for (Iterator j = newPasses.iterator(); j.hasNext(); ) {
              i.add(j.next());
            }

            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void beforePass(List passes, Pass.ID id, List newPasses) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            // Backup one position.
            i.previous();

            for (Iterator j = newPasses.iterator(); j.hasNext(); ) {
              i.add(j.next());
            }

            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void afterPass(List passes, Pass.ID id, List newPasses) {
        for (ListIterator i = passes.listIterator(); i.hasNext(); ) {
          Pass p = (Pass) i.next();

          if (p.id() == id) {
            for (Iterator j = newPasses.iterator(); j.hasNext(); ) {
              i.add(j.next());
            }

            return;
          }
        }

        throw new InternalCompilerError("Pass " + id + " not found.");
    }

    public void replacePass(List passes, Pass.ID id, Pass pass) {
        replacePass(passes, id, Collections.singletonList(pass));
    }

    public void beforePass(List passes, Pass.ID id, Pass pass) {
        beforePass(passes, id, Collections.singletonList(pass));
    }

    public void afterPass(List passes, Pass.ID id, Pass pass) {
        afterPass(passes, id, Collections.singletonList(pass));
    }

    public void removePass(List passes, Pass.ID id) {
        replacePass(passes, id, new EmptyPass(id));
    }

    public List passes(Job job) {
        ArrayList l = new ArrayList(15);

	l.add(new ParserPass(Pass.PARSE, compiler, job));

        l.add(new VisitorPass(Pass.BUILD_TYPES, job, new TypeBuilder(job, ts, nf)));
	l.add(new BarrierPass(Pass.BUILD_TYPES_ALL, job));
	l.add(new VisitorPass(Pass.CLEAN_SUPER, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SUPER)));
	l.add(new BarrierPass(Pass.CLEAN_SUPER_ALL, job));
	l.add(new VisitorPass(Pass.CLEAN_SIGS, job,
                              new AmbiguityRemover(job, ts, nf, AmbiguityRemover.SIGNATURES)));
	l.add(new VisitorPass(Pass.ADD_MEMBERS, job, new AddMemberVisitor(job, ts, nf)));
	l.add(new BarrierPass(Pass.ADD_MEMBERS_ALL, job));
	l.add(new VisitorPass(Pass.DISAM, job, new
                              AmbiguityRemover(job, ts, nf, AmbiguityRemover.ALL)));
	l.add(new VisitorPass(Pass.FOLD, job, new ConstantFolder(ts, nf)));
	l.add(new BarrierPass(Pass.DISAM_ALL, job));
        l.add(new VisitorPass(Pass.TYPE_CHECK, job, new TypeChecker(job, ts, nf)));
        l.add(new VisitorPass(Pass.SET_EXPECTED_TYPES, job, new ExpectedTypeVisitor(job, ts, nf)));
	l.add(new VisitorPass(Pass.EXC_CHECK, job, new ExceptionChecker(ts, compiler.errorQueue())));
	l.add(new BarrierPass(Pass.PRE_OUTPUT_ALL, job));

	if (compiler.dumpAst()) {
	    l.add(new VisitorPass(Pass.DUMP, job,
				  new DumpAst(new CodeWriter(System.err, 78))));
	}

	if (compiler.serializeClassInfo()) {
	    l.add(new VisitorPass(Pass.SERIALIZE,
				  job, new ClassSerializer(ts, nf,
							   job.source().lastModified(),
							   compiler.errorQueue())));
	}

	l.add(new Translator(Pass.OUTPUT, job, ts, nf, targetFactory()));

        return l;
    }

    public List passes(Job job, Pass.ID begin, Pass.ID end) {
        List l = passes(job);
        boolean in = false;

        for (Iterator i = l.iterator(); i.hasNext(); ) {
            Pass p = (Pass) i.next();
            in = in || begin == p.id();
            if (! (p instanceof BarrierPass) && ! in) i.remove();
            in = in && end != p.id();
        }

        return l;
    }

    static { Report.topics.add("jl"); }
    static { Report.topics.add("verbose"); }
    static { Report.topics.add("types"); }
    static { Report.topics.add("frontend"); }
}
