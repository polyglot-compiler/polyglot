package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

/**
 * Visitor which checks that all (terminating) paths through a 
 * method must return.
 */
public class ExitChecker extends DataFlow
{
    public ExitChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf, false /* backward analysis */);
    }

    CodeDecl code;

    protected FlowGraph initGraph(CodeDecl code, Term root) {
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

    public Item createInitialItem() {
        return DataFlowItem.EXITS;
    }

    static class DataFlowItem extends Item {
        final boolean exits; // whether all paths leaving this node lead to an exit 

        private DataFlowItem(boolean exits) {
            this.exits = exits;
        }
        
        public static DataFlowItem EXITS = new DataFlowItem(true);
        public static DataFlowItem DOES_NOT_EXIT = new DataFlowItem(false);

        public String toString() {
            return "exits=" + exits;
        }
        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.exits == ((DataFlowItem)o).exits;
            }
            return false;
        }
        public int hashCode() {
            return (exits ? 5235 : 8673);
        }
        
    }
    
    public Map flow(Item in, FlowGraph graph, Term n, Set succEdgeKeys) {
        // If every path from the exit node to the entry goes through a return,
        // we're okay.  So make the exit bit false at exit and true at every return;
        // the confluence operation is &&. 
        if (n instanceof Return) {
            return itemToMap(DataFlowItem.EXITS, succEdgeKeys);
        }

        if (n == graph.exitNode()) {
            return itemToMap(DataFlowItem.DOES_NOT_EXIT, succEdgeKeys);
        }

        return itemToMap(in, succEdgeKeys);
    }


    public Item confluence(List inItems, Term node) {
        // all paths must have an exit
        for (Iterator i = inItems.iterator(); i.hasNext(); ) {
            if (!((DataFlowItem)i.next()).exits) {
                return DataFlowItem.DOES_NOT_EXIT;
            }
        }
        return DataFlowItem.EXITS; 
    }

    public void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException {
        // Check for statements not on the path to exit; compound
        // statements are allowed to be off the path.  (e.g., "{ return; }"
        // or "while (true) S").  If a compound statement is truly
        // unreachable, one of its sub-statements will be also and we will
        // report an error there.
        if (n == graph.entryNode()) {
            if (outItems != null && !outItems.isEmpty()) {
                // due to the flow equations, all DataFlowItems in the outItems map
                // are the same, so just take the first one.
                DataFlowItem outItem = (DataFlowItem)outItems.values().iterator().next(); 
                if (outItem != null && !outItem.exits) { 
                    throw new SemanticException("Missing return statement.",
                            code.position());
                }
            }
        }
    }
}
