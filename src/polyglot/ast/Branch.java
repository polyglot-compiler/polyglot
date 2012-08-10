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

package polyglot.ast;

import polyglot.util.Enum;

/**
 * A <code>Branch</code> is an immutable representation of a branch
 * statment in Java (a break or continue).
 */
public interface Branch extends Stmt {
    /** Branch kind: either break or continue. */
    public static class Kind extends Enum {
        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind BREAK = new Kind("break");
    public static final Kind CONTINUE = new Kind("continue");

    /**
     * The kind of branch.
     */
    Kind kind();

    /**
     * Set the kind of branch.
     */
    Branch kind(Kind kind);

    /**
     * Target label of the branch.
     */
    Id labelNode();

    /**
     * Set the target label of the branch.
     */
    Branch labelNode(Id label);

    /**
     * Target label of the branch.
     */
    String label();

    /**
     * Set the target label of the branch.
     */
    Branch label(String label);
}
