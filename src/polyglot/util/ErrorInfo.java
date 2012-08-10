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

package polyglot.util;

/** Information about an error message. */
public class ErrorInfo {
    public static final int WARNING = 0;
    public static final int INTERNAL_ERROR = 1;
    public static final int IO_ERROR = 2;
    public static final int LEXICAL_ERROR = 3;
    public static final int SYNTAX_ERROR = 4;
    public static final int SEMANTIC_ERROR = 5;
    public static final int POST_COMPILER_ERROR = 6;
    public static final int DEBUG = 7;

    protected static String[] errorStrings = { "Warning", "Internal Error",
            "I/O Error", "Lexical Error", "Syntax Error", "Semantic Error",
            "Post-compiler Error", "Debug" };

    protected int kind;
    protected String message;
    protected Position position;

    public ErrorInfo(int kind, String message, Position position) {
        this.kind = kind;
        this.message = message;
        this.position = position;
    }

    public int getErrorKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }

    public Position getPosition() {
        return position;
    }

    public String getErrorString() {
        return getErrorString(kind);
    }

    public static String getErrorString(int kind) {
        if (0 <= kind && kind < errorStrings.length) {
            return errorStrings[kind];
        }
        return "(Unknown)";
    }
}
