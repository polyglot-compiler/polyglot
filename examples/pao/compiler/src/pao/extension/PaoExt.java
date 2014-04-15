/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.extension;

import pao.types.PaoTypeSystem;
import polyglot.ast.Ext;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;

/**
 * The <code>Ext</code> interface for the pao extension. The
 * <code>PaoExt</code> interface provides one additional method: 
 * {@link #rewrite(PaoTypeSystem, NodeFactory) rewrite(PaoTypeSystem, NodeFactory)}.
 */
public interface PaoExt extends Ext {
    /**
     * Rewrites the <code>Node</code> associated with this <code>Ext</code>
     * object, to automatically box and unbox primitives.
     * 
     * @param ts The <code>PaoTypeSystem</code> instance.
     * @param nf The <code>NodeFactory</code> instance.
     * @return the result of rewriting the AST node to provide boxing and
     *         unboxing.
     */
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf);
}
