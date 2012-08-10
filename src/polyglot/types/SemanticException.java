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

import polyglot.main.Report;
import polyglot.util.Position;

/**
 * Thrown during any number of phases of the compiler during which a semantic
 * error may be detected.
 */
public class SemanticException extends Exception {
    protected Position position;

    public SemanticException() {
        super();
    }

    public SemanticException(Throwable cause) {
        super(cause);
    }

    public SemanticException(Position position, Throwable cause) {
        super(cause);
        this.position = position;
    }

    public SemanticException(Position position) {
        super();
        this.position = position;
    }

    public SemanticException(String m) {
        super(m);
    }

    public SemanticException(String m, Throwable cause) {
        super(m, cause);
    }

    public SemanticException(String m, Position position) {
        super(m);
        this.position = position;
    }

    public Position position() {
        return position;
    }

    private static boolean init = false;
    public static boolean fillInStackTrace = true;

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (!fillInStackTrace) {
            // fast path: init==true, fillInStackTrace==false
            return this;
        }
        if (!init) {
            fillInStackTrace = Report.should_report("trace", 1);
            init = true;
            if (!fillInStackTrace) {
                return this;
            }
        }
        return super.fillInStackTrace();
    }
}
