package polyglot.visit;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

/**
 * Abstract dataflow Visitor, to allow simple dataflow equations to be easily
 * implemented.
 */
public abstract class DataFlow extends ErrorHandlingVisitor
{
    /**
     * Is this dataflow a forward analysis?
     */
    boolean forward;

    /**
     * Constructor
     */
    public DataFlow(Job job, TypeSystem ts, NodeFactory nf, boolean forward) {
	super(job, ts, nf);
        this.forward = forward;
    }

    /**
     * An Item contains the data which flows during the dataflow analysis. Each
     * node in the flow graph will have two Items associated with it: the input
     * Item, and the output Item, which results from calling flow with the
     * input Item. The input item may itself be the result of a call to the 
     * confluence method, if many paths flow into the same node.
     * 
     * NOTE: the equals(Item) method and hashCode method must be implemented
     * to ensure that the dataflow algorithm works correctly.
     */
    public static abstract class Item {
        public abstract boolean equals(Object i);
        public abstract int hashCode();
    }

    /**
     * Create an initial Item. This is generally the Item that will be given
     * to the entry point of a graph.
     * 
     * @return a non-null Item.
     */
    protected abstract Item createInitialItem();
    
    /**
     * Produce a new Item as appropriate for the Term n and the 
     * input Item in. Note that if the Term n has many flows going into
     * it, the Item in may be the result of a call to confluence(List)
     * 
     * @return a non-null Item.
     */
    protected abstract Item flow(Item in, FlowGraph graph, Term n);
    
    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items.
     * 
     * @return a non-null Item.
     */
    protected abstract Item confluence(List items);
    
    /**
     * Check that the term n satisfies whatever properties this
     * dataflow is checking for. This method is called for each term
     * in a code declaration block after the dataflow for that block of code 
     * has been performed.
     * 
     * @throws SemanticException if the properties this dataflow analysis
     *         is checking for is not satisfied.
     */
    protected abstract void check(FlowGraph graph, Term n, Item inItem, Item outItem) throws SemanticException;

    /**
     * Construct a flow graph for the CodeDecl provided, and call 
     * dataflow(FlowGraph). Is also responsible for calling 
     * post(FlowGraph, Block) after dataflow(FlowGraph) has been called. 
     * Returns the (possibly new) AST. 
     */
    protected CodeDecl dataflow(CodeDecl cd) throws SemanticException {
        // only bother to do the flow analysis if the body is not null...
        if (cd.body() != null) {
            // Compute the successor of each child node.
            FlowGraph g = initGraph(cd, cd);

            if (g != null) {
                // Build the control flow graph.
                CFGBuilder v = new CFGBuilder(ts, g, this);
                v.visitGraph();

                dataflow(g);

                return post(g, cd);
            }
        }

        return cd;
    } 
    
    /**
     * Perform the dataflow on the flowgraph provided.
     */
    protected void dataflow(FlowGraph graph) {
        // queue of Peers whose flow needs to be updated.
        // initially this is only the start node of the graph.
        // Thus, unreachable nodes will have no flow through them at all.
        LinkedList queue = new LinkedList(graph.peers(graph.startNode()));
        
        // ### we could be a bit smarter and determine the strongly connected
        // components of the flow graph, and process those in topographic
        // order.
        while (! queue.isEmpty()) {
            FlowGraph.Peer p = (FlowGraph.Peer) queue.removeFirst();
            
            // get the in items by examining the out items of all 
            // the predecessors of p
            List inItems = new ArrayList(p.preds.size());
            for (Iterator i = p.preds.iterator(); i.hasNext(); ) {
                FlowGraph.Peer o = (FlowGraph.Peer)i.next();
                if (o.outItem != null) {
                    inItems.add(o.outItem);
                }
            }
    
            if (inItems.isEmpty()) {
                // there are no input Items as yet (or possibly never). Use an 
                // inital Item, provided by the concrete subclass.
                p.inItem = this.createInitialItem();
            }
            else if (inItems.size() == 1) {
                // There is only one input Item, no need to use the confluence 
                // operator.
                p.inItem = (Item)inItems.get(0);
            }
            else {
                // more than one inItem, so join them together using the
                // confluence operator.
                p.inItem = this.confluence(inItems);
            }


            // calculate the out item
            Item oldOutItem = p.outItem;
            p.outItem = this.flow(p.inItem, graph, p.node);
                    
            if (oldOutItem != p.outItem && 
                 (oldOutItem == null || !oldOutItem.equals(p.outItem))) {
                // the outItem of p has changed, so we will 
                // to (re)calculate the flow for the successors of p.
                // Add each successor of p back onto the queue. 
                for (Iterator i = p.succs.iterator(); i.hasNext(); ) {
                    FlowGraph.Peer q = (FlowGraph.Peer) i.next();
                    
                    // System.out.println("// " + p.node + " -> " + q.node);
                    
                    // Edge p -> q.
                    if (!queue.contains(q)) {
                        queue.addLast(q);
                    }  
                }
            }
        }
    }

    /**
     * Initialise the FlowGraph to be used in the dataflow analysis.
     * @return null if no dataflow analysis should be performed for this
     *         code declaration; otherwise, an apropriately initialized
     *         FlowGraph.
     */
    protected FlowGraph initGraph(CodeDecl code, Term root) {
        return new FlowGraph(root, forward);
    }

    /**
     * Over-ridden superclass method, to build the flow graph, perform dataflow
     * analysis, and check the analysis for CodeDecl nodes.
     */
    public Node leaveCall(Node n) throws SemanticException {
        if (n instanceof CodeDecl) {
            return dataflow((CodeDecl)n);
        }

        return n;
    }

    /**
     * Check all of the Peers in the graph, after the dataflow analysis has
     * been performed.
     */
    public CodeDecl post(FlowGraph graph, CodeDecl root) throws SemanticException {
        // Check the nodes in approximately flow order.
        Set uncheckedPeers = new HashSet(graph.peers());
        LinkedList peersToCheck = new LinkedList(graph.peers(graph.startNode()));
        while (!peersToCheck.isEmpty()) {
            FlowGraph.Peer p = (FlowGraph.Peer) peersToCheck.removeFirst();
            uncheckedPeers.remove(p);
            this.check(graph, p.node, p.inItem, p.outItem);
            
            for (Iterator iter = p.succs.iterator(); iter.hasNext(); ) {
                FlowGraph.Peer q = (FlowGraph.Peer)iter.next();
                if (uncheckedPeers.contains(q) && !peersToCheck.contains(q)) {
                    // q hasn't been checked yet.
                    peersToCheck.addLast(q);
                }
            }
            
            if (peersToCheck.isEmpty() && !uncheckedPeers.isEmpty()) {
                // done all the we can reach...
                Iterator i = uncheckedPeers.iterator();                
                peersToCheck.add(i.next());
                i.remove();
            }
            
        }

        return root;
    }
}
