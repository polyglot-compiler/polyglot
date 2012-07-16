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

package polyglot.frontend;

import java.io.Reader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileManager.Location;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.filemanager.ExtFileManager;
import polyglot.filemanager.FileManager;
import polyglot.frontend.goals.Goal;
import polyglot.main.Options;
import polyglot.main.Version;
import polyglot.translate.ext.ToExt;
import polyglot.translate.ext.ToExt_c;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLoader;
import polyglot.types.reflect.ClassFile_c;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;

/**
 * This is an abstract <code>ExtensionInfo</code>.
 */
public abstract class AbstractExtensionInfo implements ExtensionInfo {
	protected Compiler compiler;
	private Options options;
	protected TypeSystem ts = null;
	protected NodeFactory nf = null;
	protected TargetFactory target_factory = null;
	protected Stats stats;
	protected Scheduler scheduler;
	protected FileManager extFM;
	protected ClassFileLoader classFileLoader;

	public abstract Goal getCompileGoal(Job job);

	public abstract String compilerName();

	public abstract String defaultFileExtension();

	public abstract Version version();

	public Options getOptions() {
		if (this.options == null) {
			this.options = createOptions();
		}
		return options;
	}

	protected Options createOptions() {
		return new Options(this);
	}

	/** Return a Stats object to accumulate and report statistics. */
	public Stats getStats() {
		if (this.stats == null) {
			this.stats = new Stats(this);
		}
		return stats;
	}

	public Compiler compiler() {
		return compiler;
	}

	public void initCompiler(Compiler compiler) {
		this.compiler = compiler;

		// Register the extension with the compiler.
		compiler.addExtension(this);
		
		// Initialize the output extension if there is one
        if (this.outputExtensionInfo() != null)
        	this.outputExtensionInfo().initCompiler(compiler);

		// Create the type system and node factory.
		typeSystem();
		nodeFactory();
		scheduler();

		initTypeSystem();
	}

	/** Initialize the type system of this extension. */
	protected abstract void initTypeSystem();

	public String[] fileExtensions() {
		String[] sx = getOptions() == null ? null : getOptions().source_ext;

		if (sx == null) {
			sx = defaultFileExtensions();
		}

		if (sx.length == 0) {
			return defaultFileExtensions();
		}

		return sx;
	}

	public String[] defaultFileExtensions() {
		String ext = defaultFileExtension();
		return new String[] { ext };
	}

	public SourceLoader sourceLoader() {
		return extFileManager();
	}

	public TargetFactory targetFactory() {
		if (target_factory == null) {
			target_factory = new TargetFactory(extFileManager(),
					getOptions().source_output, getOptions().output_ext,
					getOptions().output_stdout);
		}

		return target_factory;
	}

	/** Create the scheduler for this extension. */
	protected abstract Scheduler createScheduler();

	public Scheduler scheduler() {
		if (scheduler == null) {
			scheduler = createScheduler();
		}
		return scheduler;
	}

	/** Create the type system for this extension. */
	protected abstract TypeSystem createTypeSystem();

	public TypeSystem typeSystem() {
		if (ts == null) {
			ts = createTypeSystem();
		}
		return ts;
	}

	/** Create the node factory for this extension. */
	protected abstract NodeFactory createNodeFactory();

	public NodeFactory nodeFactory() {
		if (nf == null) {
			nf = createNodeFactory();
		}
		return nf;
	}

	public JobExt jobExt() {
		return null;
	}

	public abstract Parser parser(Reader reader, FileSource source,
			ErrorQueue eq);

	public String toString() {
		return getClass().getName();
	}

	public ClassFile createClassFile(FileObject f, byte[] code)
			throws IOException {
		return new ClassFile_c(f, code, this);
	}

	public FileSource createFileSource(FileObject f, boolean user)
			throws IOException {
		return new Source_c(f, user);
	}

	public FileManager extFileManager() {
		if (extFM == null)
			extFM = new ExtFileManager(this);
		return extFM;
	}

	public ClassFileLoader classFileLoader() {
		if (classFileLoader == null) {
			classFileLoader = extFileManager();
			classFileLoader.addLocation(getOptions().bootclasspath);
			classFileLoader.addLocation(getOptions().classpath);
		}
		return classFileLoader;
	}

	public void addLocationsToFileManager() {
		StandardJavaFileManager ext_fm = extFileManager();
		Options options = getOptions();
		try {
			ext_fm.setLocation(options.source_path,
					options.sourcepath_directories);
			if (!options.isSourceOutputGiven() && !options.isClassOutputGiven())
				options.source_output_dir = options.class_output_dir = Collections
						.singleton(options.current_directory);
			else if (!options.isSourceOutputGiven()
					&& options.isClassOutputGiven())
				options.source_output_dir = options.class_output_dir;
			else if (!options.isClassOutputGiven())
				options.class_output_dir = Collections
						.singleton(options.current_directory);
			ext_fm.setLocation(options.source_output, options.source_output_dir);
			ext_fm.setLocation(options.class_output, options.class_output_dir);
			if (!options.bootclasspath_given)
				options.bootclasspath_directories.addAll(options.default_bootclasspath_directories);
			ext_fm.setLocation(options.bootclasspath,
					options.bootclasspath_directories);
			ext_fm.setLocation(options.classpath, options.classpath_directories);
		} catch (IOException e) {
			throw new InternalCompilerError(e.getMessage());
		}
	}

	public ToExt getToExt(ExtensionInfo to_ext, Node n) {
		// just return the first ToExt extension we find.
		return ToExt_c.ext(n);
	}
	
	public ExtensionInfo outputExtensionInfo() {
		return null;
	}

}
