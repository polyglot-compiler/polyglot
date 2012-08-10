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

package polyglot.types;

import java.io.InvalidClassException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.SchedulerException;
import polyglot.main.Report;
import polyglot.main.Version;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLoader;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.ObjectDumper;
import polyglot.util.SimpleCodeWriter;
import polyglot.util.TypeEncoder;

/**
 * Loads class information from class files, or serialized class information
 * from within class files. It does not load from source files.
 */
public class LoadedClassResolver implements TopLevelResolver {
    protected final static int NOT_COMPATIBLE = -1;
    protected final static int MINOR_NOT_COMPATIBLE = 1;
    protected final static int COMPATIBLE = 0;

    protected TypeSystem ts;
    protected TypeEncoder te;

    protected Version version;
    protected Set<String> nocache;
    protected boolean allowRawClasses;
    protected ExtensionInfo extInfo;

    protected final static Collection<String> report_topics =
            CollectionUtil.list(Report.types, Report.resolver, Report.loader);
    protected ClassFileLoader loader;

    public LoadedClassResolver(ExtensionInfo extInfo, boolean allowRawClasses) {
        this.extInfo = extInfo;
        this.ts = extInfo.typeSystem();
        this.te = new TypeEncoder(extInfo.typeSystem());
        this.loader = extInfo.classFileLoader();
        this.version = extInfo.version();
        this.nocache = new HashSet<String>();
        this.allowRawClasses = allowRawClasses;
    }

    public boolean allowRawClasses() {
        return allowRawClasses;
    }

    @Override
    public boolean packageExists(String name) {
        return loader.packageExists(name);
    }

    /**
     * Load a class file for class <code>name</code>.
     */
    protected ClassFile loadFile(String name) {
        return loader.loadFile(name);
    }

    /**
     * Find a type by name.
     */
    @Override
    public Named find(String name) throws SemanticException {
        if (Report.should_report(report_topics, 3))
            Report.report(3, "LoadedCR.find(" + name + ")");

        Named result = null;

        // First try the class file.
        ClassFile clazz = loadFile(name);
        if (clazz == null) {
            throw new NoClassException(name);
        }

        // Check for encoded type information.
        if (clazz.encodedClassType(version.name()) != null) {
            if (Report.should_report(report_topics, 4))
                Report.report(4, "Using encoded class type for " + name);
            result = getEncodedType(clazz, name);
        }

        if (allowRawClasses) {
            if (Report.should_report(report_topics, 4))
                Report.report(4, "Using raw class file for " + name);
            result = ts.classFileLazyClassInitializer(clazz).type();
        }

        // Verify that the type we loaded has the right name. This prevents,
        // for example, requesting a type through its mangled (class file) name.
        if (result != null) {
            if (name.equals(result.fullName())) {
                return result;
            }
            if (result instanceof ClassType
                    && name.equals(ts.getTransformedClassName((ClassType) result))) {
                return result;
            }
        }

        // We have a raw class, but are not allowed to use it, and
        // cannot find appropriate encoded info.
        throw new SemanticException("Unable to find a suitable definition of \""
                + name
                + "\". A class file was found,"
                + " but it did not contain appropriate information for this"
                + " language extension. If the source for this file is written"
                + " in the language extension, try recompiling the source code.");

    }

    protected boolean recursive = false;

    /**
     * Extract an encoded type from a class file.
     */
    protected ClassType getEncodedType(ClassFile clazz, String name)
            throws SemanticException {
        // At this point we've decided to go with the Class. So if something
        // goes wrong here, we have only one choice, to throw an exception.

        // Check to see if it has serialized info. If so then check the
        // version.
        int comp = checkCompilerVersion(clazz.compilerVersion(version.name()));

        if (comp == NOT_COMPATIBLE) {
            throw new SemanticException("Unable to find a suitable definition of "
                    + clazz.name()
                    + ". Try recompiling or obtaining "
                    + " a newer version of the class file.");
        }

        // Alright, go with it!
        TypeObject dt;
        SystemResolver oldResolver = null;

        if (Report.should_report(Report.serialize, 1))
            Report.report(1, "Saving system resolver");
        oldResolver = ts.saveSystemResolver();

        boolean okay = false;

        boolean oldRecursive = recursive;

        if (!recursive) {
            ts.systemResolver().clearAdded();
        }

        recursive = true;

        try {
            try {
                if (Report.should_report(Report.serialize, 1))
                    Report.report(1, "Decoding " + name + " in " + clazz);

                dt = te.decode(clazz.encodedClassType(version.name()), name);

                if (dt == null) {
                    if (Report.should_report(Report.serialize, 1))
                        Report.report(1, "* Decoding " + name + " failed");

                    // Deserialization failed because one or more types could
                    // not
                    // be resolved. Abort this pass. Dependencies have already
                    // been set up so that this goal will be reattempted after
                    // the types are resolved.
                    throw new SchedulerException("Could not decode " + name);
                }
            }
            catch (InternalCompilerError e) {
                throw e;
            }
            catch (InvalidClassException e) {
                throw new BadSerializationException(clazz.name() + "@"
                        + clazz.getClassFileURI());
            }

            if (dt instanceof ClassType) {
                ClassType ct = (ClassType) dt;
                // Install the decoded type into the *new* system resolver.
                // It will be installed into the old resolver below by putAll.
                ts.systemResolver().addNamed(name, ct);

                if (Report.should_report(Report.serialize, 1))
                    Report.report(1, "* Decoding " + name + " succeeded");

                if (Report.should_report("typedump", 1)) {
                    new ObjectDumper(new SimpleCodeWriter(System.out, 72)).dump(dt);
                }

                if (Report.should_report(Report.serialize, 2)) {
                    LazyInitializer init = null;

                    // Save and restore the initializer to print the members.
                    // We can't access the members of ct until after we return
                    // from
                    // the resolver because the initializer may set up goals on
                    // ct,
                    // which may get discarded because of a missing dependency.
                    if (ct instanceof ParsedClassType) {
                        ParsedClassType pct = (ParsedClassType) ct;
                        init = pct.initializer();
                        pct.setInitializer(new LazyClassInitializer() {
                            @Override
                            public boolean fromClassFile() {
                                return false;
                            }

                            @Override
                            public void setClass(ParsedClassType ct) {
                            }

                            @Override
                            public void initTypeObject() {
                            }

                            @Override
                            public boolean isTypeObjectInitialized() {
                                return true;
                            }

                            @Override
                            public void initSuperclass() {
                            }

                            @Override
                            public void initInterfaces() {
                            }

                            @Override
                            public void initMemberClasses() {
                            }

                            @Override
                            public void initConstructors() {
                            }

                            @Override
                            public void initMethods() {
                            }

                            @Override
                            public void initFields() {
                            }

                            @Override
                            public void canonicalConstructors() {
                            }

                            @Override
                            public void canonicalMethods() {
                            }

                            @Override
                            public void canonicalFields() {
                            }
                        });
                    }

                    for (MethodInstance mi : ct.methods())
                        Report.report(2, "* " + mi);
                    for (FieldInstance fi : ct.fields())
                        Report.report(2, "* " + fi);
                    for (ConstructorInstance ci : ct.constructors())
                        Report.report(2, "* " + ci);

                    if (ct instanceof ParsedClassType) {
                        ParsedClassType pct = (ParsedClassType) ct;
                        pct.setInitializer(init);
                    }
                }

                if (Report.should_report(report_topics, 2))
                    Report.report(2, "Returning serialized ClassType for "
                            + clazz.name() + ".");

                okay = true;
                return ct;
            }
            else {
                throw new SemanticException("Class " + name + " not found in "
                        + clazz.name() + ".");
            }
        }
        finally {
            recursive = oldRecursive;

            if (okay) {
                if (Report.should_report(Report.serialize, 1))
                    Report.report(1, "Deserialization successful.  Installing "
                            + ts.systemResolver().justAdded()
                            + " into restored system resolver.");

                oldResolver.putAll(ts.systemResolver());
            }
            else {
                if (Report.should_report(Report.serialize, 1))
                    Report.report(1,
                                  "Deserialization failed.  Restoring previous system resolver.");
                if (Report.should_report(Report.serialize, 1))
                    Report.report(1, "Discarding "
                            + ts.systemResolver().justAdded());
            }

            ts.restoreSystemResolver(oldResolver);
        }
    }

    /**
     * Compare the encoded type's version against the loader's version.
     */
    protected int checkCompilerVersion(String clazzVersion) {
        if (clazzVersion == null) {
            return NOT_COMPATIBLE;
        }

        StringTokenizer st = new StringTokenizer(clazzVersion, ".");

        try {
            int v;
            v = Integer.parseInt(st.nextToken());
            Version version = this.version;

            if (v != version.major()) {
                // Incompatible.
                return NOT_COMPATIBLE;
            }

            v = Integer.parseInt(st.nextToken());

            if (v != version.minor()) {
                // Not the best option, but will work if its the only one.
                return MINOR_NOT_COMPATIBLE;
            }
        }
        catch (NumberFormatException e) {
            return NOT_COMPATIBLE;
        }

        // Everything is way cool.
        return COMPATIBLE;
    }
}
