package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

/**
 * Visitor which checks data flow properties of each method.
 * 1) definite assignment: local variables must be defined before use
 * 2) reachability: all statements must be reachable
 * 3) termination: all (terminating) paths through a method must return.
 */
public class ReachChecker extends DataFlow
{
    public ReachChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf,
              true /* forward analysis */,
              true /* replicate finally */);
    }

    public Item createItem(FlowGraph graph, Computation n) {
        return new DataFlowItem(n == graph.entryNode());
    }

    class DataFlowItem implements Item {
        boolean reachable;

        DataFlowItem(boolean reachable) {
            this.reachable = reachable;
        }

        public String toString() {
            return "reachable=" + reachable;
        }

        public Item flow(Computation n) {
            return this;
        }

        public boolean meet(Item item) {
            DataFlowItem x = (DataFlowItem) item;

            boolean old_reachable = reachable;
            reachable = reachable || x.reachable;

            return reachable != old_reachable;
        }

        public void check(FlowGraph graph, Computation n) throws SemanticException {
        }

    }

    public Block post(FlowGraph graph, Block root) throws SemanticException {
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

                DataFlowItem item = (DataFlowItem) p.item;

                if (item.reachable) {
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
