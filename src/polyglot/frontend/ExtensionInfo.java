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

    /** Report the version of the extension. */
    jltools.main.Version version();

    /** Report the options accepted by the extension.
        Output is newline-terminated if non-empty. */
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
    NodeFactory nodeFactory();

    /** Produce a source factory for this language extension. */
    SourceLoader sourceLoader();

    /** Produce a job for the given source. */
    SourceJob createJob(Job parent, Source source);

    /** Produce a job for the given context. */
    Job createJob(Node ast, Context context, Job outer, Pass.ID begin, Pass.ID end);

    /** Produce a target factory for this language extension. */
    TargetFactory targetFactory();

    /** Get a parser for this language extension. */
    Parser parser(Reader reader, Source source, ErrorQueue eq);

    /** Get the list of passes for a given source job. */
    List passes(SourceJob job);

    /**
     * Get the sublist of passes for a given job that performs AST
     * transformations.
     */
    List transformPasses(Job job);

    /**
     * Get the sublist of passes for a given job that performs AST
     * transformations.
     */
    List transformPasses(Job job, Pass.ID begin, Pass.ID end);

    /** Add a pass before an existing pass. */
    void beforePass(List passes, Pass.ID oldPass, Pass newPass);

    /** Add a list of passes before an existing pass. */
    void beforePass(List passes, Pass.ID oldPass, List newPasses);

    /** Add a pass after an existing pass. */
    void afterPass(List passes, Pass.ID oldPass, Pass newPass);

    /** Add a list of passes after an existing pass. */
    void afterPass(List passes, Pass.ID oldPass, List newPasses);

    /** Replace an existing pass with a new pass. */
    void replacePass(List passes, Pass.ID oldPass, Pass newPass);

    /** Replace an existing pass with a list of new passes. */
    void replacePass(List passes, Pass.ID oldPass, List newPasses);

    /** Remove a pass.  The removed pass cannot be a barrier. */
    void removePass(List passes, Pass.ID oldPass);
}
