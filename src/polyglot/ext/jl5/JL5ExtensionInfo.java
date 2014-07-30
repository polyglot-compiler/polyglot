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
package polyglot.ext.jl5;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import javax.tools.FileObject;

import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.J5Lang_c;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;
import polyglot.ext.jl5.ast.JL5NodeFactory_c;
import polyglot.ext.jl5.parse.Grm;
import polyglot.ext.jl5.parse.Lexer_c;
import polyglot.ext.jl5.types.JL5TypeSystem_c;
import polyglot.ext.jl5.types.reflect.JL5ClassFile;
import polyglot.frontend.CupParser;
import polyglot.frontend.JLExtensionInfo;
import polyglot.frontend.Parser;
import polyglot.frontend.Scheduler;
import polyglot.frontend.Source;
import polyglot.main.Options;
import polyglot.main.Version;
import polyglot.translate.JLOutputExtensionInfo;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyglot.util.ErrorQueue;

/**
 * Extension information for jl5 extension.
 */
public class JL5ExtensionInfo extends JLExtensionInfo {

    protected polyglot.frontend.ExtensionInfo outputExtensionInfo;

    @Override
    public String defaultFileExtension() {
        return "jl5";
    }

    @Override
    public String[] defaultFileExtensions() {
        String ext = defaultFileExtension();
        return new String[] { ext, "java" };
    }

    @Override
    public String compilerName() {
        return "jl5c";
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new JL5NodeFactory_c(J5Lang_c.instance, new JL5ExtFactory_c());
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new JL5TypeSystem_c();
    }

    @Override
    public Scheduler createScheduler() {
        return new JL5Scheduler(this);
    }

    @Override
    protected Options createOptions() {
        return new JL5Options(this);
    }

    @Override
    public ClassFile createClassFile(FileObject classFileSource, byte[] code)
            throws IOException {
        return new JL5ClassFile(classFileSource, code, this);
    }

    /**
     * Return a parser for {@code source} using the given
     * {@code reader}.
     */
    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        reader = new polyglot.lex.EscapedUnicodeReader(reader);

        polyglot.lex.Lexer lexer = new Lexer_c(reader, source, eq);
        polyglot.parse.BaseParser parser = new Grm(lexer, ts, nf, eq);

        return new CupParser(parser, source, eq);
    }

    @Override
    public Set<String> keywords() {
	return new Lexer_c(null).keywords();
    }

    @Override
    public polyglot.frontend.ExtensionInfo outputExtensionInfo() {
        if (outputExtensionInfo == null) {
            if (((JL5Options) getOptions()).leaveCovariantReturns) {
                outputExtensionInfo = new CovarRetOutputExtensionInfo(this);
            }
            else {
                outputExtensionInfo = new JLOutputExtensionInfo(this);
            }
        }
        return outputExtensionInfo;
    }

    @Override
    public Version version() {
        return new JL5Version();
    }

}
