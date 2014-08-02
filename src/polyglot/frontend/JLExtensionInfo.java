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
import java.util.Set;

import polyglot.lex.EscapedUnicodeReader;
import polyglot.parse.Grm;
import polyglot.parse.Lexer_c;
import polyglot.util.ErrorQueue;

/**
 * This is the default {@code ExtensionInfo} for the Java language.
 *
 * Compilation passes and visitors:
 * <ol>
 * <li> parse </li>
 * <li> build-types (TypeBuilder) </li>
 * <hr>
 * <center>BARRIER</center>
 * <hr>
 * <li> disambiguate (AmbiguityRemover) </li>
 * <hr>
 * <li> type checking (TypeChecker) </li>
 * <li> reachable checking (ReachChecker) </li>
 * <li> exception checking (ExceptionChecker)
 * <li> exit checking (ExitChecker)
 * <li> initialization checking (InitChecker)
 * <li> circular constructor call checking (ConstructorCallChecker)
 * <hr>
 * <center>PRE_OUTPUT MARKER</center>
 * <hr>
 * <li> serialization (ClassSerializer), optional </li>
 * <li> translation (Translator) </li>
 * </ol>
 */
public class JLExtensionInfo extends ParserlessJLExtensionInfo {

    /**
     * Return a parser for {@code source} using the given
     * {@code reader}.
     */
    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        reader = new EscapedUnicodeReader(reader);

        polyglot.lex.Lexer lexer = new Lexer_c(reader, source, eq);
        polyglot.parse.BaseParser parser = new Grm(lexer, ts, nf, eq);

        return new CupParser(parser, source, eq);
    }

    @Override
    public Set<String> keywords() {
        return new Lexer_c(null).keywords();
    }

}
