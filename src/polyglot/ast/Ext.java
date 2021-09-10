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

import java.io.Serializable;

import polyglot.util.CodeWriter;
import polyglot.util.Copy;

/**
 * {@code Ext} is the super type of all node extension objects.
 * It contains a pointer back to the node it is extending and a possibly-null
 * pointer to another extension node.
 */
public interface Ext extends NodeOps, Copy<Ext>, Serializable {
    /** The node we ultimately extend. */
    Node node();

    /** The parent of this extension. */
    NodeOps pred();

    /**
     * Initialize the extension object's pointer back to the node.
     * This also initializes the back pointers for all extensions of
     * the extension.
     */
    void init(Node node);

    /**
     * Initialize the extension object's pointer back to the parent.
     * This also initializes the back pointers for all extensions of
     * the extension.
     */
    void initPred(NodeOps pred);

    /** An extension of this extension, or null. */
    Ext ext();

    /** Set the extension of this extension. */
    Ext ext(Ext ext);

    /**
     * Dump the AST node for debugging purposes.
     */
    void dump(CodeWriter w);
}
