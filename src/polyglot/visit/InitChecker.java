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
public class InitChecker extends DataFlow
{
    public InitChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf,
              true /* forward analysis */,
              true /* replicate finally */);
    }

    public Item createItem(FlowGraph graph, Computation n) {
        return new DataFlowItem();
    }

    class DataFlowItem implements Item {
        Set undefined;

        DataFlowItem() {
            this.undefined = new HashSet();
        }

        public String toString() {
            return undefined.toString();
        }

        public Item flow(Computation n) {
            // We need not consider Formals -- they are never undefined.

            if (n instanceof LocalDecl) {
                LocalDecl l = (LocalDecl) n;

                if (l.init() == null) {
                    DataFlowItem x = new DataFlowItem();
                    x.undefined.addAll(undefined);
                    x.undefined.add(l.localInstance());
                    return x;
                }
            }

            if (n instanceof Assign) {
                Assign a = (Assign) n;

                if (a.left() instanceof Local) {
                    Local l = (Local) a.left();

                    DataFlowItem x = new DataFlowItem();
                    x.undefined.addAll(undefined);
                    x.undefined.remove(l.localInstance());
                    return x;
                }
            }

            return this;
        }

        public boolean meet(Item item) {
            DataFlowItem x = (DataFlowItem) item;

            if (undefined.addAll(x.undefined)) {
                // there was a change
                return true;
            }

            return false;
        }

        public void check(FlowGraph graph, Computation n) throws SemanticException {
            if (n instanceof Local) {
                Local l = (Local) n;

                if (undefined.contains(l.localInstance())) {
                    throw new SemanticException("Local variable \"" + l.name() +
                                                "\" may not have been initialized.",
                                                l.position());
                }
            }
        }
    }
}
