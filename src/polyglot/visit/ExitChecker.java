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
public class ExitChecker extends DataFlow
{
    public ExitChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf,
              false /* backward analysis */,
              true /* replicate finally */);
    }

    CodeDecl code;

    protected FlowGraph initGraph(CodeDecl code, Computation root) {
        boolean returnsValue;

        this.code = code;

        if (code instanceof MethodDecl) {
            MethodDecl d = (MethodDecl) code;
            if (! d.methodInstance().returnType().isVoid()) {
                return super.initGraph(code, root);
            }
        }

        return null;
    }

    public Item createItem(FlowGraph graph, Computation n) {
        // If every path from the exit node to the entry goes through a return,
        // we're okay.  So make the exit bit false at exit and true every else;
        // the meet operation is &&. 
        return new DataFlowItem(n != graph.exitNode());
    }

    class DataFlowItem implements Item {
        boolean exits; // whether all paths leaving this node lead to an exit 

        DataFlowItem(boolean exits) {
            this.exits = exits;
        }

        public Item flow(Computation n) {
            if (n instanceof Return) {
                return new DataFlowItem(true);
            }

            return this;
        }

        public String toString() {
            return "exits=" + exits;
        }

        public boolean meet(Item item) {
            DataFlowItem x = (DataFlowItem) item;

            boolean old_exits = exits;
            exits = exits && x.exits; // all paths must have an exit

            return exits != old_exits;
        }

        public void check(FlowGraph graph, Computation n) throws SemanticException {
            // Check for statements not on the path to exit; compound
            // statements are allowed to be off the path.  (e.g., "{ return; }"
            // or "while (true) S").  If a compound statement is truly
            // unreachable, one of its sub-statements will be also and we will
            // report an error there.
            if (n == graph.entryNode()) {
                DataFlowItem item = (DataFlowItem) flow(n);

                if (! item.exits) {
                    throw new SemanticException("Missing return statement.",
                                                code.position());
                }
            }
        }
    }
}
