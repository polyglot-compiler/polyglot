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

import java.io.IOException;

import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;

/**
 * A parser implemented with a Cup generated-parser.
 */
public class CupParser implements Parser {
    protected java_cup.runtime.lr_parser grm;
    protected Source source;
    protected ErrorQueue eq;

    public CupParser(java_cup.runtime.lr_parser grm, Source source,
            ErrorQueue eq) {
        this.grm = grm;
        this.source = source;
        this.eq = eq;
    }

    @Override
    public Node parse() {
        try {
            java_cup.runtime.Symbol sym = grm.parse();

            if (sym != null && sym.value instanceof Node) {
                SourceFile sf = (SourceFile) sym.value;
                return sf.source(source);
            }
        }
        catch (IOException e) {
            eq.enqueue(ErrorInfo.IO_ERROR, e.getMessage());
        }
        catch (RuntimeException e) {
            // Let the Compiler catch and report it.
            throw e;
        }
        catch (Exception e) {
            // Used by cup to indicate a non-recoverable error.
            if (e.getMessage() != null) {
                eq.enqueue(ErrorInfo.SYNTAX_ERROR, e.getMessage());
            }
        }

        if (!eq.hasErrors()) {
            eq.enqueue(ErrorInfo.SYNTAX_ERROR,
                       "Unable to parse " + source.path() + ".");
        }

        return null;
    }

    @Override
    public String toString() {
        return "CupParser(" + grm.getClass().getName() + ")";
    }
}
