package polyglot.visit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.CompoundStmt;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;

/**
 * Visitor which checks that all statements must be reachable
 */
public class ReachChecker extends DataFlow
{
    public ReachChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf, true /* forward analysis */);
    }

    public Item createInitialItem(FlowGraph graph) {
        return DataFlowItem.NOT_REACHABLE;
    }

    protected static class DataFlowItem extends Item {
        final boolean reachable;

        protected DataFlowItem(boolean reachable) {
            this.reachable = reachable;
        }
        
        public static DataFlowItem REACHABLE = new DataFlowItem(true);
        public static DataFlowItem NOT_REACHABLE = new DataFlowItem(false);

        public String toString() {
            return "reachable=" + reachable;
        }
        
        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.reachable == ((DataFlowItem)o).reachable;
            }
            return false;
        }
        public int hashCode() {
            return (reachable ? 5423 : 5753);
        }
    }

    public Map flow(Item in, FlowGraph graph, Term n, Set succEdgeKeys) {
        if (n == graph.entryNode()) {
            return itemToMap(DataFlowItem.REACHABLE, succEdgeKeys);
        }
        else {
            return itemToMap(in, succEdgeKeys);
        }
    }

    public Item confluence(List inItems, Term node) {
        // if any predecessor is reachable, so is this one
        for (Iterator i = inItems.iterator(); i.hasNext(); ) {
            if (((DataFlowItem)i.next()).reachable) {
                return DataFlowItem.REACHABLE;
            }
        }
        return DataFlowItem.NOT_REACHABLE;
    }

    public void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException {
        throw new InternalCompilerError("ReachChecker.check should " +
                "never be called.");
    }
    
    public Term post(FlowGraph graph, Term root) throws SemanticException {
        MAPS:
        for (Iterator i = graph.pathMaps().iterator(); i.hasNext(); ) {
            Map m = (Map) i.next();

            if (m.isEmpty()) {
                continue;
            }

            Node n = null;

            // Check that some path to a node makes the node reachable.
            for (Iterator j = m.values().iterator(); j.hasNext(); ) {
                FlowGraph.Peer p = (FlowGraph.Peer) j.next();
                n = p.node;

                if (p.outItems != null) {
                    for (Iterator k = p.outItems.values().iterator(); k.hasNext(); ) {
                        DataFlowItem item = (DataFlowItem) k.next();

                        if (item != null && item.reachable) {
                            continue MAPS;
                        }                    
                    }
                }
            }

            // Compound statements are allowed to be unreachable
            // (e.g., "{ // return; }" or "while (true) S").  If a compound
            // statement is truly unreachable, one of its sub-statements will
            // be also and we will report an error there.

            if ((n instanceof Block && ((Block) n).statements().isEmpty()) ||
                (n instanceof Stmt && ! (n instanceof CompoundStmt))) {
                throw new SemanticException("Unreachable statement.",
                                            n.position());
            }
        }

        return root;
    }
}
