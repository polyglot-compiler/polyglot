/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.tools.FileObject;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.util.QuotedStringTokenizer;
import polyglot.util.StdErrorQueue;

/**
 * Main is the main program of the extensible compiler. It should not need to be
 * replaced.
 */
public class Main {

    /** Source files specified on the command line */
    private Set<String> source;

    public final static String verbose = "verbose";

    /* modifies args */
    protected ExtensionInfo getExtensionInfo(List<String> args)
            throws TerminationException {
        ExtensionInfo ext = null;

        for (Iterator<String> i = args.iterator(); i.hasNext();) {
            String s = i.next();
            if (s.equals("-ext") || s.equals("-extension")) {
                if (ext != null) {
                    throw new TerminationException("only one extension can be specified");
                }

                i.remove();
                if (!i.hasNext()) {
                    throw new TerminationException("missing argument");
                }
                String extName = i.next();
                i.remove();
                ext =
                        loadExtension("polyglot.ext." + extName
                                + ".ExtensionInfo");
            }
            else if (s.equals("-extclass")) {
                if (ext != null) {
                    throw new TerminationException("only one extension can be specified");
                }

                i.remove();
                if (!i.hasNext()) {
                    throw new TerminationException("missing argument");
                }
                String extClass = i.next();
                i.remove();
                ext = loadExtension(extClass);
            }
        }
        if (ext != null) {
            return ext;
        }
        return loadExtension("polyglot.frontend.JLExtensionInfo");
    }

    public void start(String[] argv) throws TerminationException {
        start(argv, null, null);
    }

    public void start(String[] argv, ExtensionInfo ext)
            throws TerminationException {
        start(argv, ext, null);
    }

    public void start(String[] argv, ErrorQueue eq) throws TerminationException {
        start(argv, null, eq);
    }

    public void start(String[] argv, ExtensionInfo ext, ErrorQueue eq)
            throws TerminationException {
        source = new LinkedHashSet<String>();
        List<String> args = explodeOptions(argv);
        if (ext == null) {
            ext = getExtensionInfo(args);
        }
        Options options = ext.getOptions();

        // Allow all objects to get access to the Options object. This hack
        // should
        // be fixed somehow. XXX###@@@
        Options.global = options;
        try {
            argv = args.toArray(new String[0]);
            options.parseCommandLine(argv, source);
        }
        catch (UsageError ue) {
            PrintStream out = (ue.exitCode == 0 ? System.out : System.err);
            if (ue.getMessage() != null && ue.getMessage().length() > 0) {
                out.println(ext.compilerName() + ": " + ue.getMessage());
            }
            options.usage(out);
            throw new TerminationException(ue.exitCode);
        }

        if (eq == null) {
            eq =
                    new StdErrorQueue(System.err,
                                      options.error_count,
                                      ext.compilerName());
        }

        Compiler compiler = new Compiler(ext, eq);

        long time0 = System.currentTimeMillis();

        if (!compiler.compileFiles(source)) {
            throw new TerminationException(1);
        }

        if (Report.should_report(verbose, 1))
            Report.report(1, "Output files: " + compiler.outputFiles());

        Collection<JavaFileObject> outputFiles = compiler.outputFiles();
        if (outputFiles == null || outputFiles.size() == 0) return;

        long start_time = System.currentTimeMillis();

        /* Now call javac or jikes, if necessary. */
        if (!invokePostCompiler(options, compiler, eq)) {
            throw new TerminationException(1);
        }

        if (Report.should_report(verbose, 1)) {
            reportTime("Finished compiling Java output files. time="
                               + (System.currentTimeMillis() - start_time),
                       1);

            reportTime("Total time=" + (System.currentTimeMillis() - time0), 1);
        }
    }

    protected boolean invokePostCompiler(Options options, Compiler compiler,
            ErrorQueue eq) {
        if (!options.output_source_only && !options.output_stdout) {
            try {
                if (options.post_compiler == null) {
                    ArrayList<String> postCompilerArgs =
                            new ArrayList<String>(1);
                    if (options.generate_debugging_info)
                        postCompilerArgs.add("-g");
                    ByteArrayOutputStream err = new ByteArrayOutputStream();
                    Writer javac_err = new OutputStreamWriter(err);
                    JavaFileManager fileManager =
                            compiler.sourceExtension().extFileManager();
                    CompilationTask task =
                            ToolProvider.getSystemJavaCompiler()
                                        .getTask(javac_err,
                                                 fileManager,
                                                 null,
                                                 postCompilerArgs,
                                                 null,
                                                 compiler.outputFiles());

                    if (!task.call())
                        eq.enqueue(ErrorInfo.POST_COMPILER_ERROR,
                                   err.toString());
                }
                else {
                    Runtime runtime = Runtime.getRuntime();
                    QuotedStringTokenizer st =
                            new QuotedStringTokenizer(options.post_compiler);
                    int pc_size = st.countTokens();
                    int options_size = 2;
                    if (options.class_output_directory != null) {
                        options_size += 2;
                    }
                    if (options.generate_debugging_info) options_size++;
                    String[] javacCmd =
                            new String[pc_size + options_size
                                    + compiler.outputFiles().size()];
                    int j = 0;
                    for (int i = 0; i < pc_size; i++) {
                        javacCmd[j++] = st.nextToken();
                    }
                    javacCmd[j++] = "-classpath";
                    javacCmd[j++] = options.constructPostCompilerClasspath();
                    if (options.class_output_directory != null) {
                        javacCmd[j++] = "-d";
                        javacCmd[j++] =
                                options.class_output_directory.getPath();
                    }
                    if (options.generate_debugging_info) {
                        javacCmd[j++] = "-g";
                    }

                    for (JavaFileObject jfo : compiler.outputFiles()) {
                        URI jfoURI = jfo.toUri();
                        // XXX: the JavaCompiler API spec says toURI() must be absolute, 
                        //      but OSX does not put a scheme component on files.
                        File outfile;
                        if (!jfoURI.isAbsolute())
                            outfile = new File(jfoURI.getPath());
                        else outfile = new File(jfoURI);
                        javacCmd[j++] = outfile.getAbsolutePath();
                    }
                    if (Report.should_report(verbose, 1)) {
                        StringBuffer cmdStr = new StringBuffer();
                        for (int i = 0; i < javacCmd.length; i++)
                            cmdStr.append(javacCmd[i] + " ");
                        Report.report(1, "Executing post-compiler " + cmdStr);
                    }

                    Process proc = runtime.exec(javacCmd);

                    InputStreamReader err =
                            new InputStreamReader(proc.getErrorStream());

                    try {
                        char[] c = new char[72];
                        int len;
                        StringBuffer sb = new StringBuffer();
                        while ((len = err.read(c)) > 0) {
                            sb.append(String.valueOf(c, 0, len));
                        }

                        if (sb.length() != 0) {
                            eq.enqueue(ErrorInfo.POST_COMPILER_ERROR,
                                       sb.toString());
                        }
                    }
                    finally {
                        err.close();
                    }

                    proc.waitFor();

                    if (!options.keep_output_files) {
                        for (FileObject fo : compiler.outputFiles())
                            fo.delete();
                    }

                    if (proc.exitValue() > 0) {
                        eq.enqueue(ErrorInfo.POST_COMPILER_ERROR,
                                   "Non-zero return code: " + proc.exitValue());
                        return false;
                    }
                }
            }
            catch (Exception e) {
                eq.enqueue(ErrorInfo.POST_COMPILER_ERROR, e.getMessage());
                return false;
            }
        }
        return true;
    }

    private List<String> explodeOptions(String[] args)
            throws TerminationException {
        LinkedList<String> ll = new LinkedList<String>();

        for (int i = 0; i < args.length; i++) {
            // special case for the @ command-line parameter
            if (args[i].startsWith("@")) {
                String fn = args[i].substring(1);
                try {
                    BufferedReader lr = new BufferedReader(new FileReader(fn));
                    LinkedList<String> newArgs = new LinkedList<String>();

                    while (true) {
                        String l = lr.readLine();
                        if (l == null) break;

                        StringTokenizer st = new StringTokenizer(l, " ");
                        while (st.hasMoreTokens())
                            newArgs.add(st.nextToken());
                    }

                    lr.close();
                    ll.addAll(newArgs);
                }
                catch (java.io.IOException e) {
                    throw new TerminationException("cmdline parser: couldn't read args file "
                            + fn);
                }
                continue;
            }

            ll.add(args[i]);
        }

        return ll;
    }

    public static void main(String args[]) {
        try {
            new Main().start(args);
        }
        catch (TerminationException te) {
            if (te.getMessage() != null)
                (te.exitCode == 0 ? System.out : System.err).println(te.getMessage());
            System.exit(te.exitCode);
        }
    }

    static ExtensionInfo loadExtension(String ext) throws TerminationException {
        if (ext != null && !ext.equals("")) {
            Class<?> extClass = null;

            try {
                extClass = Class.forName(ext);
            }
            catch (ClassNotFoundException e) {
                throw new TerminationException("Extension " + ext
                        + " not found: could not find class " + ext + ".");
            }

            Object extobj;
            try {
                extobj = extClass.newInstance();
            }
            catch (Exception e) {
                throw new InternalCompilerError("Extension " + ext
                        + " could not be loaded: could not instantiate " + ext
                        + ".", e);
            }
            try {
                return (ExtensionInfo) extobj;
            }
            catch (ClassCastException e) {
                throw new TerminationException(ext
                        + " is not a valid Polyglot extension:"
                        + " extension class " + ext
                        + " exists but is not a subclass of ExtensionInfo.");
            }
        }
        return null;
    }

    static private Collection<String> timeTopics = new ArrayList<String>(1);
    static {
        timeTopics.add("time");
    }

    static private void reportTime(String msg, int level) {
        Report.report(level, msg);
    }

    /**
     * This exception signals termination of the compiler. It should be used
     * instead of <code>System.exit</code> to allow Polyglot to be called within
     * a JVM that wasn't started specifically for Polyglot, e.g. the Apache ANT
     * framework.
     */
    public static class TerminationException extends RuntimeException {
        final public int exitCode;

        public TerminationException(String msg) {
            this(msg, 1);
        }

        public TerminationException(int exit) {
            this.exitCode = exit;
        }

        public TerminationException(String msg, int exit) {
            super(msg);
            this.exitCode = exit;
        }
    }
}
