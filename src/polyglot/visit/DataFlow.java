package polyglot.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.IdentityKey;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;
import polyglot.visit.FlowGraph.Edge;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;
import polyglot.visit.FlowGraph.ExceptionEdgeKey;

/**
 * Abstract dataflow Visitor, to allow simple dataflow equations to be easily
 * implemented.
 */
public abstract class DataFlow extends ErrorHandlingVisitor
{
    /**
     * Indicates whether this dataflow is a forward analysis.
     */
    protected final boolean forward;
    
    /**
     * Indicates whether the dataflow should be performed on entering a
     * <code>CodeDecl</code>, or on leaving a <code>CodeDecl</code>.
     * If dataflow is performed on entry, then the control flow graph
     * will be available when visiting children of the
     * <code>CodeDecl</code>, via the <code>currentFlowGraph</code>
     * method. If dataflow is performed on leaving, then the control
     * flow graph will not be available, but nested
     * <code>CodeDecl</code>s will have already been processed.
     */
    protected final boolean dataflowOnEntry;
    
    /**
     * A stack of <code>FlowGraphSource</code>. The flow graph is constructed 
     * upon entering a CodeDecl AST node, and dataflow performed on that flow 
     * graph immediately. The flow graph is available during the visiting of 
     * children of the CodeDecl, if subclasses want to use this information
     * to update AST nodes. The stack is only maintained if 
     * <code>dataflowOnEntry</code> is true.
     */
    protected LinkedList flowgraphStack;
    
    protected static class FlowGraphSource {
        FlowGraphSource(FlowGraph g, CodeDecl s) {
            flowgraph = g;
            source = s;
        }
        FlowGraph flowgraph;
        CodeDecl source;
        public FlowGraph flowGraph() { return flowgraph; }
        public CodeDecl source() { return source; }
    }
    
    /**
     * Constructor.
     */
    public DataFlow(Job job, TypeSystem ts, NodeFactory nf, boolean forward) {
        this(job, ts, nf, forward, false);
    }

    /**
     * Constructor.
     */
    public DataFlow(Job job, 
                    TypeSystem ts, 
                    NodeFactory nf, 
                    boolean forward, 
                    boolean dataflowOnEntry) {
        super(job, ts, nf);
        this.forward = forward;
        this.dataflowOnEntry = dataflowOnEntry;
        if (dataflowOnEntry)
            this.flowgraphStack = new LinkedList();
        else 
            this.flowgraphStack = null;
    }

    /**
     * An <code>Item</code> contains the data which flows during the dataflow
     * analysis. Each
     * node in the flow graph will have two items associated with it: the input
     * item, and the output item, which results from calling flow with the
     * input item. The input item may itself be the result of a call to the 
     * confluence method, if many paths flow into the same node.
     * 
     * NOTE: the <code>equals(Item)</code> method and <code>hashCode()</code>
     * method must be implemented to ensure that the dataflow algorithm works
     * correctly.
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
    protected abstract Item createInitialItem(FlowGraph graph);
    
    /**
     * Produce a new <code>Item</code> as appropriate for the
     * <code>Term n</code> and the input <code>Item in</code>. 
     * 
     * @param in the Item flowing into the node. Note that if the Term n 
     *           has many flows going into it, the Item in may be the result 
     *           of a call to confluence(List, List, Term)
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
     * @param items List of <code>Item</code>s that flow into <code>node</code>.
     * @param node <code>Term</code> for which the <code>items</code> are 
     *          flowing into.
     * @return a non-null Item.
     */
    protected abstract Item confluence(List items, Term node);
    
    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items, for the confluence just before flow enters 
     * node.
     * 
     * @param items List of <code>Item</code>s that flow into <code>node</code>.
     * @param itemKeys List of <code>FlowGraph.ExceptionEdgeKey</code>s for
     *              the edges that the corresponding <code>Item</code>s in
     *              <code>items</code> flowed from.
     * @param node <code>Term</code> for which the <code>items</code> are 
     *          flowing into.
     * @return a non-null Item.
     */
    protected Item confluence(List items, List itemKeys, Term node) {
        return confluence(items, node); 
    }
    
    /**
     * Check that the term n satisfies whatever properties this
     * dataflow is checking for. This method is called for each term
     * in a code declaration block after the dataflow for that block of code 
     * has been performed.
     * 
     * @throws <code>SemanticException</code> if the properties this dataflow
     *         analysis is checking for is not satisfied.
     */
    protected abstract void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException;

    /**
     * Construct a flow graph for the <code>CodeDecl</code> provided, and call 
     * <code>dataflow(FlowGraph)</code>. Is also responsible for calling 
     * <code>post(FlowGraph, Block)</code> after
     * <code>dataflow(FlowGraph)</code> has been called, and for pushing
     * the <code>FlowGraph</code> onto the stack of <code>FlowGraph</code>s if
     * dataflow analysis is performed on entry to <code>CodeDecl</code> nodes.
     */
    protected void dataflow(CodeDecl cd) throws SemanticException {
        // only bother to do the flow analysis if the body is not null...
        if (cd.body() != null) {
            // Compute the successor of each child node.
            FlowGraph g = initGraph(cd, cd);

            if (g != null) {
                // Build the control flow graph.
                CFGBuilder v = new CFGBuilder(ts, g, this);
                v.visitGraph();

                dataflow(g);

                post(g, cd);

                // push the CFG onto the stack if we are dataflowing on entry
                if (dataflowOnEntry)
                    flowgraphStack.addFirst(new FlowGraphSource(g, cd));
            }
        }
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
            Peer p = (Peer) queue.removeFirst();
            
            // get the in items by examining the out items of all 
            // the predecessors of p
            List inItems = new ArrayList(p.preds.size());
            List inItemKeys = new ArrayList(p.preds.size());
            for (Iterator i = p.preds.iterator(); i.hasNext(); ) {
                Edge e = (Edge)i.next();
                Peer o = e.getTarget();
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
                    inItemKeys.add(e.getKey());
                }
            }
    
            if (inItems.isEmpty()) {
                // there are no input Items as yet (or possibly never). Use an 
                // inital Item, provided by the concrete subclass.
                p.inItem = this.createInitialItem(graph);
            }
            else if (inItems.size() == 1) {
                // There is only one input Item, no need to use the confluence 
                // operator.
                p.inItem = (Item)inItems.get(0);
            }
            else {
                // more than one inItem, so join them together using the
                // confluence operator.
                p.inItem = this.confluence(inItems, inItemKeys, p.node);
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
                    Peer q = ((Edge) i.next()).getTarget();
                    
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
     * Initialise the <code>FlowGraph</code> to be used in the dataflow
     * analysis.
     *
     * @return null if no dataflow analysis should be performed for this
     *         code declaration; otherwise, an apropriately initialized
     *         <code>FlowGraph.</code>
     */
    protected FlowGraph initGraph(CodeDecl code, Term root) {
        return new FlowGraph(root, forward);
    }

    /**
     * Overridden superclass method, to build the flow graph, perform dataflow
     * analysis, and check the analysis for CodeDecl nodes.
     */
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (dataflowOnEntry && n instanceof CodeDecl) {
            dataflow((CodeDecl)n);
        }
        
        return this;
    }

    /**
     * Overridden superclass method, to make sure that if a subclass has changed
     * a Term, that we update the peermaps appropriately, since they are based
     * on <code>IdentityKey</code>s.
     */
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (old != n) {            
            if (dataflowOnEntry && currentFlowGraph() != null) {
                // We currently only update the key in the peerMap.
                // We DO NOT update the Terms inside the peers, nor the
                // List of Terms that are the path maps. 
                Object o = currentFlowGraph().peerMap.get(new IdentityKey(old));
                if (o != null) {
                    currentFlowGraph().peerMap.put(new IdentityKey(n), o);
                }
            }
        }
        return super.leave(parent, old, n, v);
    }

    /**
     * Overridden superclass method, to pop from the stack of
     * <code>FlowGraph</code>s if necessary.
     */
    protected Node leaveCall(Node n) throws SemanticException {
        if (n instanceof CodeDecl) {
            if (!dataflowOnEntry) {
                dataflow((CodeDecl)n);
            }
            else if (dataflowOnEntry && !flowgraphStack.isEmpty()) {
                FlowGraphSource fgs = (FlowGraphSource)flowgraphStack.getFirst();
                if (fgs.source.equals(n)) {
                    // we are leaving the code decl that pushed this flowgraph 
                    // on the stack. pop tbe stack.
                    flowgraphStack.removeFirst();
                }
            }
        }        
        return n;
    }

    /**
     * Check all of the Peers in the graph, after the dataflow analysis has
     * been performed.
     */
    protected void post(FlowGraph graph, Term root) throws SemanticException {
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }
        
        // Check the nodes in approximately flow order.
        Set uncheckedPeers = new HashSet(graph.peers());
        LinkedList peersToCheck = new LinkedList(graph.peers(graph.startNode()));
        while (!peersToCheck.isEmpty()) {
            Peer p = (Peer) peersToCheck.removeFirst();
            uncheckedPeers.remove(p);

            this.check(graph, p.node, p.inItem, p.outItems);
            
            for (Iterator iter = p.succs.iterator(); iter.hasNext(); ) {
                Peer q = ((Edge)iter.next()).getTarget();
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
    }
    
    /**
     * Return the <code>FlowGraph</code> at the top of the stack. This method
     * should not be called if dataflow is not being performed on entry to
     * the <code>CodeDecl</code>s, as the stack is not maintained in that case.
     * If this 
     * method is called by a subclass from the <code>enterCall</code> 
     * or <code>leaveCall</code> methods, for an AST node that is a child
     * of a <code>CodeDecl</code>, then the <code>FlowGraph</code> returned 
     * should be the <code>FlowGraph</code> for the dataflow for innermost
     * <code>CodeDecl</code>.
     */
    protected FlowGraph currentFlowGraph() {
        if (!dataflowOnEntry) {
            throw new InternalCompilerError("currentFlowGraph() cannot be" +
                " called when dataflow is not performed on entry");
        }
        if (flowgraphStack.isEmpty()) {
            return null;
        }
        return ((FlowGraphSource)flowgraphStack.getFirst()).flowgraph;
    }
    
    /**
     * This utility methods is for subclasses to convert a single Item into
     * a <code>Map</code>, to return from the
     * <code>flow(Item, FlowGraph, Term, Set)</code> method. This
     * method should be used when the same output <code>Item</code> from the
     * flow is to be used for all edges leaving the node.
     * 
     * @param i the <code>Item</code> to be placed in the returned
     *          <code>Map</code> as the value for every <code>EdgeKey</code> in
     *          <code>edgeKeys.</code>
     * @param edgeKeys the <code>Set</code> of <code>EdgeKey</code>s to be used
     *           as keys in the returned <code>Map</code>.
     * @return a <code>Map</code> containing a mapping from every
     *           <code>EdgeKey</code> in <code>edgeKeys</code> to the
     *           <code>Item i</code>.
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
     * Filter a list of <code>Item</code>s to contain only <code>Item</code>s
     * that are not associated with error flows, that is, only 
     * <code>Item</code>s whose associated <code>EdgeKey</code>s are not 
     * <code>FlowGraph.ExceptionEdgeKey</code>s with a type that is a subclass
     * of <code>TypeSystem.Error()</code>.
     * 
     * @param items List of Items to filter
     * @param itemKeys List of <code>EdgeKey</code>s corresponding
     *            to the edge keys for the <code>Item</code>s in <code>items</code>.
     * @return a filtered list of items, containing only those whose edge keys
     *            are not <code>FlowGraph.ExceptionEdgeKey</code>s with 
     *            whose exception types are <code>Error</code>s.
     */    
    protected final List filterItemsNonError(List items, List itemKeys) {
        List filtered = new ArrayList(items.size());
        Iterator i = items.iterator();
        Iterator j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            Item item = (Item)i.next();
            EdgeKey key = (EdgeKey)j.next();
            
            if (!(key instanceof ExceptionEdgeKey &&
               ((ExceptionEdgeKey)key).type().isSubtype(ts.Error()))) {
                // the key is not an error edge key.
                filtered.add(item);
            }
        }
        
        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("item and item key lists " +
                                            "have different sizes.");
        }
        
        return filtered;
    }
    
    /**
     * Filter a list of <code>Item</code>s to contain only <code>Item</code>s
     * that are not associated with exception flows, that is, only 
     * <code>Item</code>s whose associated <code>EdgeKey</code>s are not 
     * <code>FlowGraph.ExceptionEdgeKey</code>s.
     * 
     * @param items List of Items to filter
     * @param itemKeys List of <code>EdgeKey</code>s corresponding
     *            to the edge keys for the <code>Item</code>s in <code>items</code>.
     * @return a filtered list of items, containing only those whose edge keys
     *            are not <code>FlowGraph.ExceptionEdgeKey</code>s.
     */    
    protected final List filterItemsNonException(List items, List itemKeys) {
        List filtered = new ArrayList(items.size());
        Iterator i = items.iterator();
        Iterator j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            Item item = (Item)i.next();
            EdgeKey key = (EdgeKey)j.next();
            
            if (!(key instanceof ExceptionEdgeKey)) {
                // the key is not an exception edge key.
                filtered.add(item);
            }
        }
        
        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("item and item key lists " +
                                            "have different sizes.");
        }
        
        return filtered;
    }
 
    
    /**
     * This utility method is for subclasses to determine if the node currently
     * under consideration has both true and false edges leaving it.  That is,
     * the flow graph at this node has successor edges with the
     * <code>EdgeKey</code>s <code>Edge_KEY_TRUE</code> and
     * <code>Edge_KEY_FALSE</code>.
     * 
     * @param edgeKeys the <code>Set</code> of <code>EdgeKey</code>s of the
     * successor edges of a given node.
     * @return true if the <code>edgeKeys</code> contains both
     * <code>Edge_KEY_TRUE</code> and
     * <code>Edge_KEY_FALSE</code>
     */
    protected static final boolean hasTrueFalseBranches(Set edgeKeys) {
        return edgeKeys.contains(FlowGraph.EDGE_KEY_FALSE) &&
               edgeKeys.contains(FlowGraph.EDGE_KEY_TRUE);
    }
        
    /**
     * This utility method is meant to be used by subclasses to help them
     * produce appropriate <code>Item</code>s for the
     * <code>FlowGraph.EDGE_KEY_TRUE</code> and
     * <code>FlowGraph.EDGE_KEY_FALSE</code> edges from a boolean condition.
     * 
     * @param booleanCond the boolean condition that is used to branch on. The
     *              type of the expression must be boolean.
     * @param startingItem the <code>Item</code> at the start of the flow for
     *              the expression <code>booleanCond</code>. 
     * @param succEdgeKeys the set of <code>EdgeKeys</code> of the successor
     *              nodes of the current node. Must contain both
     *              <code>FlowGraph.EDGE_KEY_TRUE</code>
     *              and <code>FlowGraph.EDGE_KEY_FALSE</code>.
     * @param navigator an instance of <code>ConditionNavigator</code> to be
     *              used to generate appropriate <code>Item</code>s from the
     *              boolean condition.
     * @return a <code>Map</code> containing mappings for all entries in
     *              <code>succEdgeKeys</code>.
     *              <code>FlowGraph.EDGE_KEY_TRUE</code> and
     *              <code>FlowGraph.EDGE_KEY_FALSE</code> 
     *              map to <code>Item</code>s calculated for them using
     *              navigator, and all other objects in
     *              <code>succEdgeKeys</code> are mapped to
     *              <code>startingItem</code>.
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
        // FlowGraph.EDGE_KEY_TRUE and FlowGraph.EDGE_KEY_FALSE
        for (Iterator iter = succEdgeKeys.iterator(); iter.hasNext(); ) {
            EdgeKey e = (EdgeKey)iter.next();
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
        public Item trueItem() { return trueItem; }
        public Item falseItem() { return falseItem; }
        public String toString() {
            return "[ true: " + trueItem + "; false: " + falseItem + " ]";
        }
        
    }

    /**
     * A <code>ConditionNavigator</code> is used to traverse boolean
     * expressions that are
     * used as conditions, such as in if statements, while statements, 
     * left branches of && and ||. The <code>ConditionNavigator</code> is used
     * to generate
     * a finer-grained analysis, so that the branching flows from a 
     * condition can take into account the fact that the condition is true or
     * false. For example, in the statement <code>if (cond) s1 else s2</code>,
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
         * an AND boolean operator (either &amp;&amp; or &amp;).
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
    
    protected static int flowCounter = 0;
    /**
     * Dump a flow graph, labeling edges with their flows, to aid in the
     * debugging of data flow.
     */
    protected void dumpFlowGraph(FlowGraph graph, Term root) {
        String name = StringUtil.getShortNameComponent(this.getClass().getName());
        name += flowCounter++;

        String rootName = "";
        if (graph.root() instanceof CodeDecl) {
            CodeDecl cd = (CodeDecl)graph.root();
            rootName = cd.codeInstance().toString() + " in " + 
                        cd.codeInstance().container().toString();
        }


        Report.report(2, "digraph DataFlow" + name + " {");
        Report.report(2, "  label=\"Dataflow: " + name + "\\n" + rootName +
            "\"; fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        // Loop around the nodes...
        for (Iterator iter = graph.peers().iterator(); iter.hasNext(); ) {
            Peer p = (Peer)iter.next();
            
            // dump out this node
            Report.report(2,
                          p.hashCode() + " [ label = \"" +
                          StringUtil.escape(p.node.toString()) + "\\n(" + 
                          StringUtil.escape(StringUtil.getShortNameComponent(p.node.getClass().getName()))+ ")\" ];");
            
            // dump out the successors.
            for (Iterator iter2 = p.succs.iterator(); iter2.hasNext(); ) {
                Edge q = (Edge)iter2.next();
                Report.report(2,
                              q.getTarget().hashCode() + " [ label = \"" +
                              StringUtil.escape(q.getTarget().node.toString()) + " (" + 
                              StringUtil.escape(StringUtil.getShortNameComponent(q.getTarget().node.getClass().getName()))+ ")\" ];");
                String label = q.getKey().toString();
                if (p.outItems != null) {
                    label += "\\n" + p.outItems.get(q.getKey());
                }
                else {
                    label += "\\n[no dataflow available]";
                }
                Report.report(2, p.hashCode() + " -> " + q.getTarget().hashCode() + 
                              " [label=\"" + label + "\"];");
            }
            
        }
        Report.report(2, "}");
    }
}
