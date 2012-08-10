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

package polyglot.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import polyglot.ast.Node;

/**
 * A HaltingVisitor is used to prune the traversal of the AST at a
 * particular node.  Clients can call <code>bypass(Node n) </code> to 
 * have the visitor skip n and its children when recursing through the AST.
 */
public abstract class HaltingVisitor extends NodeVisitor {
    protected Node bypassParent;
    protected Collection<Node> bypass;

    /** Return a new visitor that will bypass all children of node n. */
    public HaltingVisitor bypassChildren(Node n) {
        HaltingVisitor v = (HaltingVisitor) copy();
        v.bypassParent = n;
        return v;
    }

    /** Return a new visitor that will visit all children. */
    public HaltingVisitor visitChildren() {
        HaltingVisitor v = (HaltingVisitor) copy();
        v.bypassParent = null;
        v.bypass = null;
        return v;
    }

    /** Return a new visitor that bypasses node n during visit children. */
    public HaltingVisitor bypass(Node n) {
        if (n == null) return this;

        HaltingVisitor v = (HaltingVisitor) copy();

        // FIXME: Using a collection is expensive, but is hopefully not
        // often used.
        if (this.bypass == null) {
            v.bypass = Collections.singleton(n);
        }
        else {
            v.bypass = new ArrayList<Node>(this.bypass.size() + 1);
            v.bypass.addAll(bypass);
            v.bypass.add(n);
        }

        return v;
    }

    /** Return a new visitor that will bypass all nodes in collection c. */
    public HaltingVisitor bypass(Collection<? extends Node> c) {
        if (c == null) return this;

        HaltingVisitor v = (HaltingVisitor) copy();

        // FIXME: Using a collection is expensive, but is hopefully not
        // often used.
        if (this.bypass == null) {
            v.bypass = new ArrayList<Node>(c);
        }
        else {
            v.bypass = new ArrayList<Node>(this.bypass.size() + c.size());
            v.bypass.addAll(bypass);
            v.bypass.addAll(c);
        }

        return v;
    }

    @Override
    public Node override(Node parent, Node n) {
        if (bypassParent != null && bypassParent == parent) {
            // System.out.println("bypassing " + n +
            //                    " (child of " + parent + ")");
            return n;
        }

        if (bypass != null) {
            for (Node n2 : bypass) {
                if (n2 == n) {
                    // System.out.println("bypassing " + n);
                    return n;
                }
            }
        }

        return null;
    }
}
