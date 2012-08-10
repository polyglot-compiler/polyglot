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

import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It contains a pointer back to the node it is extending and a possibly-null
 * pointer to another extension node. 
 */
public abstract class Ext_c implements Ext {
    protected Node node;
    protected Ext ext;

    public Ext_c(Ext ext) {
        this.node = null;
        this.ext = ext;
    }

    public Ext_c() {
        this(null);
        this.node = null;
    }

    /** Initialize the extension object's pointer back to the node.
     * This also initializes the back pointers for all extensions of
     * the extension.
     */
    @Override
    public void init(Node node) {
        if (this.node != null) {
            throw new InternalCompilerError("Already initialized.");
        }

        this.node = node;

        if (this.ext != null) {
            this.ext.init(node);
        }
    }

    /**
     * Return the node we ultimately extend.
     */
    @Override
    public Node node() {
        return node;
    }

    /**
     * Return our extension object, or null.
     */
    @Override
    public Ext ext() {
        return ext;
    }

    @Override
    public Ext ext(Ext ext) {
        Ext old = this.ext;
        this.ext = null;

        Ext_c copy = (Ext_c) copy();

        copy.ext = ext;

        this.ext = old;

        return copy;
    }

    /**
     * Copy the extension.
     */
    @Override
    public Object copy() {
        try {
            Ext_c copy = (Ext_c) super.clone();
            if (ext != null) {
                copy.ext = (Ext) ext.copy();
            }
            copy.node = null; // uninitialize
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    @Override
    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }

    /**
     * Dump the AST node for debugging purposes.
     */
    @Override
    public void dump(CodeWriter w) {
        w.write(toString());
    }
}
