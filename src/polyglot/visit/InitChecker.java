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

    public Item createInitialItem() {
        return new DataFlowItem();
    }

    static class DataFlowItem extends Item {
        Set undefined;

        DataFlowItem() {
            this.undefined = new HashSet();
        }
        DataFlowItem(Set s) {
            this.undefined = s;
        }

        public String toString() {
            return undefined.toString();
        }

        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.undefined.equals(((DataFlowItem)o).undefined);
            }
            return false;
        }
        
        public int hashCode() {
            return (undefined.hashCode());
        }

    }

    public Item flow(Item inItem, FlowGraph graph, Computation n) {
        // We need not consider Formals -- they are never undefined.

        if (n instanceof LocalDecl) {
            LocalDecl l = (LocalDecl) n;

            if (l.init() == null) {
                DataFlowItem x = new DataFlowItem();
                x.undefined.addAll(((DataFlowItem)inItem).undefined);
                x.undefined.add(l.localInstance());
                return x;
            }
        }

        if (n instanceof Assign) {
            Assign a = (Assign) n;

            if (a.left() instanceof Local) {
                Local l = (Local) a.left();

                DataFlowItem x = new DataFlowItem();
                x.undefined.addAll(((DataFlowItem)inItem).undefined);
                x.undefined.remove(l.localInstance());
                return x;
            }
        }

        return inItem;
    }

    public Item confluence(List inItems) {
        // confluence operator is union
        Set undef = new HashSet();
        for (Iterator i = inItems.iterator(); i.hasNext(); ) {
            undef.addAll(((DataFlowItem)i.next()).undefined);
        }
        return new DataFlowItem(undef);
    }

    public void check(FlowGraph graph, Computation n, Item in, Item out) throws SemanticException {
        if (n instanceof Local) {
            Local l = (Local) n;

            if (((DataFlowItem)in).undefined.contains(l.localInstance())) {
                throw new SemanticException("Local variable \"" + l.name() +
                                            "\" may not have been initialized.",
                                            l.position());
            }
        }
    }
}
