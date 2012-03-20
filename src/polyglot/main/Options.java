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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringTokenizer;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import polyglot.frontend.ExtensionInfo;
import polyglot.util.InternalCompilerError;

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
	protected ExtensionInfo extension = null;

	/*
	 * Fields for storing values for options.
	 */
	public int error_count = 100;
	protected Set<File> source_path_directories = new LinkedHashSet<File>();
	public Set<File> classpath_directories = new LinkedHashSet<File>();
	public JavaFileManager.Location source_path; // List<File>
	public JavaFileManager.Location source_output;
	public JavaFileManager.Location class_output;
	public JavaFileManager.Location classpath;
	public JavaFileManager.Location output_classpath;
	public JavaFileManager.Location bootclasspath;
	public boolean assertions = false;
	public boolean generate_debugging_info = false;

	public boolean compile_command_line_only = false;

	public String[] source_ext = null; // e.g., java, jl, pj
	public String output_ext = "java"; // java, by default
	public boolean output_stdout = false; // whether to output to stdout
	// compiler to run on java output file
	public JavaCompiler post_compiler;
	// arguments passed to post compiler
	public String post_compiler_args;

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

	protected boolean java_output_given = false;
	protected boolean classpath_given = false;
	
	private StandardJavaFileManager ext_fm;
	private StandardJavaFileManager java_fm;

	/**
	 * Constructor
	 */
	public Options(ExtensionInfo extension) {
		this.extension = extension;
		ext_fm = extension.extFileManager();
		java_fm = extension.javaFileManager();
		setDefaultValues();
	}

	/**
	 * Set default values for options
	 */
	public void setDefaultValues() {
		File currFile = new File(System.getProperty("user.dir"));
		File bootclassFile = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar");
		
		source_path = StandardLocation.SOURCE_PATH;
		source_output = StandardLocation.SOURCE_OUTPUT;
		class_output = StandardLocation.CLASS_OUTPUT;
		output_classpath = StandardLocation.CLASS_PATH;
		classpath = StandardLocation.CLASS_PATH;
		bootclasspath = StandardLocation.PLATFORM_CLASS_PATH;
		
		source_path_directories.add(currFile);
		
		Collection<File> s = Collections.singleton(currFile);
		Collection<File> b = Collections.singleton(bootclassFile);
		try {
			ext_fm.setLocation(source_path, source_path_directories);
			ext_fm.setLocation(source_output, s);
			java_fm.setLocation(source_path, s);
			ext_fm.setLocation(class_output, s);
			java_fm.setLocation(class_output, s);
			ext_fm.setLocation(bootclasspath, b);
			java_fm.setLocation(bootclasspath, b);
			ext_fm.setLocation(classpath, classpath_directories);
			java_fm.setLocation(classpath, classpath_directories);
		} catch (IOException e) {
			throw new InternalCompilerError(
					"Error setting directory for class output or bootclasspath or classpath");
		}

		post_compiler = ToolProvider.getSystemJavaCompiler();
		post_compiler_args = "";
	}

	/**
	 * Parse the command line
	 * 
	 * @throws UsageError
	 *             if the usage is incorrect.
	 */
	public void parseCommandLine(String args[], Set<String> source)
			throws UsageError {
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
	protected int parseCommand(String args[], int index, Set<String> source)
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
			Collection<File> od = Collections.singleton(new File(args[i]));
			try {
				ext_fm.setLocation(class_output, od);
				java_fm.setLocation(class_output, od);
				//if -D has not been specified, default -D to -d
				if (!java_output_given) {
					ext_fm.setLocation(source_output,od);
					java_fm.setLocation(source_path, od);
				}
			} catch (IOException e) {
				throw new UsageError(e.getMessage());
			}
			i++;
		} else if (args[i].equals("-D")) {
			i++;
			Collection<File> od = Collections.singleton(new File(args[i]));
			try {
				ext_fm.setLocation(source_output, od);
				java_fm.setLocation(source_path, od);
			} catch (IOException e) {
				throw new UsageError(e.getMessage());
			}
			java_output_given = true;
			i++;
		} else if (args[i].equals("-classpath") || args[i].equals("-cp")) {
			i++;
			StringTokenizer st = new StringTokenizer(args[i],
					File.pathSeparator);
			while (st.hasMoreTokens()) {
				File f = new File(st.nextToken());
				if(f.exists())
					classpath_directories.add(f);
			}
			classpath_given = true;
			i++;
		} else if (args[i].equals("-bootclasspath")) {
			i++;
			List<File> path = new ArrayList<File>();
			StringTokenizer st = new StringTokenizer(args[i],
					File.pathSeparator);
			while (st.hasMoreTokens()) {
				File f = new File(st.nextToken());
				if(f.exists())
					path.add(f);
			}
			try {
				ext_fm.setLocation(bootclasspath, path);
				java_fm.setLocation(bootclasspath, path);
			} catch (IOException e) {
				throw new UsageError(e.getMessage());
			}
			i++;
		} else if (args[i].equals("-sourcepath")) {
			i++;
			StringTokenizer st = new StringTokenizer(args[i],
					File.pathSeparator);
			while (st.hasMoreTokens()) {
				File f = new File(st.nextToken());
				if(f.exists())
					source_path_directories.add(f);
			}
			try {
				ext_fm.setLocation(source_path,
						source_path_directories);
			} catch (IOException e) {
				throw new UsageError(e.getMessage());
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
			ServiceLoader<JavaCompiler> loader = java.util.ServiceLoader.load(
					JavaCompiler.class, extension.classLoader());
			JavaCompiler javac = null;
			for (JavaCompiler c : loader) {
				if (c.getClass().getName().equals(args[i]))
					javac = c;
			}
			if (javac == null)
				throw new UsageError("Compiler " + args[i] + " not found.");
			post_compiler = javac;
			i++;
		} else if (args[i].equals("-post_args")) {
			i++;
			post_compiler_args = args[i];
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
		} else if (!args[i].startsWith("-")) {
			source.add(args[i]);
			File f = new File(args[i]).getAbsoluteFile().getParentFile();
			if (f != null && !source_path_directories.contains(f)) {
				source_path_directories.add(f);
				if(!classpath_given)
					classpath_directories.add(f);
			}
			try {
				ext_fm.setLocation(source_path,
						source_path_directories);
			} catch (IOException e) {
				throw new UsageError(e.getMessage());
			}
			i++;
		}

		return i;
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
		for (Iterator<String> iter = Report.topics.iterator(); iter.hasNext();) {
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

	public String constructFullClasspath() {
		StringBuffer fullcp = new StringBuffer();
		if (bootclasspath != null) {
			fullcp.append(bootclasspath);
		}
		fullcp.append(classpath);
		return fullcp.toString();
	}

	public String constructPostCompilerClasspath() {
		return source_output + File.pathSeparator + "." + File.pathSeparator
				+ output_classpath;
	}
}
