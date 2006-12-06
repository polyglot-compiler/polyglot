package pao.extension;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Ext_c;
import pao.types.PaoTypeSystem;

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
    public Node rewrite(PaoTypeSystem ts, NodeFactory nf) {
        return node();
    }
}
