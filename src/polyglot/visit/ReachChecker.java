package polyglot.visit;

import java.util.Collection;
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
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;

/**
 * Visitor which checks that all statements must be reachable
 */
public class ReachChecker extends DataFlow
{
    public ReachChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf, 
              true /* forward analysis */, 
              true /* perform dataflow on entry to CodeDecls */);
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

    public Node leaveCall(Node n) throws SemanticException {
        // check for reachability.
        if (n instanceof Term) {
           n = checkReachability((Term)n);
        }
         
        return super.leaveCall(n);
    }

    protected Node checkReachability(Term n) throws SemanticException {
        FlowGraph g = currentFlowGraph();
        if (g != null) {   
            Collection peers = g.peers(n);
            if (peers != null && !peers.isEmpty()) {
                for (Iterator iter = peers.iterator(); iter.hasNext(); ) {
                    FlowGraph.Peer p = (FlowGraph.Peer) iter.next();
        
                    // the peer is reachable if at least one of it's out items
                    // is reachable. This would cover all cases, except that some
                    // peers may have no successors (e.g. peers that throw an
                    // an exception that is not caught by the method). So we need 
                    // to also check the inItem.
                    if (p.inItem() != null && ((DataFlowItem)p.inItem()).reachable) {
                        return n.reachable(true);                
                    }
                    
                    if (p.outItems != null) {
                        for (Iterator k = p.outItems.values().iterator(); k.hasNext(); ) {
                            DataFlowItem item = (DataFlowItem) k.next();
                        
                            if (item != null && item.reachable) {
                                // n is reachable.
                                return n.reachable(true);
                            }                    
                        }
                    }
                }
                
                // if we fall through to here, then no peer for n was reachable.
                n = n.reachable(false);
                
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
        }        
        return n;
    }
    
    public void post(FlowGraph graph, Term root) throws SemanticException {
        // There is no need to do any checking in this method, as this will
        // be handled by leaveCall and checkReachability.
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }
    } 

    public void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException {
        throw new InternalCompilerError("ReachChecker.check should " +
                "never be called.");
    }
    
}
