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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.filemanager.ExtFileManager;
import polyglot.filemanager.FileManager;
import polyglot.frontend.Source.Kind;
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
 * This is an abstract {@code ExtensionInfo}.
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
    protected List<Location> default_locations;

    @Override
    public abstract Goal getCompileGoal(Job job);

    @Override
    public abstract String compilerName();

    @Override
    public abstract String defaultFileExtension();

    @Override
    public abstract Version version();

    @Override
    public Options getOptions() {
        if (options == null) {
            options = createOptions();
        }
        return options;
    }

    protected Options createOptions() {
        return new Options(this);
    }

    /** Return a Stats object to accumulate and report statistics. */
    @Override
    public Stats getStats() {
        if (stats == null) {
            stats = new Stats(this);
        }
        return stats;
    }

    @Override
    public Compiler compiler() {
        return compiler;
    }

    @Override
    public void initCompiler(Compiler compiler) {
        this.compiler = compiler;

        // Register the extension with the compiler.
        compiler.addExtension(this);

        // Initialize the output extension if there is one
        if (outputExtensionInfo() != null)
            outputExtensionInfo().initCompiler(compiler);

        // Create the type system and node factory.
        typeSystem();
        nodeFactory();
        scheduler();

        initTypeSystem();
    }

    /** Initialize the type system of this extension. */
    protected abstract void initTypeSystem();

    @Override
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

    @Override
    public String[] defaultFileExtensions() {
        String ext = defaultFileExtension();
        return new String[] { ext };
    }

    @Override
    public SourceLoader sourceLoader() {
        return extFileManager();
    }

    @Override
    public TargetFactory targetFactory() {
        if (target_factory == null) {
            target_factory =
                    new TargetFactory(extFileManager(),
                                      getOptions().source_output,
                                      getOptions().output_ext,
                                      getOptions().output_stdout);
        }

        return target_factory;
    }

    /** Create the scheduler for this extension. */
    protected abstract Scheduler createScheduler();

    @Override
    public Scheduler scheduler() {
        if (scheduler == null) {
            scheduler = createScheduler();
        }
        return scheduler;
    }

    /** Create the type system for this extension. */
    protected abstract TypeSystem createTypeSystem();

    @Override
    public TypeSystem typeSystem() {
        if (ts == null) {
            ts = createTypeSystem();
        }
        return ts;
    }

    /** Create the node factory for this extension. */
    protected abstract NodeFactory createNodeFactory();

    @Override
    public NodeFactory nodeFactory() {
        if (nf == null) {
            nf = createNodeFactory();
        }
        return nf;
    }

    @Override
    public JobExt jobExt() {
        return null;
    }

    @Override
    public abstract Parser parser(Reader reader, Source source, ErrorQueue eq);

    @Override
    public String toString() {
        return getClass().getName();
    }

    @Override
    public ClassFile createClassFile(FileObject f, byte[] code)
            throws IOException {
        return new ClassFile_c(f, code, this);
    }

    @Deprecated
    @Override
    public FileSource createFileSource(FileObject f, boolean user)
            throws IOException {
        return createFileSource(f, user ? Kind.USER_SPECIFIED : Kind.DEPENDENCY);
    }

    @Override
    public FileSource createFileSource(FileObject fo, Kind kind)
            throws IOException {
        return new Source_c(fo, kind);
    }

    @Override
    public FileManager extFileManager() {
        if (extFM == null) {
            extFM = createFileManager();
            try {
                configureFileManager();
            }
            catch (IOException e) {
                throw new InternalCompilerError(e);
            }
        }
        return extFM;
    }

    protected FileManager createFileManager() {
        return new ExtFileManager(this);
    }

    protected void configureFileManager() throws IOException {
        Options opt = getOptions();
        extFM.setLocation(opt.source_path, opt.sourcepath_directories);
        extFM.setLocation(opt.source_output,
                          Collections.singleton(opt.sourceOutputDirectory()));
        extFM.setLocation(opt.class_output,
                          Collections.singleton(opt.classOutputDirectory()));
        extFM.setLocation(opt.bootclasspath, opt.bootclasspathDirectories());
        extFM.setLocation(opt.classpath, opt.classpathDirectories());
    }

    /**
     * Configure the file manager for the post-compiler. This implementation
     * constructs a classpath from the source output directory, the current
     * directory, the classpath, and the bootclasspath.
     *
     * @return
     */
    @Override
    public void configureFileManagerForPostCompiler() throws IOException {
        Options opt = getOptions();

        extFM.setLocation(StandardLocation.PLATFORM_CLASS_PATH,
                          opt.defaultPlatformClasspath());

        List<File> sourcepath =
                Collections.singletonList(opt.sourceOutputDirectory());
        extFM.setLocation(StandardLocation.SOURCE_PATH, sourcepath);

        List<File> classpath = new ArrayList<>();
        classpath.addAll(opt.bootclasspathDirectories());
        classpath.addAll(opt.classpathDirectories());
        extFM.setLocation(StandardLocation.CLASS_PATH, classpath);

        List<File> classout =
                Collections.singletonList(opt.classOutputDirectory());
        extFM.setLocation(StandardLocation.CLASS_OUTPUT, classout);
    }

    @Override
    public ClassFileLoader classFileLoader() {
        if (classFileLoader == null) {
            classFileLoader = extFileManager();
        }
        return classFileLoader;
    }

    @Override
    public ToExt getToExt(ExtensionInfo to_ext, Node n) {
        // just return the first ToExt extension we find.
        return ToExt_c.ext(n);
    }

    @Override
    public ExtensionInfo outputExtensionInfo() {
        return null;
    }

    @Override
    public void cleanup() {
        // Flush the outputFiles collection
        compiler().outputFiles().clear();
        ts = null;
    }

    @Override
    public List<Location> defaultLocations() {
        if (default_locations == null) {
            default_locations = new ArrayList<>();
            default_locations.add(getOptions().bootclasspath);
            default_locations.add(getOptions().classpath);
        }
        return default_locations;
    }

}
