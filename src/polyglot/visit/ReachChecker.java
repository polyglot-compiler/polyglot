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
              false /* replicate finally */);
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
            // Check for unreachable statements; compound statements are
            // allowed to be unreachable (e.g., "{ return; }" or
            // "while (true) S").  If a compound statement is truly
            // unreachable, one of its sub-statements will be also and we will
            // report an error there.
            if ((n instanceof Block && ((Block) n).statements().isEmpty()) ||
                (n instanceof Stmt && ! (n instanceof CompoundStmt))) {

                if (! reachable) {
                    throw new SemanticException("Unreachable statement.",
                                                n.position());
                }
            }
        }
    }
}
