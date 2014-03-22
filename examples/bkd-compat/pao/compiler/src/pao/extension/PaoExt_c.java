/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package pao.extension;

import pao.types.PaoTypeSystem;
import polyglot.ast.Ext_c;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;

/**
 * Default implementation of <code>PaoExt</code>.
 */
public class PaoExt_c extends Ext_c implements PaoExt {
    /**
     * Default implementation of <code>rewrite</code>, returns the node
     * unchanged.
     * 
     * @see PaoExt#rewrite(PaoTypeSystem, NodeFactory)
     */
    @Override
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {
        return node();
    }
}
