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

package polyglot.frontend;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.filemanager.FileManager;
import polyglot.frontend.Source.Kind;
import polyglot.frontend.goals.Goal;
import polyglot.main.Options;
import polyglot.translate.ext.ToExt;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLoader;
import polyglot.util.ErrorQueue;

/**
 * {@code ExtensionInfo} is the main interface for defining language
 * extensions. The frontend will load the {@code ExtensionInfo} specified
 * on the command-line. It defines the type system, AST node factory, parser,
 * and other parameters of a language extension.
 */
public interface ExtensionInfo {
    /** The name of the compiler for usage messages */
    String compilerName();

    /** Report the version of the extension. */
    polyglot.main.Version version();

    /** Returns the pass scheduler. */
    Scheduler scheduler();

    /**
     * Return the goal for compiling a particular compilation unit. The goal may
     * have subgoals on which it depends.
     */
    Goal getCompileGoal(Job job);

    /**
     * @return the goal for validating a particular compilation unit.
     */
    Goal getValidationGoal(Job job);

    /**
     * Return an Options object, which will be given the command line to parse.
     */
    Options getOptions();

    /**
     * Return a Stats object to accumulate and report statistics.
     */
    Stats getStats();

    /**
     * Initialize the extension with a particular compiler. This must be called
     * after the compiler is initialized, but before the compiler starts work.
     */
    void initCompiler(polyglot.frontend.Compiler compiler);

    Compiler compiler();

    /**
     * Get the file name extension of source files. This is either the language
     * extension's default file name extension or the string passed in with the
     * "-sx" command-line option.
     */
    String[] fileExtensions();

    /**
     * The default extensions that source files are expected to have. Defaults
     * to an array containing defaultFileExtension
     */
    String[] defaultFileExtensions();

    /** The default extension that source files are expected to have. */
    String defaultFileExtension();

    /** Produce a type system for this language extension. */
    TypeSystem typeSystem();

    /** Produce a node factory for this language extension. */
    NodeFactory nodeFactory();

    /** Get the source file loader for this extension. */
    SourceLoader sourceLoader();

    /**
     * Get the job extension for this language extension. The job extension is
     * used to extend the {@code Job} class without subtyping.
     */
    JobExt jobExt();

    /**
     * Produce a target factory for this language extension. The target factory
     * is responsible for naming and opening output files given a package name
     * and a class or source file name.
     */
    TargetFactory targetFactory();

    /**
     * Gets a set of keywords for this language extension.
     */
    Set<String> keywords();

    /** Get a parser for this language extension. */
    Parser parser(Reader reader, Source source, ErrorQueue eq);

    /**
     * Get the ToExt extension object used for translating AST nodes to the
     * to_ext language.
     */
    ToExt getToExt(ExtensionInfo to_ext, Node n);

    /** Get the extension file mananger used by this extension. */
    FileManager extFileManager();

    /** Configure the filemanger for the post-compilation phase. */
    void configureFileManagerForPostCompiler() throws IOException;

    /** Create class file for a file object. */
    ClassFile createClassFile(FileObject f, byte[] code) throws IOException;

    /**
     * Create file source for a file object.
     * @deprecated Use {@link #createFileSource(FileObject, Kind)} instead.
     */
    @Deprecated
    FileSource createFileSource(FileObject fo, boolean userSpecified)
            throws IOException;

    /** Create file source for a file object. */
    FileSource createFileSource(FileObject fo, Kind kind) throws IOException;

    /** Produce a class factory for this language extension. */
    ClassFileLoader classFileLoader();

    /**
     * Produce an extension info object for the output language this extension
     * translates to.
     */
    ExtensionInfo outputExtensionInfo();

    /**
     * Delete cached types and source files to reduce memory footprint.
     */
    void cleanup();

    /**
     * The set of locations to search for packages and class files when
     * otherwise unspecified.  This provides some backward compatibility
     * for code written prior to the FileManager API
     */
    List<Location> defaultLocations();

}
