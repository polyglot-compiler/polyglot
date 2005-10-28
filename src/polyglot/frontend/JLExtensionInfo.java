package polyglot.ext.jl;

import java.io.Reader;
import java.io.OutputStream;
import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl.ast.NodeFactory_c;
import polyglot.ext.jl.parse.Grm;
import polyglot.ext.jl.parse.Lexer_c;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

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
 * <li> disambiguate (AmbiguityRemover) </li>
 * <hr>
 * <li> type checking (TypeChecker) </li>
 * <li> reachable checking (ReachChecker) </li>
 * <li> exception checking (ExceptionChecker)
 * <li> exit checking (ExitChecker)
 * <li> initialization checking (InitChecker)
 * <li> circular constructor call checking (ConstructorCallChecker)
 * <hr>
 * <center>PRE_OUTPUT MARKER</center>
 * <hr>
 * <li> serialization (ClassSerializer), optional </li>
 * <li> translation (Translator) </li>
 * </ol>
 */
public class ExtensionInfo extends polyglot.frontend.AbstractExtensionInfo {
    protected void initTypeSystem() {
	try {
            LoadedClassResolver lr;
            lr = new SourceClassResolver(compiler, this, getOptions().constructFullClasspath(),
                                         compiler.loader(), true, getOptions().compile_command_line_only);

            // Resolver to handle lookups of member classes.
            MemberClassResolver mcr = new MemberClassResolver(ts, lr, true);

            ts.initialize(mcr, this);
	}
	catch (SemanticException e) {
	    throw new InternalCompilerError(
		"Unable to initialize type system: " + e.getMessage());
	}
    }
    
    protected polyglot.frontend.Scheduler createScheduler() {
        return new JLScheduler(this);
    }

    public String defaultFileExtension() {
        return "jl";
    }

    public String compilerName() {
	return "jlc";
    }

    public polyglot.main.Version version() {
	return new Version();
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

    /**
     * Return a parser for <code>source</code> using the given
     * <code>reader</code>.
     */
    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
	polyglot.lex.Lexer lexer = new Lexer_c(reader, source, eq);
	polyglot.parse.BaseParser parser = new Grm(lexer, ts, nf, eq);

	return new CupParser(parser, source, eq);
    }
    
    /**
     * Return the <code>Goal</code> to compile the source file associated with
     * <code>job</code> to completion.
     */
    public Goal getCompileGoal(Job job) {
        return scheduler.CodeGenerated(job);
    }

    static { Topics t = new Topics(); }


}
