package polyglot.visit;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.main.Report;
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
     * input Item in. 
     * 
     * @param in the Item flowing into the node. Note that if the Term n 
     *           has many flows going into it, the Item in may be the result 
     *           of a call to confluence(List, Term)
     * @param graph the FlowGraph which the dataflow is operating on
     * @param n the Term which this method must calculate the flow for.
     * @param edgeKeys a set of FlowGraph.EdgeKeys, being all the 
     *          EdgeKeys of the edges leaving this node. The 
     *          returned Map must have mappings for all objects in this set.
     * @return a Map from FlowGraph.EdgeKeys to Items. The map must have 
     *          entries for all EdgeKeys in edgeKeys. 
     */
    protected abstract Map flow(Item in, FlowGraph graph, Term n, Set edgeKeys);
    
    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items, for the confluence just before flow enters 
     * node.
     * 
     * @return a non-null Item.
     */
    protected abstract Item confluence(List items, Term node);
    
    /**
     * Check that the term n satisfies whatever properties this
     * dataflow is checking for. This method is called for each term
     * in a code declaration block after the dataflow for that block of code 
     * has been performed.
     * 
     * @throws SemanticException if the properties this dataflow analysis
     *         is checking for is not satisfied.
     */
    protected abstract void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException;

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
                FlowGraph.Edge e = (FlowGraph.Edge)i.next();
                FlowGraph.Peer o = e.getTarget();
                Item it = null;
                if (o.outItems != null) {
                    it = (Item)o.outItems.get(e.getKey());
                    if (it == null) {
                        throw new InternalCompilerError("There should have " +
                                "an out Item with edge key " + e.getKey() +
                                "; instead there were only " + 
                                o.outItems.keySet());
                    }
                    inItems.add(it);
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
                p.inItem = this.confluence(inItems, p.node);
            }


            // calculate the out item
            Map oldOutItems = p.outItems;
            p.outItems = this.flow(p.inItem, graph, p.node, p.succEdgeKeys());
                    
            if (!p.succEdgeKeys().equals(p.outItems.keySet())) {
                // This check is more for developers to ensure that they
                // have implemented their dataflow correctly. If performance
                // is an issue, maybe we should remove this check.
                throw new InternalCompilerError("The flow only defined " +
                        "outputs for " + p.outItems.keySet() + "; needs to " +
                        "define outputs for all of: " + p.succEdgeKeys());
            }
            
            if (oldOutItems != p.outItems && 
                 (oldOutItems == null || !oldOutItems.equals(p.outItems))) {
                // the outItems of p has changed, so we will 
                // to (re)calculate the flow for the successors of p.
                // Add each successor of p back onto the queue. 
                for (Iterator i = p.succs.iterator(); i.hasNext(); ) {
                    FlowGraph.Peer q = ((FlowGraph.Edge) i.next()).getTarget();
                    
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
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }
        
        // Check the nodes in approximately flow order.
        Set uncheckedPeers = new HashSet(graph.peers());
        LinkedList peersToCheck = new LinkedList(graph.peers(graph.startNode()));
        while (!peersToCheck.isEmpty()) {
            FlowGraph.Peer p = (FlowGraph.Peer) peersToCheck.removeFirst();
            uncheckedPeers.remove(p);

            this.check(graph, p.node, p.inItem, p.outItems);
            
            for (Iterator iter = p.succs.iterator(); iter.hasNext(); ) {
                FlowGraph.Peer q = ((FlowGraph.Edge)iter.next()).getTarget();
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
    
    /**
     * This utility methods is for subclasses to convert a single Item into
     * a Map, to return from the flow(Item, FlowGraph, Term, Set) method. This
     * method should be used when the same output Item from the flow is to be
     * used for all edges leaving the node.
     * 
     * @param i the Item to be placed in the returned Map as the value for
     *          every EdgeKey in edgeKeys.
     * @param edgeKeys the Set of EdgeKeys to be used as keys in the returned 
     *          Map.
     * @return a Map containing a mapping from every EdgeKey in edgeKeys to
     *          the Item i.
     */
    protected static final Map itemToMap(Item i, Set edgeKeys) {
        Map m = new HashMap();
        for (Iterator iter = edgeKeys.iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            m.put(o, i);
        }
        return m;
    }
    
    /**
     * This utility method is for subclasses to determine if the node currently
     * under consideration has both true and false edges leaving it, i.e. that
     * the flow graph at this node has successor edges with the EdgeKeys 
     * FlowGraph.EDGE_KEY_TRUE and FlowGraph.EDGE_KEY_FALSE.
     * 
     * @param edgeKeys the Set of EdgeKeys of the successor edges of a given 
     *            node.
     * @return true if the edgeKeys contains both FlowGraph.EDGE_KEY_TRUE and 
     *            FlowGraph.EDGE_KEY_FALSE
     */
    protected static final boolean hasTrueFalseBranches(Set edgeKeys) {
        return edgeKeys.contains(FlowGraph.EDGE_KEY_FALSE) &&
               edgeKeys.contains(FlowGraph.EDGE_KEY_TRUE);
    }
        
    /**
     * This utility method is meant to be used by subclasses to help them 
     * produce appropriate <code>Item</code>s for the FlowGraph.EDGE_KEY_TRUE
     * and FlowGraph.EDGE_KEY_FALSE edges from a boolean condition.
     * 
     * @param booleanCond the boolean condition that is used to branch on. The
     *              type of the expression must be boolean.
     * @param startingItem the <code>Item</code> at the start of the flow for
     *              the expression <code>booleanCond</code>. 
     * @param succEdgeKeys the set of EdgeKeys of the successor nodes of the 
     *              current node. Must contain both FlowGraph.EDGE_KEY_TRUE
     *              and FlowGraph.EDGE_KEY_FALSE
     * @param navigator an instance of ConditionNavigator to be used to generate
     *              appropriate Items from the boolean condition.
     * @return a Map containing mappings for all entries in succEdgeKeys.
     *              FlowGraph.EDGE_KEY_TRUE and FlowGraph.EDGE_KEY_FALSE 
     *              map to Items calculated for them using navigator, and all
     *              other objects in succEdgeKeys are mapped to startingItem.
     */
    protected static Map constructItemsFromCondition(Expr booleanCond, 
                                                     Item startingItem,
                                                     Set succEdgeKeys,
                                                     ConditionNavigator navigator) {
        // check the arguments to make sure this method is used correctly
        if (!booleanCond.type().isBoolean()) {
            throw new IllegalArgumentException("booleanCond must be a boolean expression");
        }
        if (!hasTrueFalseBranches(succEdgeKeys)) {
            throw new IllegalArgumentException("succEdgeKeys does not have true and false branches.");
        }
        
        
        BoolItem results = navigator.navigate(booleanCond, startingItem);
        
        Map m = new HashMap();
        m.put(FlowGraph.EDGE_KEY_TRUE, results.trueItem);
        m.put(FlowGraph.EDGE_KEY_FALSE, results.falseItem);
        
        // put the starting item in the map for any EdgeKeys other than
        // EDGE_KEY_TRUE and EDGE_KEY_FALSE
        for (Iterator iter = succEdgeKeys.iterator(); iter.hasNext(); ) {
            FlowGraph.EdgeKey e = (FlowGraph.EdgeKey)iter.next();
            if (!FlowGraph.EDGE_KEY_TRUE.equals(e) &&
                !FlowGraph.EDGE_KEY_FALSE.equals(e)) {
                m.put(e, startingItem);
            }
        }
        
        return m;
    }
    
    /**
     * This class contains two <code>Item</code>s, one being the 
     * <code>Item</code> that is used when an expression is true, the
     * other being the one that is used when an expression is false. It is used
     * by the <code>ConditionNavigator</code>.
     */
    protected static class BoolItem {
        public BoolItem(Item trueItem, Item falseItem) {
            this.trueItem = trueItem;
            this.falseItem = falseItem;            
        }
        Item trueItem;
        Item falseItem;
        public String toString() {
            return "[ true: " + trueItem + "; false: " + falseItem + " ]";
        }
        
    }

    /**
     * A ConditionNavigator is used to traverse boolean expressions that are
     * used as conditions, such as in if statements, while statements, 
     * left branches of && and ||. The ConditionNavigator is used to generate
     * a finer-grained analysis, so that the branching flows from a 
     * condition can take into account the fact that the condition is true or
     * false. For example, in the statement <code>if cond then s1 else s2</code>,
     * dataflow for <code>s1</code> can continue in the knowledge that 
     * <code>cond</code> evaluated to true, and similarly, <code>s2</code>
     * can be analyzed using the knowledge that <code>cond</code> evaluated to
     * false.
     */
    protected abstract static class ConditionNavigator {
        /**
         * Navigate the expression <code>expr</code>, where the 
         * <code>Item</code> at the start of evaluating the expression is 
         * <code>startingItem</code>.
         * 
         * A <code>BoolItem</code> is returned, containing the 
         * <code>Item</code>s that are appropriate when <code>expr</code>
         * evaluates to true and false.
         */
        public BoolItem navigate(Expr expr, Item startingItem) {
            if (expr.type().isBoolean()) {
                if (expr instanceof Binary) {
                    Binary b = (Binary)expr;
                    if (Binary.COND_AND.equals(b.operator()) ||
                        Binary.BIT_AND.equals(b.operator())) {
                        
                        BoolItem leftRes = navigate(b.left(), startingItem);
                        Item rightResStart = startingItem;
                        if (Binary.COND_AND.equals(b.operator())) {
                            // due to short circuiting, if the right
                            // branch is evaluated, the starting item is
                            // in fact the true part of the left result
                            rightResStart = leftRes.trueItem;                            
                        }
                        BoolItem rightRes = navigate(b.right(), rightResStart);
                        return andResults(leftRes, rightRes, startingItem);
                    }
                    else if (Binary.COND_OR.equals(b.operator()) ||
                             Binary.BIT_OR.equals(b.operator())) {
                        
                        BoolItem leftRes = navigate(b.left(), startingItem);
                        Item rightResStart = startingItem;
                        if (Binary.COND_OR.equals(b.operator())) {
                            // due to short circuiting, if the right
                            // branch is evaluated, the starting item is
                            // in fact the false part of the left result
                            rightResStart = leftRes.falseItem;                            
                        }
                        BoolItem rightRes = navigate(b.right(), rightResStart);
                        return orResults(leftRes, rightRes, startingItem);
                    }
                }
                else if (expr instanceof Unary) {
                    Unary u = (Unary)expr;
                    if (Unary.NOT.equals(u.operator())) {
                        BoolItem res = navigate(u.expr(), startingItem);
                        return notResult(res);
                    }
                }

            }
            
            // either we are not a boolean expression, or not a logical 
            // connective. Let the subclass deal with it.
            return handleExpression(expr, startingItem);
        }
        
        /**
         * Combine the results of analyzing the left and right arms of
         * an AND boolean operator (either && or &).
         */
        public BoolItem andResults(BoolItem left, 
                                   BoolItem right, 
                                   Item startingItem) {
            return new BoolItem(combine(left.trueItem, right.trueItem),
                                startingItem);            
        }

        /**
         * Combine the results of analyzing the left and right arms of
         * an OR boolean operator (either || or |).
         */
        public BoolItem orResults(BoolItem left, 
                                  BoolItem right, 
                                  Item startingItem) {
            return new BoolItem(startingItem,
                                combine(left.falseItem, right.falseItem));                        
        }

        /**
         * Modify the results of analyzing the child of 
         * a NEGATION boolean operator (a !).
         */
        public BoolItem notResult(BoolItem results) {
            return new BoolItem(results.falseItem, results.trueItem);            
        }

        /**
         * Combine two <code>Item</code>s together, when the information 
         * contained in both items is true. Thus, for example, in a not-null
         * analysis, where <code>Item</code>s are sets of not-null variables,
         * combining them corresponds to unioning the sets. Note that this
         * could be a different operation to the confluence operation.
         */
        public abstract Item combine(Item item1, Item item2);

        /**
         * Produce a <code>BoolItem</code> for an expression that is not
         * a boolean operator, such as &&, &, ||, | or !.
         */
        public abstract BoolItem handleExpression(Expr expr, Item startingItem);
    }
    
    private static int flowCounter = 0;
    /**
     * Dump a flow graph, labelling edges with their flows, to aid in the
     * debugging of data flow.
     */
    private void dumpFlowGraph(FlowGraph graph, CodeDecl root) {
        Report.report(2, "digraph Flow" + (flowCounter++) + " {");
        Report.report(2, "  center=true; ratio=auto; size = \"8.5,11\";");
        // Loop around the nodes...
        for (Iterator iter = graph.peers().iterator(); iter.hasNext(); ) {
            FlowGraph.Peer p = (FlowGraph.Peer)iter.next();
            
            // dump out this node
            Report.report(2,
                          p.hashCode() + " [ label = \"" +
                          StringUtil.escape(p.node.toString()) + " (" + 
                          StringUtil.escape(StringUtil.getShortNameComponent(p.node.getClass().getName()))+ ")\" ];");
            
            // dump out the successors.
            for (Iterator iter2 = p.succs.iterator(); iter2.hasNext(); ) {
                FlowGraph.Edge q = (FlowGraph.Edge)iter2.next();
                Report.report(2,
                              q.getTarget().hashCode() + " [ label = \"" +
                              StringUtil.escape(q.getTarget().node.toString()) + " (" + 
                              StringUtil.escape(StringUtil.getShortNameComponent(q.getTarget().node.getClass().getName()))+ ")\" ];");
                Report.report(2, p.hashCode() + " -> " + q.getTarget().hashCode() + 
                              " [label=\"" + q.getKey() + ": " + 
                              q.getTarget().outItems.get(q.getKey()) + "\"];");
            }
            
        }
        Report.report(2, "}");
    }
}
