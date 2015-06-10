/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.main;

import static java.io.File.pathSeparator;
import static java.io.File.pathSeparatorChar;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

import polyglot.frontend.ExtensionInfo;
import polyglot.main.OptFlag.Arg;
import polyglot.main.OptFlag.IntFlag;
import polyglot.main.OptFlag.Kind;
import polyglot.main.OptFlag.PathFlag;
import polyglot.main.OptFlag.Switch;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;

/**
 * This object encapsulates various Polyglot options.
 */
public class Options {
    /**
     * An annoying hack to allow objects to get their hands on the Options
     * object. This should be fixed. XXX###@@@
     */
    public static Options global;

    /**
     * Back pointer to the extension that owns this options
     */
    public final ExtensionInfo extension;

    /*
     * Fields for storing values for options.
     */
    public int error_count;
    private File source_output_directory;
    private File class_output_directory;
    public final List<File> sourcepath_directories = new ArrayList<>();
    private final List<File> classpath_directories = new ArrayList<>();
    private final List<File> bootclasspath_directories = new ArrayList<>();

    public JavaFileManager.Location source_path = StandardLocation.SOURCE_PATH;
    public JavaFileManager.Location source_output =
            StandardLocation.SOURCE_OUTPUT;
    public JavaFileManager.Location class_output =
            StandardLocation.CLASS_OUTPUT;
    public JavaFileManager.Location classpath = StandardLocation.CLASS_PATH;
    public JavaFileManager.Location bootclasspath =
            StandardLocation.PLATFORM_CLASS_PATH;

    public boolean noOutputToFS = false;
    public boolean assertions = false;
    public boolean generate_debugging_info = false;

    public boolean compile_command_line_only = false;

    public String[] source_ext; // e.g., java, jl, pj
    public String output_ext; // java, by default
    public boolean output_stdout; // whether to output to stdout

    public String post_compiler;
    public String post_compiler_opts;

    public int output_width;
    public boolean fully_qualified_names;

    /** Inject type information in serialized form into output file? */
    public boolean serialize_type_info;

    /** Dump the AST after the following passes? */
    public final Set<String> dump_ast = new HashSet<>();

    /** Pretty-print the AST after the following passes? */
    public final Set<String> print_ast = new HashSet<>();

    /** Disable the following passes? */
    public final Set<String> disable_passes = new HashSet<>();

    /** keep output files */
    public boolean keep_output_files;

    /** Generate position information for compiler-generated code. */
    public boolean precise_compiler_generated_positions;

    /** Use SimpleCodeWriter instead of OptimalCodeWriter */
    public boolean use_simple_code_writer;

    /**
     * Parse "a" + "b" as "ab" to avoid very deep AST, e.g., for action tables,
     * and for serialization.
     */
    public boolean merge_strings;

    public boolean classpath_given;
    public boolean bootclasspath_given;

    protected final Set<OptFlag<?>> flags;
    protected final List<OptFlag.Arg<?>> arguments;

    protected boolean output_source_only;

    protected Boolean print_args;

    /**
     * Constructor
     */
    public Options(ExtensionInfo extension) {
        this(extension, true);
    }

    public Options(ExtensionInfo extension, boolean checkFlags) {
        this.extension = extension;
        flags = new LinkedHashSet<>();
        arguments = new ArrayList<>();
        populateFlags(flags);
        if (checkFlags) {
            Set<String> ids = new LinkedHashSet<>();
            for (OptFlag<?> flag : flags) {
                for (String id : flag.ids()) {
                    if (!ids.add(id)) {
                        throw new InternalCompilerError("Flag " + flag.ids()
                                + " conflicts with "
                                + OptFlag.lookupFlag(id, flags).ids());
                    }
                }
            }
        }
        setDefaultValues();
    }

    public Set<OptFlag<?>> flags() {
        return flags;
    }

    public List<OptFlag.Arg<?>> arguments() {
        return arguments;
    }

    public List<OptFlag.Arg<?>> filterArgs(Set<OptFlag<?>> flags) {
        List<Arg<?>> matches = new ArrayList<>();
        for (Arg<?> arg : arguments) {
            if (arg.flag != null && flags.contains(arg.flag()))
                matches.add(arg);
        }
        return matches;
    }

    protected void populateFlags(Set<OptFlag<?>> flags) {
        flags.add(new OptFlag<Void>(Kind.HELP, new String[] { "--help", "-h",
                "-help", "-?" }, null, "print this message") {
            @Override
            public Arg<Void> handle(String[] args, int index) throws UsageError {
                throw new UsageError("", 0);
            }
        });

        flags.add(new OptFlag<Void>(Kind.VERSION, new String[] { "--version",
                "-version" }, null, "print version info") {
            @Override
            public Arg<Void> handle(String[] args, int index) {
                StringBuffer sb = new StringBuffer();
                if (extension != null) {
                    sb.append(extension.compilerName() + " version "
                            + extension.version() + "\n");
                }
                sb.append("Polyglot compiler toolkit version "
                        + new polyglot.frontend.JLVersion());
                throw new Main.TerminationException(sb.toString(), 0);
            }
        });

        flags.add(new OptFlag<File>("-d",
                                    "<directory>",
                                    "output directory",
                                    "current directory") {
            @Override
            public Arg<File> handle(String[] args, int index) {
                File f = new File(args[index]);
                if (!f.exists()) f.mkdirs();
                return createArg(index + 1, f);
            }

            @Override
            public Arg<File> defaultArg() {
                return createDefault(new File(System.getProperty("user.dir")));
            }
        });

        flags.add(new OptFlag<File>("-D",
                                    "<directory>",
                                    "output directory for .java files",
                                    "same as -d") {

            @Override
            public Arg<File> handle(String[] args, int index) {
                File f = new File(args[index]);
                if (!f.exists()) f.mkdirs();
                return createArg(index + 1, f);
            }

            @Override
            public Arg<File> defaultArg(List<Arg<?>> args) {
                // There could be more than one arg specified.
                List<Arg<?>> outdirs = OptFlag.lookupAll("-d", args);
                if (!outdirs.isEmpty()) {
                    Arg<?> arg = outdirs.get(outdirs.size() - 1);
                    return createDefault((File) arg.value());
                }
                else return createDefault(new File(System.getProperty("user.dir")));
            }

            @Override
            public Arg<File> defaultArg() {
                throw new UnsupportedOperationException("The -D flag requires other arguments to set its default value.");
            }
        });

        flags.add(new StdPathFlag(new String[] { "-classpath", "-cp" },
                                  "<path>",
                                  "where to find user class files",
                                  "JVM property: java.class.path") {
            @Override
            public Arg<List<File>> defaultArg() {
                return handle(new String[] { System.getProperty("java.class.path") },
                              0);
            }
        });

        flags.add(new StdPathFlag("-bootclasspath",
                                  "<path>",
                                  "where to find runtime class files",
                                  "JVM property: sun.boot.class.path (or all jars in java.home/lib)") {
            @Override
            public Arg<List<File>> defaultArg() {
                return handle(new String[] { jvmbootclasspath() }, 0);
            }
        });

        flags.add(new StdPathFlag("-addbootcp",
                                  "<path>",
                                  "prepend <path> to the bootclasspath"));

        flags.add(new StdPathFlag("-sourcepath",
                                  "<path>",
                                  "where to find source files",
                                  "current directory") {
            @Override
            public Arg<List<File>> defaultArg() {
                return handle(new String[] { System.getProperty("user.dir") },
                              0);
            }
        });

        flags.add(new Switch("-commandlineonly",
                             "only compile files named on the command-line (may also require -c)"));

        flags.add(new Switch("-preferclassfiles",
                             "prefer class files to source files even if the source is newer"));

        flags.add(new Switch("-assert", "recognize the assert keyword"));

        flags.add(new Switch("-fqcn", "output fully-qualified class names"));

        flags.add(new Switch("-g", "generate debugging info in class files"));

        flags.add(new Switch("-c", "compile only to .java"));

        flags.add(new IntFlag("-errors",
                              "<num>",
                              "set the maximum number of errors",
                              100));

        flags.add(new IntFlag("-w",
                              "<num>",
                              "set the maximum width of the .java output files",
                              80));

        flags.add(new OptFlag<String>("-postcompiler",
                                      "<compiler>",
                                      "run javac-like compiler after translation") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        flags.add(new OptFlag<String>("-postopts",
                                      "<options>",
                                      "options to pass to the compiler after translation") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        flags.add(new Switch("-stdout", "output to stdout"));

        flags.add(new OptFlag<String>("-sx", "<ext>", "set source extension") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        flags.add(new OptFlag<String>("-ox", "<ext>", "set output extension") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }

            @Override
            public Arg<String> defaultArg() {
                return createDefault("java");
            }
        });

        flags.add(new Switch("-noserial", "disable class serialization"));

        flags.add(new OptFlag<String>("-dump",
                                      "<pass>",
                                      "dump the ast after pass <pass>") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        flags.add(new OptFlag<String>("-print",
                                      "<pass>",
                                      "pretty-print the ast after pass <pass>") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        flags.add(new OptFlag<String>("-disable",
                                      "<pass>",
                                      "disable pass <pass>") {
            @Override
            public Arg<String> handle(String[] args, int index)
                    throws UsageError {
                return createArg(index + 1, args[index]);
            }
        });

        flags.add(new Switch("-nooutput",
                             "delete output files after compilation"));

        flags.add(new Switch(new String[] { "-v", "-verbose" },
                             "delete output files after compilation"));

        StringBuffer allowedTopics = new StringBuffer("Allowed topics: ");
        for (Iterator<String> iter = Report.topics.iterator(); iter.hasNext();) {
            allowedTopics.append(iter.next());
            if (iter.hasNext()) {
                allowedTopics.append(", ");
            }
        }

        flags.add(new OptFlag<Pair<String, Integer>>("-report",
                                                     "<topic>=<level>",
                                                     "print verbose debugging information about"
                                                             + " topic at specified verbosity. "
                                                             + allowedTopics.toString()) {
            @Override
            public Arg<Pair<String, Integer>> handle(String[] args, int index)
                    throws UsageError {
                StringTokenizer st = new StringTokenizer(args[index], "=");
                String topic = "";
                int level = 0;
                if (st.hasMoreTokens()) topic = st.nextToken();
                if (st.hasMoreTokens()) {
                    try {
                        level = Integer.parseInt(st.nextToken());
                    }
                    catch (NumberFormatException e) {
                    }
                }
                return createArg(index + 1, new Pair<>(topic, level));
            }
        });

        flags.add(new Switch("-debugpositions",
                             "generate position information for compiler-generated code"));

        flags.add(new Switch("-simpleoutput", "use SimpleCodeWriter"));

        flags.add(new Switch("-mergestrings",
                             "parse concatenated string literals as one single string literal"));

        flags.add(new Switch(Kind.SECRET,
                             "-print-arguments",
                             "Check that no options try to handle the same command line flag."));

        flags.add(new Switch("-no-output-to-fs",
                             "keep .java files in memory if possible"));;
    }

    /**
     * A PathFlag<File> that accepts a list of directory names and filters out
     * the invalid ones.
     * 
     */
    public static class StdPathFlag extends PathFlag<File> {
        public StdPathFlag(String id, String params, String usage,
                String defaultValue) {
            super(id, params, usage, defaultValue);
        }
        public StdPathFlag(String id, String params, String usage) {
            super(id, params, usage);
        }
        public StdPathFlag(String[] ids, String params, String usage) {
            super(ids, params, usage);
        }
        public StdPathFlag(String[] ids, String params, String usage, String defaultValue) {
            super(ids, params, usage, defaultValue);
        }
        @Override
        public File handlePathEntry(String entry) {
            File f = new File(entry);
            if (f.exists())
                return f;
            else return null;
        }
    }

    /**
     * Set default values for options
     */
    @Deprecated
    public void setDefaultValues() {
    }

    /**
     * Parse the command line and process arguments.
     * 
     * @throws UsageError
     *             if the usage is incorrect.
     */
    public final void parseCommandLine(String args[], Set<String> source)
            throws UsageError {
        for (int index = 0; index < args.length;) {
            try {
                int nextIndex = parseCommand(args, index, source);
                if (nextIndex == index)
                    throw new UsageError("Illegal option: " + args[index]);
                index = nextIndex;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new UsageError("Missing argument");
            }
        }
        validateArgs();
        applyArgs(source);
        if (print_args) printCommandLine(System.out);
        postApplyArgs();
    }

    /**
     * Process a list of arguments
     * 
     * @throws UsageError
     *             if the usage is incorrect.
     */
    public final void processArguments(List<Arg<?>> arguments,
            Set<String> source) throws UsageError {
        this.arguments.clear();
        this.arguments.addAll(arguments);
        validateArgs();
        applyArgs(source);
        if (print_args) printCommandLine(System.out);
        postApplyArgs();
    }

    protected void postApplyArgs() {
        // If we are using an external post compiler, 
        // we have to output files to disk
        if (post_compiler != null || keep_output_files) noOutputToFS = false;
    }

    /**
     * Validates the arguments parsed from the command line.
     * @throws UsageError if the arguments are invalid.
     */
    protected void validateArgs() throws UsageError {
        if (arguments.size() < 1) {
            throw new UsageError("No command line arguments given");
        }

        if (!OptFlag.hasSourceArg(arguments)) {
            throw new UsageError("must specify at least one source file");
        }
    }

    /**
     * Iterates over arguments parsed from the command line and applies them to
     * this object. Any source arguments are added to {@code source}.
     * 
     * @param source
     *          The set of source filenames provided on the command line.
     */
    final protected void applyArgs(Set<String> source) throws UsageError {
        Set<OptFlag<?>> seen = new LinkedHashSet<>();
        for (Arg<?> arg : arguments) {
            if (arg.flag == null) {
                handleSourceArg(arg, source);
            }
            else {
                seen.add(arg.flag);
                try {
                    handleArg(arg);
                }
                catch (UsageError e) {
                    throw e;
                }
                catch (Throwable e) {
                    throw new InternalCompilerError("Error while handling arg "
                            + arg + " created by " + arg.flag().getClass(), e);
                }
            }
        }
        for (OptFlag<?> flag : flags) {
            if (seen.contains(flag))
                continue;
            else {
                Arg<?> arg = flag.defaultArg(arguments);
                if (arg != null) handleArg(arg);
            }
        }
    }

    /**
     * Iterates over arguments parsed from the command line and applies them to
     * this object. Any source arguments are added to {@code source}.
     * 
     * @param source
     *          The set of source filenames provided on the command line.
     */
    public void printCommandLine(PrintStream out) {
        Set<OptFlag<?>> seen = new LinkedHashSet<>();
        for (Arg<?> arg : arguments) {
            if (arg.flag != null) {
                seen.add(arg.flag);
            }
            out.print(" " + arg.toString());
        }
        for (OptFlag<?> flag : flags) {
            if (seen.contains(flag))
                continue;
            else {
                Arg<?> arg = flag.defaultArg(arguments);
                if (arg != null) out.print(" " + arg.toString());
            }
        }
        out.println();
    }

    /**
     * Performs a shallow checked cast of parameterized collections
     * @param in
     * @return
     */
    protected <To extends Collection<Param>, Param> To sccast(Object in,
            Class<Param> clazz) {
        @SuppressWarnings("unchecked")
        To out = (To) in;
        for (Param p : out) {
            if (!clazz.isInstance(p))
                throw new ClassCastException("Expected " + clazz.getName()
                        + " but " + p + " has type " + p.getClass().getName());
        }
        return out;
    }

    /**
     * Process the option specified by {@code arg}
     */
    protected void handleArg(Arg<?> arg) throws UsageError {
        assert arg.flag != null;
        Set<String> ids = arg.flag().ids();

        if (ids.contains("-d")) {
            setClassOutput((File) arg.value());
        }
        else if (ids.contains("-D")) {
            setSourceOutput((File) arg.value());
        }
        else if (ids.contains("-classpath")) {
            setClasspath(this.<List<File>, File> sccast(arg.value(), File.class));
        }
        else if (ids.contains("-bootclasspath")) {
            setBootclasspath(this.<List<File>, File> sccast(arg.value(),
                                                            File.class));
        }
        else if (ids.contains("-addbootcp")) {
            addBootCP(this.<List<File>, File> sccast(arg.value(), File.class));
        }
        else if (ids.contains("-sourcepath")) {
            setSourcepath(this.<List<File>, File> sccast(arg.value(),
                                                         File.class));
        }
        else if (ids.contains("-commandlineonly")) {
            setCommandLineOnly((Boolean) arg.value());
        }
        else if (ids.contains("-preferclassfiles")) {
            setIgnoreModTimes((Boolean) arg.value());
        }
        else if (ids.contains("-assert")) {
            setAssertions((Boolean) arg.value());
        }
        else if (ids.contains("-fqcn")) {
            setFullyQualifiedNames((Boolean) arg.value());
        }
        else if (ids.contains("-g")) {
            setGenerateDebugInfo((Boolean) arg.value());
        }
        else if (ids.contains("-c")) {
            setOutputOnly((Boolean) arg.value());
        }
        else if (ids.contains("-errors")) {
            setErrorCount((Integer) arg.value());
        }
        else if (ids.contains("-w")) {
            setOutputWidth((Integer) arg.value());
        }
        else if (ids.contains("-postcompiler")) {
            setPostCompiler((String) arg.value());
        }
        else if (ids.contains("-postopts")) {
            setPostCompilerOpts((String) arg.value());
        }
        else if (ids.contains("-stdout")) {
            setOutputStdOut((Boolean) arg.value());
        }
        else if (ids.contains("-sx")) {
            addSourceExtension((String) arg.value());
        }
        else if (ids.contains("-ox")) {
            setOutputExtension((String) arg.value());
        }
        else if (ids.contains("-noserial")) {
            setNoSerializedTypes((Boolean) arg.value());
        }
        else if (ids.contains("-dump")) {
            addDumpAST((String) arg.value());
        }
        else if (ids.contains("-print")) {
            addPrintAST((String) arg.value());
        }
        else if (ids.contains("-disable")) {
            addDisablePass((String) arg.value());
        }
        else if (ids.contains("-nooutput")) {
            setNoOutput((Boolean) arg.value());
        }
        else if (ids.contains("-verbose")) {
            setVerbose((Boolean) arg.value());
        }
        else if (ids.contains("-report")) {
            @SuppressWarnings("unchecked")
            Pair<String, Integer> pair = (Pair<String, Integer>) arg.value();
            addReportTopic(pair.part1(), pair.part2());
        }
        else if (ids.contains("-debugpositions")) {
            setDebugPositions((Boolean) arg.value());
        }
        else if (ids.contains("-simpleoutput")) {
            setSimpleOutput((Boolean) arg.value());
        }
        else if (ids.contains("-mergestrings")) {
            setMergeStrings((Boolean) arg.value());
        }
        else if (ids.contains("-print-arguments")) {
            print_args = (Boolean) arg.value();
        }
        else if (ids.contains("-no-output-to-fs")) {
            noOutputToFS = (Boolean) arg.value();
        }
        else throw new UnhandledArgument(arg);
    }

    /**
     * Process a source argument and add it to the source collection.
     * @param source
     *          The set of filenames to compile.
     */
    protected void handleSourceArg(Arg<?> arg, Set<String> source) {
        String filename = (String) arg.value();
        source.add(filename);
    }

    protected void setClassOutput(File f) {
        class_output_directory = f;
    }

    protected void setSourceOutput(File f) {
        source_output_directory = f;
    }

    protected void setClasspath(List<File> value) {
        classpathDirectories().addAll(value);
    }

    protected void setBootclasspath(List<File> value) {
        bootclasspathDirectories().addAll(value);
    }

    protected void addBootCP(List<File> value) {
        bootclasspathDirectories().addAll(value);
    }

    protected void setSourcepath(List<File> value) {
        sourcepath_directories.addAll(value);
    }

    protected void setCommandLineOnly(boolean value) {
        compile_command_line_only = value;
    }

    protected void setIgnoreModTimes(boolean value) {
        ignore_mod_times = value;
    }

    protected void setAssertions(boolean value) {
        assertions = value;
    }

    protected void setFullyQualifiedNames(boolean value) {
        fully_qualified_names = value;
    }

    protected void setGenerateDebugInfo(boolean value) {
        generate_debugging_info = value;
    }

    protected void setOutputOnly(boolean value) {
        output_source_only = value;
    }

    protected void setErrorCount(Integer value) {
        error_count = value;
    }

    protected void setOutputWidth(Integer value) {
        output_width = value;
    }

    protected void setPostCompiler(String value) {
        post_compiler = value;
    }

    protected void setPostCompilerOpts(String value) {
        post_compiler_opts = value;
    }

    protected void setOutputStdOut(boolean value) {
        output_stdout = value;
    }

    protected void addSourceExtension(String value) {
        if (source_ext == null) {
            source_ext = new String[] { value };
        }
        else {
            String[] s = new String[source_ext.length + 1];
            System.arraycopy(source_ext, 0, s, 0, source_ext.length);
            s[s.length - 1] = value;
            source_ext = s;
        }
    }

    protected void setOutputExtension(String value) {
        output_ext = value;
    }

    protected void setNoSerializedTypes(boolean value) {
        serialize_type_info = !value;
    }

    protected void addDumpAST(String value) {
        dump_ast.add(value);
    }

    protected void addPrintAST(String value) {
        print_ast.add(value);
    }

    protected void addDisablePass(String value) {
        disable_passes.add(value);
    }

    protected void setNoOutput(boolean value) {
        keep_output_files = !value;
        if (value) {
            // If we do not keep the output files, set the output_width to a
            // large number to reduce the time spent pretty-printing
            output_width = 1000;
        }
    }

    protected void addReportTopic(String topic, Integer level) {
        Report.addTopic(topic, level);
    }

    protected void setVerbose(boolean value) {
        if (value) Report.addTopic("verbose", 1);
    }

    protected void setDebugPositions(boolean value) {
        precise_compiler_generated_positions = value;
    }

    protected void setSimpleOutput(boolean value) {
        use_simple_code_writer = value;
    }

    protected void setMergeStrings(boolean value) {
        merge_strings = value;
    }

    /**
     * Parse a command
     * 
     * @return the next index to process. i.e., if calling this method processes
     *         two commands, then the return value should be index+2
     */
    protected int parseCommand(String args[], int index, Set<String> source)
            throws UsageError, Main.TerminationException {
        // Find a flag whose id matches args[index] and let it process the
        // arguments.
        for (OptFlag<?> flag : flags) {
            if (flag.ids.contains(args[index])) {
                Arg<?> arg = flag.handle(args, index + 1);
                arguments.add(arg);
                return arg.next();
            }
        }

        if (!args[index].startsWith("-")) {
            index = parseSourceArg(args, index);
        }

        return index;
    }

    protected int parseSourceArg(String[] args, int index) {
        Arg<String> src = new Arg<>(index + 1, args[index]);
        arguments.add(src);
        return src.next();
    }

    public Location outputLocation() {
        return source_output;
    }

    public Location classOutputLocation() {
        return class_output;
    }

    public File classOutputDirectory() {
        return class_output_directory;
    }

    public void usageHeader(PrintStream out) {
        out.println("usage: " + extension.compilerName() + " [options] "
                + "<source-file>." + extension.fileExtensions()[0] + " ...");
        out.println("where [options] includes:");
//      usageForFlag(out, "@<file>", "read options from <file>");
    }

    public void usage(PrintStream out, boolean showSecretMenu) {
        usageHeader(out);

        boolean firstSecretItem = true;
        List<OptFlag<?>> sorted = new ArrayList<>(flags);
        Collections.sort(sorted, null);
        for (OptFlag<?> flag : sorted) {
            boolean isSecret = flag.kind.compareTo(Kind.SECRET) >= 0;
            if (showSecretMenu && isSecret && firstSecretItem) {
                out.println();
                out.println("Secret menu:");
                firstSecretItem = false;
            }

            if (showSecretMenu || !isSecret) flag.printUsage(out);
        }
    }

    /**
     * Print usage information
     */
    public void usage(PrintStream out) {
        usage(out, false);
    }

    /**
     * The maximum width of a line when printing usage information. Used by
     * {@code usageForFlag} and {@code usageSubsection}.
     */
    protected int USAGE_SCREEN_WIDTH = 76;
    /**
     * The number of spaces from the left that the descriptions for flags will
     * be displayed. Used by {@code usageForFlag}.
     */
    protected int USAGE_FLAG_WIDTH = 27;
    /**
     * The number of spaces to indent a subsection of usage information. Used by
     * {@code usageSubsection}.
     */
    protected int USAGE_SUBSECTION_INDENT = 8;

    /**
     * Ignore source and class file modification times when compiling; always
     * prefer the class file.
     */
    public boolean ignore_mod_times;

    /**
     * Output a flag and a description of its usage in a nice format. This makes
     * it easier for extensions to output their usage in a consistent format.
     * 
     * @param out
     *            output PrintStream
     * @param flag
     * @param description
     *            description of the flag.
     */
    protected void usageForFlag(PrintStream out, String flag, String description) {
        out.print("  ");
        out.print(flag);
        // cur is where the cursor is on the screen.
        int cur = flag.length() + 2;

        // print space to get up to indentation level
        if (cur < USAGE_FLAG_WIDTH) {
            printSpaces(out, USAGE_FLAG_WIDTH - cur);
        }
        else {
            // the flag is long. Get a new line before printing the
            // description.
            out.println();
            printSpaces(out, USAGE_FLAG_WIDTH);
        }
        cur = USAGE_FLAG_WIDTH;

        // break up the description.
        StringTokenizer st = new StringTokenizer(description);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (cur + s.length() > USAGE_SCREEN_WIDTH) {
                out.println();
                printSpaces(out, USAGE_FLAG_WIDTH);
                cur = USAGE_FLAG_WIDTH;
            }
            out.print(s);
            cur += s.length();
            if (st.hasMoreTokens()) {
                if (cur + 1 > USAGE_SCREEN_WIDTH) {
                    out.println();
                    printSpaces(out, USAGE_FLAG_WIDTH);
                    cur = USAGE_FLAG_WIDTH;
                }
                else {
                    out.print(" ");
                    cur++;
                }
            }
        }
        out.println();
    }

    /**
     * Output a section of text for usage information. This text will be
     * displayed indented a certain amount from the left, controlled by the
     * field {@code USAGE_SUBSECTION_INDENT}
     * 
     * @param out
     *            the output PrintStream
     * @param text
     *            the text to output.
     */
    protected void usageSubsection(PrintStream out, String text) {
        // print space to get up to indentation level
        printSpaces(out, USAGE_SUBSECTION_INDENT);

        // cur is where the cursor is on the screen.
        int cur = USAGE_SUBSECTION_INDENT;

        // break up the description.
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (cur + s.length() > USAGE_SCREEN_WIDTH) {
                out.println();
                printSpaces(out, USAGE_SUBSECTION_INDENT);
                cur = USAGE_SUBSECTION_INDENT;
            }
            out.print(s);
            cur += s.length();
            if (st.hasMoreTokens()) {
                if (cur + 1 > USAGE_SCREEN_WIDTH) {
                    out.println();
                    printSpaces(out, USAGE_SUBSECTION_INDENT);
                    cur = USAGE_SUBSECTION_INDENT;
                }
                else {
                    out.print(' ');
                    cur++;
                }
            }
        }
        out.println();
    }

    /**
     * Utility method to print a number of spaces to a PrintStream.
     * 
     * @param out
     *            output PrintStream
     * @param n
     *            number of spaces to print.
     */
    protected static void printSpaces(PrintStream out, int n) {
        while (n-- > 0) {
            out.print(' ');
        }
    }

    /**
     * Construct the classpath for the post-compiler. This implementation
     * constructs a path from the source output directory, the current
     * directory, the classpath, and the bootclasspath.
     * 
     * @return
     */
    public String constructPostCompilerClasspath() {
        StringBuilder builder = new StringBuilder();

        builder.append(sourceOutputDirectory().getAbsolutePath());
        builder.append(pathSeparatorChar);

        builder.append('.');
        builder.append(pathSeparatorChar);

        for (File f : classpathDirectories()) {
            builder.append(f.getAbsolutePath());
            builder.append(pathSeparatorChar);
        }

        for (File f : bootclasspathDirectories()) {
            builder.append(f.getAbsolutePath());
            builder.append(pathSeparatorChar);
        }
        return builder.toString();
    }

    public String jvmbootclasspath() {
        String boot = System.getProperty("sun.boot.class.path");
        if (boot == null) {
            boot = "";
            // TODO : make external config property file.
            File java_home_libdir;
            if (System.getProperty("os.name").indexOf("Mac") != -1) {
                // XXX: gross!
                java_home_libdir =
                        new File(System.getProperty("java.home")
                                + File.separator + ".." + File.separator
                                + "Classes");
            }
            else {
                java_home_libdir =
                        new File(System.getProperty("java.home")
                                + File.separator + "lib");
            }
            File[] files = java_home_libdir.listFiles();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith("jar")) {
                    sb.append(files[i]);
                    if (i + 1 < files.length) sb.append(';');
                }
            }
            boot = sb.toString();
        }
        return boot;
    }

    public List<File> defaultPlatformClasspath() {
        List<File> path = new ArrayList<>();
        StringTokenizer st =
                new StringTokenizer(jvmbootclasspath(), pathSeparator);
        while (st.hasMoreTokens()) {
            File next = new File(st.nextToken());
            path.add(next);
        }
        return path;
    }

    public List<File> classpathDirectories() {
        return classpath_directories;
    }

    public List<File> bootclasspathDirectories() {
        return bootclasspath_directories;
    }

    public File sourceOutputDirectory() {
        return source_output_directory;
    }

}
// vim: ts=4
