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

package polyglot.ast;

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

/** 
 * An {@code IntLit} represents a literal in Java of an integer
 * type.
 */
public interface IntLit extends NumLit {
    /** Integer literal kinds: int (e.g., 0) or long (e.g., 0L). */
    public static class Kind extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind INT = new Kind("int");
    public static final Kind LONG = new Kind("long");

    /** Get the literal's value. */
    long value();

    /** Set the literal's value. */
    IntLit value(long value);

    /** Get the kind of the literal: INT or LONG. */
    Kind kind();

    /** Set the kind of the literal: INT or LONG. */
    IntLit kind(Kind kind);

    /**
     * Is this a boundary case, i.e., max int or max long + 1?
     * 
     * @return True if this is a boundary case: the literal can only appear
     * as the operand of a unary minus.
     */
    boolean boundary();

    /** Print the string as a positive number. */
    String positiveToString();
}
