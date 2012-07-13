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

import static java.io.File.pathSeparator;
import static java.io.File.pathSeparatorChar;
import static java.io.File.separator;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;

import polyglot.frontend.ExtensionInfo;

/**
 * This object encapsulates various polyglot options.
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
	public ExtensionInfo extension = null;

	/*
	 * Fields for storing values for options.
	 */
	public int error_count = 100;
	public Set<File> sourcepath_directories = new LinkedHashSet<File>();
	public Collection<File> source_output_dir;
	public String source_output_directory;
	public Collection<File> class_output_dir;
	public String class_output_directory;
	public Set<File> classpath_directories = new LinkedHashSet<File>();
	public Set<File> bootclasspath_directories = new LinkedHashSet<File>();
	public File currFile;
	public File bootFile;
	public JavaFileManager.Location source_path;
	public JavaFileManager.Location source_output;
	public JavaFileManager.Location class_output;
	public JavaFileManager.Location classpath;
	public String output_classpath;
	public JavaFileManager.Location bootclasspath;
	public String bootClassPath;
	public boolean outputToFS = false;
	public boolean assertions = false;
	public boolean generate_debugging_info = false;

	public boolean compile_command_line_only = false;

	public String[] source_ext = null; // e.g., java, jl, pj
	public String output_ext = "java"; // java, by default
	public boolean output_stdout = false; // whether to output to stdout
	// compiler to run on java output file
	// NOTE: null value of post_compiler sets JavaCompiler provided by
	// ToolProvider as the default post compiler
	public String post_compiler;

	public int output_width = 80;
	public boolean fully_qualified_names = false;

	/** Inject type information in serialized form into output file? */
	public boolean serialize_type_info = true;

	/** Dump the AST after the following passes? */
	public Set<String> dump_ast = new HashSet<String>();

	/** Pretty-print the AST after the following passes? */
	public Set<String> print_ast = new HashSet<String>();

	/** Disable the following passes? */
	public Set<String> disable_passes = new HashSet<String>();

	/** keep output files */
	public boolean keep_output_files = true;

	/** Generate position information for compiler-generated code. */
	public boolean precise_compiler_generated_positions = false;

	/** Use SimpleCodeWriter instead of OptimalCodeWriter */
	public boolean use_simple_code_writer = false;

	/**
	 * Parse "a" + "b" as "ab" to avoid very deep AST, e.g., for action tables,
	 * and for serialization.
	 */
	public boolean merge_strings = false;

	public boolean classpath_given = false;
	public boolean bootclasspath_given = false;

	/**
	 * Constructor
	 */
	public Options(ExtensionInfo extension) {
		this.extension = extension;
		setDefaultValues();
	}

	/**
	 * Set default values for options
	 */
	public void setDefaultValues() {
		currFile = new File(System.getProperty("user.dir"));
		// TODO : make external config property file.
		if (System.getProperty("os.name").indexOf("Mac") != -1) {
			// XXX: gross!
			bootFile = new File(System.getProperty("java.home") + separator
					+ ".." + separator + "Classes" + separator + "classes.jar");
		} else {
			bootFile = new File(System.getProperty("java.home") + separator
					+ "lib" + separator + "rt.jar");
		}
		source_path = StandardLocation.SOURCE_PATH;
		source_output = StandardLocation.SOURCE_OUTPUT;
		class_output = StandardLocation.CLASS_OUTPUT;
		classpath = StandardLocation.CLASS_PATH;
		String systemJavaClasspath = System.getProperty("java.class.path");
		output_classpath = systemJavaClasspath;
		bootclasspath = StandardLocation.PLATFORM_CLASS_PATH;

		sourcepath_directories.add(currFile);

		StringTokenizer st = new StringTokenizer(systemJavaClasspath,
				pathSeparator);
		while (st.hasMoreTokens()) {
			File f = new File(st.nextToken());
			if (f.exists())
				classpath_directories.add(f);
		}
	}

	/**
	 * Parse the command line
	 * 
	 * @throws UsageError
	 *             if the usage is incorrect.
	 */
	public void parseCommandLine(String args[], Set source) throws UsageError {
		if (args.length < 1) {
			throw new UsageError("No command line arguments given");
		}

		for (int i = 0; i < args.length;) {
			try {
				int ni = parseCommand(args, i, source);
				if (ni == i) {
					throw new UsageError("illegal option -- " + args[i]);
				}

				i = ni;

			} catch (ArrayIndexOutOfBoundsException e) {
				throw new UsageError("missing argument");
			}
		}

		if (source.size() < 1) {
			throw new UsageError("must specify at least one source file");
		}
	}

	/**
	 * Parse a command
	 * 
	 * @return the next index to process. i.e., if calling this method processes
	 *         two commands, then the return value should be index+2
	 */
	protected int parseCommand(String args[], int index, Set source)
			throws UsageError, Main.TerminationException {
		int i = index;
		if (args[i].equals("-h") || args[i].equals("-help")
				|| args[i].equals("--help")) {
			throw new UsageError("", 0);
		} else if (args[i].equals("-version")) {
			StringBuffer sb = new StringBuffer();
			if (extension != null) {
				sb.append(extension.compilerName() + " version "
						+ extension.version() + "\n");
			}
			sb.append("Polyglot compiler toolkit version "
					+ new polyglot.frontend.JLVersion());
			throw new Main.TerminationException(sb.toString(), 0);
		} else if (args[i].equals("-d")) {
			i++;
			File f = new File(args[i]);
			if (!f.exists())
				f.mkdirs();
			class_output_dir = Collections.singleton(f);
			class_output_directory = args[i];
			i++;
		} else if (args[i].equals("-D")) {
			i++;
			source_output_dir = Collections.singleton(new File(args[i]));
			source_output_directory = args[i];
			outputToFS = true;
			i++;
		} else if (args[i].equals("-classpath") || args[i].equals("-cp")) {
			i++;
			output_classpath = args[i] + pathSeparator + output_classpath;
			StringTokenizer st = new StringTokenizer(args[i], pathSeparator);
			while (st.hasMoreTokens()) {
				File f = new File(st.nextToken());
				if (f.exists())
					classpath_directories.add(f);
			}
			classpath_given = true;
			i++;
		} else if (args[i].equals("-bootclasspath")) {
			i++;
			bootClassPath = args[i];
			StringTokenizer st = new StringTokenizer(args[i], pathSeparator);
			while (st.hasMoreTokens()) {
				File f = new File(st.nextToken());
				if (f.exists())
					bootclasspath_directories.add(f);
			}
			bootclasspath_given = true;
			i++;
		} else if (args[i].equals("-sourcepath")) {
			i++;
			StringTokenizer st = new StringTokenizer(args[i], pathSeparator);
			while (st.hasMoreTokens()) {
				File f = new File(st.nextToken());
				if (f.exists())
					sourcepath_directories.add(f);
			}
			i++;
		} else if (args[i].equals("-commandlineonly")) {
			i++;
			compile_command_line_only = true;
		} else if (args[i].equals("-preferclassfiles")) {
			i++;
			ignore_mod_times = true;
		} else if (args[i].equals("-assert")) {
			i++;
			assertions = true;
		} else if (args[i].equals("-fqcn")) {
			i++;
			fully_qualified_names = true;
		} else if (args[i].equals("-g")) {
			i++;
			generate_debugging_info = true;
		} else if (args[i].equals("-c")) {
			post_compiler = null;
			i++;
		} else if (args[i].equals("-errors")) {
			i++;
			try {
				error_count = Integer.parseInt(args[i]);
			} catch (NumberFormatException e) {
			}
			i++;
		} else if (args[i].equals("-w")) {
			i++;
			try {
				output_width = Integer.parseInt(args[i]);
			} catch (NumberFormatException e) {
			}
			i++;
		} else if (args[i].equals("-post")) {
			i++;
			post_compiler = args[i];
			outputToFS = true;
			i++;
		} else if (args[i].equals("-stdout")) {
			i++;
			output_stdout = true;
		} else if (args[i].equals("-sx")) {
			i++;
			if (source_ext == null) {
				source_ext = new String[] { args[i] };
			} else {
				String[] s = new String[source_ext.length + 1];
				System.arraycopy(source_ext, 0, s, 0, source_ext.length);
				s[s.length - 1] = args[i];
				source_ext = s;
			}
			i++;
		} else if (args[i].equals("-ox")) {
			i++;
			output_ext = args[i];
			i++;
		} else if (args[i].equals("-noserial")) {
			i++;
			serialize_type_info = false;
		} else if (args[i].equals("-dump")) {
			i++;
			String pass_name = args[i];
			dump_ast.add(pass_name);
			i++;
		} else if (args[i].equals("-print")) {
			i++;
			String pass_name = args[i];
			print_ast.add(pass_name);
			i++;
		} else if (args[i].equals("-disable")) {
			i++;
			String pass_name = args[i];
			disable_passes.add(pass_name);
			i++;
		} else if (args[i].equals("-nooutput")) {
			i++;
			keep_output_files = false;
			output_width = 1000; // we do not keep the output files, so
									// set the output_width to a large number
									// to reduce the time spent pretty-printing
		} else if (args[i].equals("-v") || args[i].equals("-verbose")) {
			i++;
			Report.addTopic("verbose", 1);
		} else if (args[i].equals("-report")) {
			i++;
			String report_option = args[i];
			StringTokenizer st = new StringTokenizer(args[i], "=");
			String topic = "";
			int level = 0;
			if (st.hasMoreTokens())
				topic = st.nextToken();
			if (st.hasMoreTokens()) {
				try {
					level = Integer.parseInt(st.nextToken());
				} catch (NumberFormatException e) {
				}
			}
			Report.addTopic(topic, level);
			i++;
		} else if (args[i].equals("-debugpositions")) {
			precise_compiler_generated_positions = true;
			i++;
		} else if (args[i].equals("-simpleoutput")) {
			use_simple_code_writer = true;
			i++;
		} else if (args[i].equals("-mergestrings")) {
			merge_strings = true;
			i++;
		} else if (args[i].equals("-output-to-fs")) {
			outputToFS = true;
			i++;
		} else if (!args[i].startsWith("-")) {
			source.add(args[i]);
			File f = new File(args[i]).getAbsoluteFile().getParentFile();
			if (f != null && !sourcepath_directories.contains(f)) {
				sourcepath_directories.add(f);
				if (!classpath_given)
					classpath_directories.add(f);
			}
			i++;
		}

		return i;
	}

	public boolean isSourceOutputGiven() {
		return source_output_dir != null && source_output_dir.size() != 0;
	}

	public boolean isClassOutputGiven() {
		return class_output_dir != null && class_output_dir.size() != 0;
	}

	public Location outputDirectory() {
		return source_output;
	}

	public Location classOutputDirectory() {
		return class_output;
	}

	/**
	 * Print usage information
	 */
	public void usage(PrintStream out) {
		out.println("usage: " + extension.compilerName() + " [options] "
				+ "<source-file>." + extension.fileExtensions()[0] + " ...");
		out.println("where [options] includes:");
		usageForFlag(out, "@<file>", "read options from <file>");
		usageForFlag(out, "-g", "generate debugging info in class files");
		usageForFlag(out, "-d <directory>", "output directory");
		usageForFlag(out, "-assert", "recognize the assert keyword");
		usageForFlag(out, "-sourcepath <path>", "source path");
		usageForFlag(out, "-bootclasspath <path>",
				"path for bootstrap class files");
		usageForFlag(out, "-ext <extension>", "use language extension");
		usageForFlag(out, "-extclass <ext-class>", "use language extension");
		usageForFlag(out, "-commandlineonly",
				"only compile files named on the command-line (may also require -c)");
		usageForFlag(out, "-preferclassfiles",
				"prefer class files to source files even if the source is newer");
		usageForFlag(out, "-fqcn", "use fully-qualified class names");
		usageForFlag(out, "-sx <ext>", "set source extension");
		usageForFlag(out, "-ox <ext>", "set output extension");
		usageForFlag(out, "-errors <num>", "set the maximum number of errors");
		usageForFlag(out, "-w <num>",
				"set the maximum width of the .java output files");
		usageForFlag(out, "-dump <pass>", "dump the ast after pass <pass>");
		usageForFlag(out, "-print <pass>",
				"pretty-print the ast after pass <pass>");
		usageForFlag(out, "-disable <pass>", "disable pass <pass>");
		// usageForFlag(out, "-scramble [seed]",
		// "scramble the ast (for testing)");
		usageForFlag(out, "-noserial", "disable class serialization");
		usageForFlag(out, "-D <directory>", "output directory for .java files");
		usageForFlag(out, "-nooutput", "delete output files after compilation");
		usageForFlag(out, "-c", "compile only to .java");
		usageForFlag(out, "-post <compiler>",
				"run javac-like compiler after translation");
		usageForFlag(out, "-debugpositions",
				"generate position information for compiler-generated code");
		usageForFlag(out, "-simpleoutput", "use SimpleCodeWriter");
		usageForFlag(out, "-mergestrings",
				"parse concatenated string literals as one single string literal");
		usageForFlag(out, "-v -verbose", "print verbose debugging information");
		usageForFlag(out, "-report <topic>=<level>",
				"print verbose debugging information about "
						+ "topic at specified verbosity");

		StringBuffer allowedTopics = new StringBuffer("Allowed topics: ");
		for (Iterator iter = Report.topics.iterator(); iter.hasNext();) {
			allowedTopics.append(iter.next().toString());
			if (iter.hasNext()) {
				allowedTopics.append(", ");
			}
		}
		usageSubsection(out, allowedTopics.toString());

		usageForFlag(out, "-version", "print version info");
		usageForFlag(out, "-h", "print this message");
	}

	/**
	 * The maximum width of a line when printing usage information. Used by
	 * <code>usageForFlag</code> and <code>usageSubsection</code>.
	 */
	protected int USAGE_SCREEN_WIDTH = 76;
	/**
	 * The number of spaces from the left that the descriptions for flags will
	 * be displayed. Used by <code>usageForFlag</code>.
	 */
	protected int USAGE_FLAG_WIDTH = 27;
	/**
	 * The number of spaces to indent a subsection of usage information. Used by
	 * <code>usageSubsection</code>.
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
		} else {
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
				} else {
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
	 * field <code>USAGE_SUBSECTION_INDENT</code>
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
				} else {
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

	public String constructPostCompilerClasspath() {
		StringBuilder builder = new StringBuilder();
		for (File f : source_output_dir) {
			builder.append(f.getAbsolutePath());
			builder.append(pathSeparatorChar);
		}
		builder.append('.');
		builder.append(pathSeparatorChar);
		builder.append(output_classpath);
		if (bootClassPath != null)
			builder.append(bootClassPath);
		return builder.toString();
	}
}
