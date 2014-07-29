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

import java.io.Reader;

import polyglot.ast.JLang_c;
import polyglot.ast.NodeFactory;
import polyglot.ast.NodeFactory_c;
import polyglot.frontend.goals.Goal;
import polyglot.main.Version;
import polyglot.types.LoadedClassResolver;
import polyglot.types.MemberClassResolver;
import polyglot.types.SemanticException;
import polyglot.types.SourceClassResolver;
import polyglot.types.TopLevelResolver;
import polyglot.types.TypeSystem;
import polyglot.types.TypeSystem_c;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;

/**
 * This class implements most of the {@code ExtensionInfo} for the Java
 * language. It does not include a parser, however. EPL-licensed extensions
 * should extend this class rather than JLExtensionInfo since they should not
 * use the CUP-based grammar.
 *
 * @author nystrom
 *
 */
public abstract class ParserlessJLExtensionInfo extends AbstractExtensionInfo {

    /**
     * The LoadedClassResolver to use when initializing the type system.
     *
     * @see polyglot.frontend.ParserlessJLExtensionInfo#initTypeSystem()
     */
    protected LoadedClassResolver makeLoadedClassResolver() {
        return new SourceClassResolver(compiler,
                                       this,
                                       true,
                                       getOptions().compile_command_line_only,
                                       getOptions().ignore_mod_times);
    }

    @Override
    protected void initTypeSystem() {
        try {
            LoadedClassResolver lr = makeLoadedClassResolver();

            TopLevelResolver r = lr;

            // Resolver to handle lookups of member classes.
            if (TypeSystem.SERIALIZE_MEMBERS_WITH_CONTAINER) {
                MemberClassResolver mcr = new MemberClassResolver(ts, lr, true);
                r = mcr;
            }

            ts.initialize(r, this);
        }
        catch (SemanticException e) {
            throw new InternalCompilerError("Unable to initialize type system: "
                                                    + e.getMessage(),
                                            e);
        }
    }

    @Override
    protected polyglot.frontend.Scheduler createScheduler() {
        return new JLScheduler(this);
    }

    @Override
    public String defaultFileExtension() {
        return "jl";
    }

    @Override
    public String compilerName() {
        return "jlc";
    }

    @Override
    public Version version() {
        return new JLVersion();
    }

    /** Create the type system for this extension. */
    @Override
    protected TypeSystem createTypeSystem() {
        return new TypeSystem_c();
    }

    /** Create the node factory for this extension. */
    @Override
    protected NodeFactory createNodeFactory() {
        return new NodeFactory_c(JLang_c.instance);
    }

    @Override
    public JobExt jobExt() {
        return null;
    }

    /**
     * Return a parser for {@code source} using the given
     * {@code reader}.
     */
    @Override
    public abstract Parser parser(Reader reader, Source source, ErrorQueue eq);

    /**
     * Return the {@code Goal} to compile the source file associated with
     * {@code job} to completion.
     */
    @Override
    public Goal getCompileGoal(Job job) {
        return scheduler.CodeGenerated(job);
    }

    @Override
    public Goal getValidationGoal(Job job) {
        return scheduler.Validated(job);
    }

    static {
        // Force Topics to load.
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

}
