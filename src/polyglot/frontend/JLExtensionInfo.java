package polyglot.ext.jl;

import java.io.Reader;
import java.util.*;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl.ast.NodeFactory_c;
import polyglot.ext.jl.parse.Grm;
import polyglot.ext.jl.parse.Lexer_c;
import polyglot.ext.jl.types.TypeSystem_c;
import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;

/**
 * This is the default <code>ExtensionInfo</code> for the Java language.
 *
 * Compilation passes and visitors:
 * <ol>
 * <li> parse </li>
 * <li> build-types (TypeBuilder) </li>
 * <hr>
 * <center>GLOBAL BARRIER</center>
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
 * <hr>
 * <center>BARRIER</center>
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
                                         compiler.loader(), true);
            ts.initialize(lr, this);
	}
	catch (SemanticException e) {
	    throw new InternalCompilerError(
		"Unable to initialize type system: " + e.getMessage());
	}
    }
    
    public polyglot.frontend.Scheduler scheduler() {
        return polyglot.ext.jl.Scheduler(this);
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
	polyglot.lex.Lexer lexer = new Lexer_c(reader, source.name(), eq);
	polyglot.parse.BaseParser parser = new Grm(lexer, ts, nf, eq);

	return new CupParser(parser, source, eq);
    }
    
    protected List compileGoalList(Job job) {
        Goal parse = scheduler.Parsed(job);
        Goal buildTypes = scheduler.TypesInitialized(job);
        Goal buildTypesBarrier = scheduler.TypesInitializedForCommandLine();
        Goal typeCheck = scheduler.TypeChecked(job);
        Goal constCheck = scheduler.ConstantsChecked(job);
        Goal reachCheck = scheduler.ReachabilityChecked(job);
        Goal excCheck = scheduler.ExceptionsChecked(job);
        Goal exitCheck = scheduler.ExitPathsChecked(job);
        Goal initCheck = scheduler.InitializationsChecked(job);
        Goal ctorCheck = scheduler.ConstructorCallsChecked(job);
        Goal frefCheck = scheduler.ForwardReferencesChecked(job);
        Goal serialize = scheduler.Serialized(job);
        Goal output = scheduler.CodeGenerated(job);
        
        List l = new ArrayList(15);
        
        l.add(parse);
        l.add(buildTypes);
        l.add(buildTypesBarrier);
        l.add(typeCheck);
        l.add(constCheck);
        l.add(reachCheck);
        l.add(excCheck);
        l.add(exitCheck);
        l.add(initCheck);
        l.add(ctorCheck);
        l.add(frefCheck);
        l.add(serialize);
        l.add(output);
        
        return l;
    }
    
    /**
     * Return the <code>Goal</code> to compile the source file associated with
     * <code>job</code> to completion.
     */
    public Goal getCompileGoal(Job job) {
        try {
            List l = compileGoalList(job);
            Iterator i = l.iterator();
            
            if (! i.hasNext()) {
                throw new InternalCompilerError("Empty list of compile goals.");
            }
            
            Goal prev = (Goal) i.next();
            
            while (i.hasNext()) {
                Goal g = (Goal) i.next();
                scheduler.addPrerequisiteDependency(g, prev);
                prev = g;
            }
            
            return prev;
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e.getMessage());
        }
    }
    
    static { Topics t = new Topics(); }
}
