package jltools.frontend;

import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import java.io.*;
import java.util.*;
import jltools.main.UsageError;
import jltools.main.Options;

/**
 * <code>ExtensionInfo</code> is the main interface for defining language
 * extensions.  The frontend will load the <code>ExtensionInfo</code>
 * specified on the command-line.  It defines the type system, AST node
 * factory, parser, and other parameters of a language extension.
 */
public interface ExtensionInfo {
    /** Parse as much of the command line as this extension understands,
     *  up to the first source file. Return the index of the first
     *  switch not understood by the extension, or of the first
     *  source file, or args.length if neither. */
    int parseCommandLine(String args[], int index, Options options)
	throws UsageError;

    /**
     * Initialize the extension with the command-line options.
     * This must be called before any other method of the extension except
     * the command-line parsing methods:
     *     compilerName()
     *     options()
     *     parseCommandLine()
     */
    void setOptions(Options options) throws UsageError;

    /** The name of the compiler for usage messages */
    String compilerName();

    /** Options accepted by the extension. Newline terminated if non-empty */
    String options();

    /**
     * Initialize the extension with a particular compiler.  This must
     * be called after the compiler is initialized, but before the compiler
     * starts work.
     */
    void initCompiler(jltools.frontend.Compiler compiler);

    /** The default extension that source files are expected to have */
    String fileExtension();

    /** Produce a type system for this language extension. */
    TypeSystem typeSystem();
    
    /** Produce a node factory for this language extension. */
    // NodeFactory nodeFactory();
    ExtensionFactory extensionFactory();

    /** Produce a source factory for this language extension. */
    SourceLoader sourceLoader();

    /** Produce a job used to compile the given source. */
    Job createJob(Source source);

    /** Produce a target factory for this language extension. */
    TargetFactory targetFactory();

    /** Get a list of compiler passes for a given job. */
    Scheduler scheduler();

    /** Create a pass for a particular job. */
    Pass getPass(Job job, PassID key);

    /** Get a parser for this language extension. */
    Parser parser(Reader reader, Job job);
}
