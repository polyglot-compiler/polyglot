package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

/**
 * Visitor which checks that all statements must be reachable
 */
public class ReachChecker extends DataFlow
{
    public ReachChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf, true /* forward analysis */);
    }

    public Item createInitialItem() {
        return DataFlowItem.NOT_REACHABLE;
    }

    static class DataFlowItem extends Item {
        final boolean reachable;

        private DataFlowItem(boolean reachable) {
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

    public Item flow(Item in, FlowGraph graph, Term n) {
        if (n == graph.entryNode()) {
            return DataFlowItem.REACHABLE;
        }
        else {
            return in;
        }
    }

    public Item confluence(List inItems) {
        // if any predecessor is reachable, so is this one
        for (Iterator i = inItems.iterator(); i.hasNext(); ) {
            if (((DataFlowItem)i.next()).reachable) {
                return DataFlowItem.REACHABLE;
            }
        }
        return DataFlowItem.NOT_REACHABLE;
    }

    public void check(FlowGraph graph, Term n, Item inItem, Item outItem) throws SemanticException {
    }
    
    public CodeDecl post(FlowGraph graph, CodeDecl root) throws SemanticException {
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

                DataFlowItem item = (DataFlowItem) p.outItem;

                if (item != null && item.reachable) {
                    continue MAPS;
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
