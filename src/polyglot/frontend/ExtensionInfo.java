package polyglot.frontend;

import java.io.Reader;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;

import polyglot.ast.NodeFactory;
import polyglot.frontend.goals.Goal;
import polyglot.main.Options;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.util.ErrorQueue;
import polyglot.util.CodeWriter;

/**
 * <code>ExtensionInfo</code> is the main interface for defining language
 * extensions.  The frontend will load the <code>ExtensionInfo</code>
 * specified on the command-line.  It defines the type system, AST node
 * factory, parser, and other parameters of a language extension.
 */
public interface ExtensionInfo {
    /** The name of the compiler for usage messages */
    String compilerName();

    /** Report the version of the extension. */
    polyglot.main.Version version();

    /** Returns the pass scheduler. */
    Scheduler scheduler();
    
    /**
     * Return the goal for compiling a particular compilation unit.
     * The goal may have subgoals on which it depends.
     */
    Goal getCompileGoal(Job job);
    
    /** 
     * Return an Options object, which will be given the command line to parse.
     */    
    Options getOptions();

    /**
     * Return a Stats object to accumulate and report statistics.
     */ 
    Stats getStats();

    /**
     * Initialize the extension with a particular compiler.  This must
     * be called after the compiler is initialized, but before the compiler
     * starts work.
     */
    void initCompiler(polyglot.frontend.Compiler compiler);

    Compiler compiler();

    /** The extensions that source files are expected to have.
     * Defaults to the array defaultFileExtensions. */
    String[] fileExtensions();

    /** The default extensions that source files are expected to have.
     * Defaults to an array containing defaultFileExtension */
    String[] defaultFileExtensions();

    /** The default extension that source files are expected to have. */
    String defaultFileExtension();

    /** Produce a type system for this language extension. */
    TypeSystem typeSystem();

    /** Produce a node factory for this language extension. */
    NodeFactory nodeFactory();

    /** Produce a source factory for this language extension. */
    SourceLoader sourceLoader();

    /**
     * Get the job extension for this language extension.  The job
     * extension is used to extend the <code>Job</code> class
     * without subtyping.
     */
    JobExt jobExt();
    
    /**
     * Produce a target factory for this language extension.  The target
     * factory is responsible for naming and opening output files given a
     * package name and a class or source file name.
     */
    TargetFactory targetFactory();

    /** Get a parser for this language extension. */
    Parser parser(Reader reader, FileSource source, ErrorQueue eq);

    /** Create class file */ 
    ClassFile createClassFile(File classFileSource, byte[] code);

    /** Create file source for a file. The main purpose is to allow
        the character encoding to be defined. */
    FileSource createFileSource(File sourceFile, boolean userSpecified)
	throws IOException;

}
