package polyglot.ext.jl;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.ext.jl.ast.*;
import polyglot.ext.jl.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.UsageError;
import polyglot.main.Options;
import polyglot.main.Report;
import polyglot.frontend.Compiler;

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
public class ExtensionInfo extends polyglot.frontend.AbstractExtensionInfo {
    protected void initTypeSystem() {
	try {
            LoadedClassResolver lr;
            lr = new SourceClassResolver(compiler, this, options.classpath,
                                         compiler.loader());
            ts.initialize(lr);
	}
	catch (SemanticException e) {
	    throw new InternalCompilerError(
		"Unable to initialize type system: " + e.getMessage());
	}
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

    public polyglot.main.Version version() {
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

    /** Create the node factory for this extension. */
    protected NodeFactory createNodeFactory() {
	return new NodeFactory_c();
    }

    public JobExt jobExt() {
      return null;
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
	polyglot.ext.jl.parse.Lexer lexer;
	java_cup.runtime.lr_parser grm;

	lexer = new polyglot.ext.jl.parse.Lexer(reader, source.name(), eq);
	grm = new polyglot.ext.jl.parse.Grm(lexer, ts, nf, eq);

	return new CupParser(grm, source, eq);
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
	    l.add(new PrettyPrintPass(Pass.DUMP, job,
                                      new CodeWriter(System.err, 78),
                                      new PrettyPrinter()));
            /*
	    l.add(new VisitorPass(Pass.DUMP, job,
				  new DumpAst(new CodeWriter(System.err, 78))));
                                  */
	}

	if (compiler.serializeClassInfo()) {
	    l.add(new VisitorPass(Pass.SERIALIZE,
				  job, new ClassSerializer(ts, nf,
							   job.source().lastModified(),
							   compiler.errorQueue(),
                                                           version())));
	}

	l.add(new OutputPass(Pass.OUTPUT, job,
                             new Translator(job, ts, nf, targetFactory())));

        return l;
    }

    static { Report.topics.add("jl"); }
    static { Report.topics.add("qq"); }
}
