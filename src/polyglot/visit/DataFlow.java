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
    
    boolean replicateFinally;

    /**
     * Constructor
     */
    public DataFlow(Job job, TypeSystem ts, NodeFactory nf, boolean forward, boolean replicateFinally) {
	super(job, ts, nf);
        this.forward = forward;
        this.replicateFinally = replicateFinally;
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
     * Produce a new Item as appropriate for the Computation n and the 
     * input Item in. Note that if the Computation n has many flows going into
     * it, the Item in may be the result of a call to confluence(List)
     * 
     * @return a non-null Item.
     */
    protected abstract Item flow(Item in, FlowGraph graph, Computation n);
    
    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items.
     * 
     * @return a non-null Item.
     */
    protected abstract Item confluence(List items);
    
    /**
     * Check that the computation n satisfies whatever properties this
     * dataflow is checking for. This method is called for each computation
     * in a code declaration block after the dataflow for that block of code 
     * has been performed.
     * 
     * @throws SemanticException if the properties this dataflow analysis
     *         is checking for is not satisfied.
     */
    protected abstract void check(FlowGraph graph, Computation n, Item inItem, Item outItem) throws SemanticException;

    /**
     * Perform the dataflow on the flowgraph provided.
     */
    public void dataflow(FlowGraph graph) {
        // queue of Peers whose flow needs to be updated.
        LinkedList queue = new LinkedList();
      
        // Initially put all the Peers in the queue. 
        // ###We could probably be smarter and just put the
        // start peers in, 
        //    i.e. queue = new LinkedList(graph.peers(graph.startNode()));
        for (Iterator i = graph.peers().iterator(); i.hasNext(); ) {
            queue.addLast(i.next());
        }

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
    protected FlowGraph initGraph(CodeDecl code, Computation root) {
        return new FlowGraph(root, forward, replicateFinally);
    }

    /**
     * Over-ridden superclass method, to build the flow graph, perform dataflow
     * analysis, and check the analysis for CodeDecl nodes.
     */
    public Node leaveCall(Node n) throws SemanticException {
        if (n instanceof CodeDecl) {
            Block body = ((CodeDecl) n).body();

            if (body != null) {
                // Compute the successor of each child node.
                FlowGraph g = initGraph((CodeDecl) n, body);

                if (g != null) {
                    // Build the control flow graph.
                    CFGBuilder v = new CFGBuilder(ts, g, this);
                    v.visitGraph();

                    dataflow(g);

                    return ((CodeDecl) n).body(post(g, body));
                }
            }
        }

        return n;
    }

    /**
     * Check all of the Peers in the graph, after the dataflow analysis has
     * been performed.
     */
    public Block post(FlowGraph graph, Block root) throws SemanticException {
        for (Iterator i = graph.peers().iterator(); i.hasNext(); ) {
            FlowGraph.Peer p = (FlowGraph.Peer) i.next();
            this.check(graph, p.node, p.inItem, p.outItem);
        }

        return root;
    }
}
