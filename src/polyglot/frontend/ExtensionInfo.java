package polyglot.frontend;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import java.io.*;
import java.util.*;
import polyglot.main.UsageError;
import polyglot.main.Options;

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
    polyglot.main.Version version();

    /** Report the options accepted by the extension.
        Output is newline-terminated if non-empty. */
    String options();

    /**
     * Initialize the extension with a particular compiler.  This must
     * be called after the compiler is initialized, but before the compiler
     * starts work.
     */
    void initCompiler(polyglot.frontend.Compiler compiler);

    Compiler compiler();

    /** The extension that source files are expected to have. */
    String fileExtension();

    /** The default extension that source files are expected to have. */
    String defaultFileExtension();

    /** Produce a type system for this language extension. */
    TypeSystem typeSystem();

    /** Produce a node factory for this language extension. */
    NodeFactory nodeFactory();

    /** Produce a source factory for this language extension. */
    SourceLoader sourceLoader();

    /** Produce a job for the given source. */
    SourceJob addJob(Source source);

    /** Produce a job for a given source using the given AST. */
    SourceJob addJob(Source source, Node ast);

    /** Produce a job for the given AST (possibly null). */
    SourceJob createJob(Job parent, Source source, Node ast);

    /** Produce a job for the given context. */
    Job createJob(Node ast, Context context, Job outer, Pass.ID begin, Pass.ID end);

    /** Run all jobs to completion. */
    boolean runToCompletion();

    /** Run the given job up to a given pass. */
    boolean runToPass(Job job, Pass.ID goal);

    /** Run the given job to completion. */
    boolean runAllPasses(Job job);

    /** Read a source file and compile up to the current job's barrier. */
    boolean readSource(FileSource source);

    /**
     * Produce a target factory for this language extension.  The target
     * factory is responsible for naming and opening output files given a
     * package name and a class or source file name.
     */
    TargetFactory targetFactory();

    /** Get a parser for this language extension. */
    Parser parser(Reader reader, FileSource source, ErrorQueue eq);

    /** Get the list of passes for a given source job. */
    List passes(Job job);

    /** Get the sublist of passes for a given job. */
    List passes(Job job, Pass.ID begin, Pass.ID end);

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
