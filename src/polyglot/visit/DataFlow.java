/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Binary;
import polyglot.ast.CodeDecl;
import polyglot.ast.CodeNode;
import polyglot.ast.Conditional;
import polyglot.ast.Expr;
import polyglot.ast.JLang;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Term;
import polyglot.ast.Unary;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.MemberInstance;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.IdentityKey;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.StringUtil;
import polyglot.visit.FlowGraph.Edge;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.ExceptionEdgeKey;
import polyglot.visit.FlowGraph.Peer;
import polyglot.visit.FlowGraph.PeerKey;

/**
 * Abstract dataflow Visitor, to allow simple dataflow equations to be easily
 * implemented.
 */
public abstract class DataFlow<FlowItem extends DataFlow.Item> extends ErrorHandlingVisitor {
    /**
     * Indicates whether this dataflow is a forward analysis.
     */
    protected final boolean forward;

    /**
     * Indicates whether the dataflow should detect back edges. Detecting
     * back edges allows clients to use a widening operator just on confluences
     * from back edges, permitting analyses to use data flow lattices of
     * infinite height while guaranteeing termination.
     */
    protected final boolean detectBackEdges;

    /**
     * Indicates whether the dataflow should be performed on entering a
     * {@code CodeNode}, or on leaving a {@code CodeNode}.
     * If dataflow is performed on entry, then the control flow graph
     * will be available when visiting children of the
     * {@code CodeNode}, via the {@code currentFlowGraph}
     * method. If dataflow is performed on leaving, then the control
     * flow graph will not be available, but nested
     * {@code CodeNode}s will have already been processed.
     */
    protected final boolean dataflowOnEntry;

    /**
     * A stack of {@code FlowGraphSource}. The flow graph is constructed
     * upon entering a CodeNode AST node, and dataflow performed on that flow
     * graph immediately. The flow graph is available during the visiting of
     * children of the CodeNode, if subclasses want to use this information
     * to update AST nodes. The stack is maintained only if
     * {@code dataflowOnEntry} is true.
     */
    protected LinkedList<FlowGraphSource<FlowItem>> flowgraphStack;

    protected static class FlowGraphSource<FlowItem extends Item> {
        FlowGraphSource(FlowGraph<FlowItem> g, CodeDecl s) {
            this(g, (CodeNode) s);
        }

        FlowGraphSource(FlowGraph<FlowItem> g, CodeNode s) {
            flowgraph = g;
            source = s;
        }

        private FlowGraph<FlowItem> flowgraph;
        private CodeNode source;

        public FlowGraph<FlowItem> flowGraph() {
            return flowgraph;
        }

        public CodeNode source() {
            return source;
        }
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
    public DataFlow(
            Job job, TypeSystem ts, NodeFactory nf, boolean forward, boolean dataflowOnEntry) {
        this(job, ts, nf, forward, dataflowOnEntry, false);
    }

    /**
     * Constructor.
     */
    public DataFlow(
            Job job,
            TypeSystem ts,
            NodeFactory nf,
            boolean forward,
            boolean dataflowOnEntry,
            boolean detectBackEdges) {
        super(job, ts, nf);
        this.forward = forward;
        this.detectBackEdges = detectBackEdges;
        this.dataflowOnEntry = dataflowOnEntry;
        if (dataflowOnEntry) this.flowgraphStack = new LinkedList<>();
        else this.flowgraphStack = null;
    }

    @Override
    public JLang lang() {
        return (JLang) super.lang();
    }

    /**
     * An {@code Item} contains the data which flows during dataflow
     * analysis. Each node in the flow graph has two items associated with it:
     * the input item, and the output item, which results from calling
     * {@code flow} with the input item. The input item may itself be the
     * result of a call to the confluence method, if many paths flow into the
     * same node.
     *
     * NOTE: the {@code equals(Item)} method and {@code hashCode()}
     * methods must be implemented to ensure that the dataflow algorithm works
     * correctly.
     */
    public abstract static class Item {
        @Override
        public abstract boolean equals(Object i);

        @Override
        public abstract int hashCode();
    }

    /**
     * Create an initial Item for the term node. This is generally how the Item
     * that will be given to the entry peer of a graph is created, although this
     * method may also be called for other (non-start) nodes.
     *
     * @return a (possibly null) Item.
     */
    protected FlowItem createInitialItem(FlowGraph<FlowItem> graph, Peer<FlowItem> peer) {
        return createInitialItem(graph, peer.node, peer.isEntry());
    }

    /**
     * Create an initial Item for the term node. This is generally how the Item
     * that will be given to the start node of a graph is created, although this
     * method may also be called for other (non-start) nodes.
     *
     * @return a (possibly null) Item.
     */
    protected abstract FlowItem createInitialItem(
            FlowGraph<FlowItem> graph, Term node, boolean entry);

    /**
     * Produce new {@code Item}s as appropriate for the
     * {@code Peer} and the input {@code Item}s.
     *
     * @param inItems all the Items flowing into the node.
     * @param inItemKeys the FlowGraph.EdgeKeys for the items in the list inItems
     * @param graph the FlowGraph which the dataflow is operating on
     * @param peer the Peer which this method must calculate the flow for.
     * @return a Map from FlowGraph.EdgeKeys to Items. The map must have
     *          entries for all EdgeKeys in edgeKeys.
     */
    protected Map<EdgeKey, FlowItem> flow(
            FlowItem in, FlowGraph<FlowItem> graph, Peer<FlowItem> peer) {
        throw new InternalCompilerError(
                "Unimplemented: should be " + "implemented by subclasses if " + "needed");
    }

    /**
     * Produce new {@code Item}s as appropriate for the
     * {@code Peer} and the input {@code Item}s.
     * Subclasses should override if flow behavior needs to distinguish
     * between source peers.
     *
     * @param inItems all the Items flowing into the node.
     * @param inItemKeys the FlowGraph.EdgeKeys for the items in the list inItems
     * @param inItemPeers the Peers from which the Items flowed into the node
     * @param graph the FlowGraph which the dataflow is operating on
     * @param peer the Peer which this method must calculate the flow for.
     * @return a Map from FlowGraph.EdgeKeys to Items. The map must have
     *          entries for all EdgeKeys in edgeKeys.
     */
    protected Map<EdgeKey, FlowItem> flow(
            List<FlowItem> inItems,
            List<EdgeKey> inItemKeys,
            List<Peer<FlowItem>> inItemPeers,
            FlowGraph<FlowItem> graph,
            Peer<FlowItem> p) {
        if (this.detectBackEdges) {
            // determine for each inItemPeer whether it represents a back edge or not,
            // and record the results in a list.
            // Since there is an edge from inItemPeer to the current peer p, the edge
            // is a back edge if the post-order number of inItemPeer is less than
            // the post-order number of the peer p.
            List<Boolean> isBackEdges = new ArrayList<>(inItemPeers.size());

            int currentPeerOrder = postordering.get(p).intValue();

            for (Peer<FlowItem> inPeer : inItemPeers) {
                int inPeerOrder = postordering.get(inPeer).intValue();
                isBackEdges.add(Boolean.valueOf(inPeerOrder < currentPeerOrder));
            }
            return flow(inItems, inItemKeys, inItemPeers, isBackEdges, graph, p);
        }
        return flow(inItems, inItemKeys, graph, p);
    }

    /**
     *
     * @param inItems all the Items flowing into the node.
     * @param inItemKeys the FlowGraph.EdgeKeys for the items in the list inItems
     * @param inItemPeers the Peers from which the Items flowed into the node
     * @param isBackEdge list of Booleans indicating whether the edge represented
     *              by the corresponding element in inItems is a back edge or not.
     * @param graph the FlowGraph which the dataflow is operating on
     * @param peer the Peer which this method must calculate the flow for.
     * @return a Map from FlowGraph.EdgeKeys to Items. The map must have
     *          entries for all EdgeKeys in edgeKeys.
     */
    protected Map<EdgeKey, FlowItem> flow(
            List<FlowItem> inItems,
            List<EdgeKey> inItemKeys,
            List<Peer<FlowItem>> inItemPeers,
            List<Boolean> isBackEdges,
            FlowGraph<FlowItem> graph,
            Peer<FlowItem> p) {
        throw new InternalCompilerError(
                "Unimplemented. Should be implemented if a subclass wants to detect back edges. The"
                    + " standard thing to do in this method is to merge the inItems by item key,"
                    + " using the widening operator for back edges, and the standard merge operator"
                    + " otherwise.");
    }

    /**
     * Produce new {@code Item}s as appropriate for the peer's node
     * and the input {@code Item}s. The default
     * implementation of this method is simply to call {@code confluence}
     * for the list of inItems, and pass the result to flow(Item, FlowGraph,
     * Term, Set). Subclasses may want to override this method if a finer-grained
     * dataflow is required. Some subclasses may wish to override this method
     * to call {@code flowToBooleanFlow}.
     *
     * @param inItems all the Items flowing into the node.
     * @param inItemKeys the FlowGraph.EdgeKeys for the items in the list inItems
     * @param graph the FlowGraph which the dataflow is operating on
     * @param n the Term which this method must calculate the flow for.
     * @param entry indicates whether we are looking at the entry or exit of n.
     * @param edgeKeys a set of FlowGraph.EdgeKeys, being all the
     *          EdgeKeys of the edges leaving this node. The
     *          returned Map must have mappings for all objects in this set.
     * @return a Map from FlowGraph.EdgeKeys to Items. The map must have
     *          entries for all EdgeKeys in edgeKeys.
     */
    protected Map<EdgeKey, FlowItem> flow(
            List<FlowItem> inItems,
            List<EdgeKey> inItemKeys,
            FlowGraph<FlowItem> graph,
            Peer<FlowItem> peer) {
        FlowItem inItem = this.safeConfluence(inItems, inItemKeys, peer, graph);

        return this.flow(inItem, graph, peer);
    }

    /**
     * A utility method that simply collects together all the
     * TRUE items, FALSE items, and all other items (including ExceptionEdgeKey
     * items), calls {@code confluence} on each of these three collections
     * as necessary, and passes the results to
     * flow(Item, Item, Item, FlowGraph, Term, Set). It is expected that
     * this method will typically be called by subclasses overriding the
     * flow(List, List, FlowGraph, Term, Set) method, due to the need for
     * a finer-grained dataflow analysis.
     *
     * @param inItems all the Items flowing into the node.
     * @param inItemKeys the FlowGraph.EdgeKeys for the items in the list inItems
     * @param graph the FlowGraph which the dataflow is operating on
     * @param peer the Peer which this method must calculate the flow for.
     * @return a Map from FlowGraph.EdgeKeys to Items. The map must have
     *          entries for all EdgeKeys in edgeKeys.
     */
    protected final Map<EdgeKey, FlowItem> flowToBooleanFlow(
            List<FlowItem> inItems,
            List<EdgeKey> inItemKeys,
            FlowGraph<FlowItem> graph,
            Peer<FlowItem> peer) {
        List<FlowItem> trueItems = new ArrayList<>();
        List<EdgeKey> trueItemKeys = new ArrayList<>();
        List<FlowItem> falseItems = new ArrayList<>();
        List<EdgeKey> falseItemKeys = new ArrayList<>();
        List<FlowItem> otherItems = new ArrayList<>();
        List<EdgeKey> otherItemKeys = new ArrayList<>();

        Iterator<FlowItem> i = inItems.iterator();
        Iterator<EdgeKey> j = inItemKeys.iterator();
        while (i.hasNext() || j.hasNext()) {
            FlowItem item = i.next();
            EdgeKey key = j.next();

            if (FlowGraph.EDGE_KEY_TRUE.equals(key)) {
                trueItems.add(item);
                trueItemKeys.add(key);
            } else if (FlowGraph.EDGE_KEY_FALSE.equals(key)) {
                falseItems.add(item);
                falseItemKeys.add(key);
            } else {
                otherItems.add(item);
                otherItemKeys.add(key);
            }
        }

        FlowItem trueItem =
                trueItems.isEmpty()
                        ? null
                        : this.safeConfluence(trueItems, trueItemKeys, peer, graph);
        FlowItem falseItem =
                falseItems.isEmpty()
                        ? null
                        : this.safeConfluence(falseItems, falseItemKeys, peer, graph);
        FlowItem otherItem =
                otherItems.isEmpty()
                        ? null
                        : this.safeConfluence(otherItems, otherItemKeys, peer, graph);

        return this.flow(trueItem, falseItem, otherItem, graph, peer);
    }

    protected Map<EdgeKey, FlowItem> flow(
            FlowItem trueItem,
            FlowItem falseItem,
            FlowItem otherItem,
            FlowGraph<FlowItem> graph,
            Peer<FlowItem> peer) {
        throw new InternalCompilerError(
                "Unimplemented: should be " + "implemented by subclasses if " + "needed");
    }

    /**
     *
     * @param trueItem The item for flows coming into n for true conditions. Cannot be null.
     * @param falseItem The item for flows coming into n for false conditions. Cannot be null.
     * @param otherItem The item for all other flows coming into n
     * @param peer The Peer for the boolean expression.
     * @return Map from edge keys to Items. Will return null if the binary
     *      operator was not one of !, &&, ||, & or |, to allow the calling
     *      method to determine which map to use.
     */
    protected Map<EdgeKey, FlowItem> flowBooleanConditions(
            FlowItem trueItem,
            FlowItem falseItem,
            FlowItem otherItem,
            FlowGraph<FlowItem> graph,
            Peer<FlowItem> peer) {
        Expr n = (Expr) peer.node();
        Set<EdgeKey> edgeKeys = peer.succEdgeKeys();
        if (!n.type().isBoolean()
                || !(n instanceof Binary || n instanceof Conditional || n instanceof Unary)) {
            throw new InternalCompilerError(
                    "This method only takes binary, conditional, "
                            + "or unary operators of boolean type");
        }

        if (trueItem == null || falseItem == null) {
            throw new IllegalArgumentException(
                    "The trueItem and falseItem " + "for flowBooleanConditions must be non-null.");
        }

        if (n instanceof Unary) {
            Unary u = (Unary) n;
            if (u.operator() == Unary.NOT) {
                return itemsToMap(falseItem, trueItem, otherItem, edgeKeys);
            }
        } else if (n instanceof Binary) {
            Binary b = (Binary) n;
            if (b.operator() == Binary.COND_AND) {
                // the only true item coming into this node should be
                // if the second operand was true.
                return itemsToMap(trueItem, falseItem, otherItem, edgeKeys);
            } else if (b.operator() == Binary.COND_OR) {
                // the only false item coming into this node should be
                // if the second operand was false.
                return itemsToMap(trueItem, falseItem, otherItem, edgeKeys);
            } else if (b.operator() == Binary.BIT_AND) {
                // there is both a true and a false item coming into this node,
                // from the second operand. However, this operator could be false
                // if either the first or the second argument returned false.
                FlowItem bitANDFalse =
                        this.safeConfluence(
                                trueItem,
                                FlowGraph.EDGE_KEY_TRUE,
                                falseItem,
                                FlowGraph.EDGE_KEY_FALSE,
                                peer,
                                graph);
                return itemsToMap(trueItem, bitANDFalse, otherItem, edgeKeys);
            } else if (b.operator() == Binary.BIT_OR) {
                // there is both a true and a false item coming into this node,
                // from the second operand. However, this operator could be true
                // if either the first or the second argument returned true.
                FlowItem bitORTrue =
                        this.safeConfluence(
                                trueItem,
                                FlowGraph.EDGE_KEY_TRUE,
                                falseItem,
                                FlowGraph.EDGE_KEY_FALSE,
                                peer,
                                graph);
                return itemsToMap(bitORTrue, falseItem, otherItem, edgeKeys);
            }
        } else if (n instanceof Conditional) {
            return itemsToMap(trueItem, falseItem, otherItem, edgeKeys);
        }
        return null;
    }

    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items, for the confluence just before flow enters
     * node.
     *
     * @param items List of {@code Item}s that flow into {@code node}.
     *            this method will only be called if the list has at least 2
     *            elements.
     * @param peer {@code Peer} for which the {@code items} are
     *          flowing into.
     * @return a non-null Item.
     */
    protected abstract FlowItem confluence(
            List<FlowItem> items, Peer<FlowItem> peer, FlowGraph<FlowItem> graph);

    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items, for the confluence just before flow enters
     * node.
     *
     * @param items List of {@code Item}s that flow into {@code node}.
     *               This method will only be called if the list has at least 2
     *               elements.
     * @param itemKeys List of {@code FlowGraph.ExceptionEdgeKey}s for
     *              the edges that the corresponding {@code Item}s in
     *              {@code items} flowed from.
     * @param peer {@code Peer} for which the {@code items} are
     *          flowing into.
     * @return a non-null Item.
     */
    protected FlowItem confluence(
            List<FlowItem> items,
            List<EdgeKey> itemKeys,
            Peer<FlowItem> peer,
            FlowGraph<FlowItem> graph) {
        return confluence(items, peer, graph);
    }

    /**
     * The confluence operator for many flows. This method produces a single
     * Item from a List of Items, for the confluence just before flow enters
     * node.
     *
     * @param items List of {@code Item}s that flow into {@code node}.
     *               This method will only be called if the list has at least 2
     *               elements.
     * @param itemKeys List of {@code FlowGraph.ExceptionEdgeKey}s for
     *              the edges that the corresponding {@code Item}s in
     *              {@code items} flowed from.
     * @param peer {@code Peer} for which the {@code items} are
     *          flowing into.
     */
    protected FlowItem safeConfluence(
            List<FlowItem> items,
            List<EdgeKey> itemKeys,
            Peer<FlowItem> peer,
            FlowGraph<FlowItem> graph) {
        if (items.isEmpty()) {
            return this.createInitialItem(graph, peer);
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        return confluence(items, itemKeys, peer, graph);
    }

    protected FlowItem safeConfluence(
            FlowItem item1,
            FlowGraph.EdgeKey key1,
            FlowItem item2,
            FlowGraph.EdgeKey key2,
            Peer<FlowItem> peer,
            FlowGraph<FlowItem> graph) {
        return safeConfluence(item1, key1, item2, key2, null, null, peer, graph);
    }

    protected FlowItem safeConfluence(
            FlowItem item1,
            FlowGraph.EdgeKey key1,
            FlowItem item2,
            FlowGraph.EdgeKey key2,
            FlowItem item3,
            FlowGraph.EdgeKey key3,
            Peer<FlowItem> peer,
            FlowGraph<FlowItem> graph) {
        List<FlowItem> items = new ArrayList<>(3);
        List<EdgeKey> itemKeys = new ArrayList<>(3);

        if (item1 != null) {
            items.add(item1);
            itemKeys.add(key1);
        }
        if (item2 != null) {
            items.add(item2);
            itemKeys.add(key2);
        }
        if (item3 != null) {
            items.add(item3);
            itemKeys.add(key3);
        }
        return safeConfluence(items, itemKeys, peer, graph);
    }

    /**
     * Check that the term n satisfies whatever properties this
     * dataflow is checking for. Provided the method
     * check(FlowGraph, Peer) is not overridden, this
     * method is called for each term in a code declaration block
     * after the dataflow for that block of code has been performed.
     *
     * @throws SemanticException if the properties this dataflow
     *         analysis is checking for is not satisfied.
     */
    protected abstract void check(
            FlowGraph<FlowItem> graph,
            Term n,
            boolean entry,
            FlowItem inItem,
            Map<EdgeKey, FlowItem> outItems)
            throws SemanticException;

    /**
     * Check that the term n satisfies whatever properties this
     * dataflow is checking for. This method is called for each term
     * in a code declaration block after the dataflow for that block of code
     * has been performed.
     *
     * @throws SemanticException if the properties this dataflow
     *         analysis is checking for is not satisfied.
     */
    protected void check(FlowGraph<FlowItem> graph, Peer<FlowItem> p) throws SemanticException {
        check(graph, p.node(), p.isEntry(), p.inItem(), p.outItems);
    }

    /**
     * Construct a flow graph for the {@code CodeNode} provided, and call
     * {@code dataflow(FlowGraph)}. Is also responsible for calling
     * {@code post(FlowGraph, Block)} after
     * {@code dataflow(FlowGraph)} has been called, and for pushing
     * the {@code FlowGraph} onto the stack of {@code FlowGraph}s if
     * dataflow analysis is performed on entry to {@code CodeNode} nodes.
     */
    protected void dataflow(CodeDecl cd) throws SemanticException {
        this.dataflow((CodeNode) cd);
    }

    protected void dataflow(CodeNode cd) throws SemanticException {
        // only bother to do the flow analysis if the body is not null...
        if (cd.codeBody() != null) {
            // Compute the successor of each child node.
            FlowGraph<FlowItem> g = initGraph(cd, cd);

            if (g != null) {
                // Build the control flow graph.
                CFGBuilder<FlowItem> v = createCFGBuilder(ts, g);

                try {
                    v.visitGraph();
                } catch (CFGBuildError e) {
                    throw new SemanticException(e.message(), e.position());
                }

                dataflow(g);

                post(g, cd);

                // push the CFG onto the stack if we are dataflowing on entry
                if (dataflowOnEntry) flowgraphStack.addFirst(new FlowGraphSource<>(g, cd));
            }
        }
    }

    protected Frame<FlowItem> createFrame(
            Peer<FlowItem> p, boolean forward, FlowGraph<FlowItem> grahp) {
        return new Frame<>(p, forward);
    }

    /** A "stack frame" for recursive DFS */
    protected static class Frame<FlowItem extends Item> {
        protected Peer<FlowItem> peer;
        protected Iterator<Edge<FlowItem>> edges;

        protected Frame() {}

        Frame(Peer<FlowItem> p, boolean forward) {
            peer = p;
            if (forward) edges = p.succs().iterator();
            else edges = p.preds().iterator();
        }
    }

    /** Returns the linked list [by_scc, scc_head] where
     *  by_scc is an array in which SCCs occur in topologically
     *  sorted order.
     *  scc_head[n] where n is the first peer in an SCC is set to -1.
     *  scc_head[n] where n is the last peer in a (non-singleton) SCC is set
     *  to the index of the first peer. Otherwise it is -2. */
    protected Pair<Peer<FlowItem>[], int[]> findSCCs(FlowGraph<FlowItem> graph) {
        Collection<Peer<FlowItem>> peers = graph.peers();
        @SuppressWarnings("unchecked")
        Peer<FlowItem>[] sorted = new Peer[peers.size()];
        Collection<Peer<FlowItem>> start = graph.startPeers();
        // if start == peers, making all nodes reachable,
        // the problem still arises.

        // System.out.println("scc: npeers = " + peers.size());

        // First, topologically sort the nodes (put in postorder)
        int n = 0;
        LinkedList<Frame<FlowItem>> stack = new LinkedList<>();
        HashSet<Peer<FlowItem>> reachable = new HashSet<>();
        for (Peer<FlowItem> peer : start) {
            if (!reachable.contains(peer)) {
                reachable.add(peer);
                stack.addFirst(createFrame(peer, true, graph));
                while (stack.size() != 0) {
                    Frame<FlowItem> top = stack.getFirst();
                    if (top.edges.hasNext()) {
                        Edge<FlowItem> e = top.edges.next();
                        Peer<FlowItem> q = e.getTarget();
                        if (!reachable.contains(q)) {
                            reachable.add(q);
                            stack.addFirst(createFrame(q, true, graph));
                        }
                    } else {
                        stack.removeFirst();
                        sorted[n++] = top.peer;
                    }
                }
            }
        }
        // System.out.println("scc: reached " + n);
        // Now, walk the transposed graph picking nodes in reverse
        // postorder, thus picking out one SCC at a time and
        // appending it to "by_scc".
        @SuppressWarnings("unchecked")
        Peer<FlowItem>[] by_scc = new Peer[n];
        int[] scc_head = new int[n];
        HashSet<Peer<FlowItem>> visited = new HashSet<>();
        int head = 0;
        for (int i = n - 1; i >= 0; i--) {
            if (!visited.contains(sorted[i])) {
                // First, find all the nodes in the SCC
                HashSet<Peer<FlowItem>> SCC = new HashSet<>();
                visited.add(sorted[i]);
                stack.add(createFrame(sorted[i], false, graph));
                while (stack.size() != 0) {
                    Frame<FlowItem> top = stack.getFirst();
                    if (top.edges.hasNext()) {
                        Edge<FlowItem> e = top.edges.next();
                        Peer<FlowItem> q = e.getTarget();
                        if (reachable.contains(q) && !visited.contains(q)) {
                            visited.add(q);
                            Frame<FlowItem> f = createFrame(q, false, graph);
                            stack.addFirst(f);
                        }
                    } else {
                        stack.removeFirst();
                        SCC.add(top.peer);
                    }
                }
                // Now, topologically sort the SCC (as much as possible)
                // and place into by_scc[head..head+scc_size-1]
                stack.add(createFrame(sorted[i], true, graph));
                HashSet<Peer<FlowItem>> revisited = new HashSet<>();
                revisited.add(sorted[i]);
                int scc_size = SCC.size();
                int nsorted = 0;
                while (stack.size() != 0) {
                    Frame<FlowItem> top = stack.getFirst();
                    if (top.edges.hasNext()) {
                        Edge<FlowItem> e = top.edges.next();
                        Peer<FlowItem> q = e.getTarget();
                        if (SCC.contains(q) && !revisited.contains(q)) {
                            revisited.add(q);
                            Frame<FlowItem> f = createFrame(q, true, graph);
                            stack.addFirst(f);
                        }
                    } else {
                        stack.removeFirst();
                        int n3 = head + scc_size - nsorted - 1;
                        scc_head[n3] = -2;
                        by_scc[n3] = top.peer;
                        nsorted++;
                    }
                }
                scc_head[head + scc_size - 1] = head;
                scc_head[head] = -1;
                head = head + scc_size;
            }
        }
        if (Report.should_report(Report.dataflow, 2)) {
            for (int j = 0; j < n; j++) {
                switch (scc_head[j]) {
                    case -1:
                        Report.report(2, j + "[HEAD] : " + by_scc[j]);
                        break;
                    case -2:
                        Report.report(2, j + "       : " + by_scc[j]);
                        break;
                    default:
                        Report.report(2, j + " ->" + scc_head[j] + " : " + by_scc[j]);
                }
                for (Edge<FlowItem> e : by_scc[j].succs()) {
                    Report.report(3, "     successor: " + e.getTarget());
                }
            }
        }
        return new Pair<>(by_scc, scc_head);
    }

    /**
     * Map from {@code Peer}s to {@code Integer}s that contains a post-ordering
     * of {@code Peer}s if {@code this.detectBackEdges} is true.
     */
    protected Map<Peer<FlowItem>, Integer> postordering = null;

    /**
     * Create a postorder on {@code Peer p} and all {@code Peer}s
     * reachable from {@code p} (that are reachable without going through any
     * peer in the set {@code visited}). The postorder will start from
     * {@code count}.
     *
     * @param p
     * @param count
     * @param visited Set of Peer
     * @return
     */
    private int postorder(Peer<FlowItem> p, int count, Set<Peer<FlowItem>> visited) {
        if (visited.contains(p)) return count;

        // visit p
        visited.add(p);

        // visit all the successors of p
        for (Edge<FlowItem> e : p.succs()) {
            count = postorder(e.getTarget(), count, visited);
        }

        // number p
        this.postordering.put(p, Integer.valueOf(count++));

        return count;
    }

    /**
     * Perform the dataflow on flow graph {@code graph}.
     */
    protected void dataflow(FlowGraph<FlowItem> graph) {
        if (Report.should_report(Report.dataflow, 1)) {
            Report.report(1, "Finding strongly connected components");
        }
        Pair<Peer<FlowItem>[], int[]> pair = findSCCs(graph);
        Peer<FlowItem>[] by_scc = pair.part1();
        int[] scc_head = pair.part2();
        int npeers = by_scc.length;

        /* by_scc contains the peers grouped by SCC.
           scc_head marks where the SCCs are. The SCC
           begins with a -1 and ends with the index of
           the beginning of the SCC.
        */

        if (this.detectBackEdges) {
            // construct a postordering of the peers by visiting each peer in a depth first manner
            this.postordering = new HashMap<>();
            int count = 0;
            Set<Peer<FlowItem>> visited = new HashSet<>();
            for (Peer<FlowItem> p : graph.startPeers()) {
                count = postorder(p, count, visited);
            }
        }

        if (Report.should_report(Report.dataflow, 1)) {
            Report.report(1, "Iterating dataflow equations");
        }

        int current = 0;
        boolean change = false;

        while (current < npeers) {
            Peer<FlowItem> p = by_scc[current];
            if (scc_head[current] == -1) {
                change = false; // just started working on a new SCC
            }

            // get the in items by examining the out items of all
            // the predecessors of p
            List<FlowItem> inItems = new ArrayList<>(p.preds.size());
            List<EdgeKey> inItemKeys = new ArrayList<>(p.preds.size());
            List<Peer<FlowItem>> inItemPeers = new ArrayList<>(p.preds.size());
            for (Edge<FlowItem> e : p.preds) {
                Peer<FlowItem> o = e.getTarget();
                if (o.outItems != null) {
                    if (!o.outItems.keySet().contains(e.getKey())) {
                        throw new InternalCompilerError(
                                "There should have "
                                        + "an out Item with edge key "
                                        + e.getKey()
                                        + "; instead there were only "
                                        + o.outItems.keySet());
                    }
                    FlowItem it = o.outItems.get(e.getKey());
                    if (it != null) {
                        inItems.add(it);
                        inItemKeys.add(e.getKey());
                        inItemPeers.add(o);
                    }
                }
            }

            // calculate the out item
            Map<EdgeKey, FlowItem> oldOutItems = p.outItems;
            p.inItem = this.safeConfluence(inItems, inItemKeys, p, graph);
            p.outItems = this.flow(inItems, inItemKeys, inItemPeers, graph, p);

            if (!p.succEdgeKeys().equals(p.outItems.keySet())) {
                // This check is more for developers to ensure that they
                // have implemented their dataflow correctly. If performance
                // is an issue, maybe we should remove this check.
                throw new InternalCompilerError(
                        "The flow only defined "
                                + "outputs for "
                                + p.outItems.keySet()
                                + "; needs to "
                                + "define outputs for all of: "
                                + p.succEdgeKeys()
                                + " for node "
                                + p.node,
                        p.node.position());
            }

            if (oldOutItems != p.outItems
                    && (oldOutItems == null || !oldOutItems.equals(p.outItems))) {
                // the outItems of p has changed, so we will
                // loop when we get to the end of the current SCC.
                change = true;
            }
            if (change && scc_head[current] >= 0) {
                current = scc_head[current]; // loop!
                /* now scc_head[current] == -1 */
            } else {
                current++;
            }
        }
        if (Report.should_report(Report.dataflow, 1)) {
            Report.report(1, "Done.");
        }
    }

    /**
     * Initialize the {@code FlowGraph} to be used in the dataflow
     * analysis.
     *
     * @return null if no dataflow analysis should be performed for this
     *         code declaration; otherwise, an appropriately initialized
     *         {@code FlowGraph.}
     */
    protected FlowGraph<FlowItem> initGraph(CodeNode code, Term root) {
        return new FlowGraph<>(root, forward);
    }

    /**
     * Initialize the {@code FlowGraph} to be used in the dataflow
     * analysis.
     *
     * @return null if no dataflow analysis should be performed for this
     *         code declaration; otherwise, an appropriately initialized
     *         {@code FlowGraph.}
     */
    protected FlowGraph<FlowItem> initGraph(CodeDecl code, Term root) {
        return initGraph((CodeNode) code, root);
    }

    /**
     * Construct a CFGBuilder.
     *
     * @param ts The type system
     * @param g The flow graph to that the CFGBuilder will construct.
     * @return a new CFGBuilder
     */
    protected CFGBuilder<FlowItem> createCFGBuilder(TypeSystem ts, FlowGraph<FlowItem> g) {
        return new CFGBuilder<>(lang(), ts, g, this);
    }

    /**
     * Overridden superclass method, to build the flow graph, perform dataflow
     * analysis, and check the analysis for CodeNode nodes.
     */
    @Override
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (dataflowOnEntry && n instanceof CodeNode) {
            dataflow((CodeNode) n);
        }

        return this;
    }

    /**
     * Overridden superclass method, to make sure that if a subclass has changed
     * a Term, that we update the peer maps appropriately, since they are based
     * on {@code IdentityKey}s.
     */
    @Override
    public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
        if (old != n) {
            if (dataflowOnEntry && currentFlowGraph() != null) {
                // We currently only update the key in the peerMap.
                // We DO NOT update the Terms inside the peers, nor the
                // List of Terms that are the path maps.
                Map<PeerKey, Peer<FlowItem>> o =
                        currentFlowGraph().peerMap.get(new IdentityKey(old));
                if (o != null) {
                    currentFlowGraph().peerMap.put(new IdentityKey(n), o);
                }
            }
        }
        return super.leave(parent, old, n, v);
    }

    /**
     * Overridden superclass method, to pop from the stack of
     * {@code FlowGraph}s if necessary.
     */
    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof CodeNode) {
            if (!dataflowOnEntry) {
                dataflow((CodeNode) n);
            } else if (dataflowOnEntry && !flowgraphStack.isEmpty()) {
                FlowGraphSource<FlowItem> fgs = flowgraphStack.getFirst();
                if (fgs.source().equals(old)) {
                    // we are leaving the code decl that pushed this flowgraph
                    // on the stack. pop the stack.
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
    protected void post(FlowGraph<FlowItem> graph, Term root) throws SemanticException {
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }

        // Check the nodes in approximately flow order.
        Set<Peer<FlowItem>> uncheckedPeers = new HashSet<>(graph.peers());
        LinkedList<Peer<FlowItem>> peersToCheck = new LinkedList<>(graph.startPeers());
        while (!peersToCheck.isEmpty()) {
            Peer<FlowItem> p = peersToCheck.removeFirst();
            uncheckedPeers.remove(p);

            this.check(graph, p);

            for (Edge<FlowItem> e : p.succs) {
                Peer<FlowItem> q = e.getTarget();
                if (uncheckedPeers.contains(q) && !peersToCheck.contains(q)) {
                    // q hasn't been checked yet.
                    peersToCheck.addLast(q);
                }
            }

            if (peersToCheck.isEmpty() && !uncheckedPeers.isEmpty()) {
                // done all the we can reach...
                Iterator<Peer<FlowItem>> i = uncheckedPeers.iterator();
                peersToCheck.add(i.next());
                i.remove();
            }
        }
    }

    /**
     * Return the {@code FlowGraph} at the top of the stack. This method
     * should not be called if dataflow is not being performed on entry to
     * the {@code CodeNode}s, as the stack is not maintained in that case.
     * If this
     * method is called by a subclass from the {@code enterCall}
     * or {@code leaveCall} methods, for an AST node that is a child
     * of a {@code CodeNode}, then the {@code FlowGraph} returned
     * should be the {@code FlowGraph} for the dataflow for innermost
     * {@code CodeNode}.
     */
    protected FlowGraph<FlowItem> currentFlowGraph() {
        if (!dataflowOnEntry) {
            throw new InternalCompilerError(
                    "currentFlowGraph() cannot be"
                            + " called when dataflow is not performed on entry");
        }
        if (flowgraphStack.isEmpty()) {
            return null;
        }
        return flowgraphStack.getFirst().flowGraph();
    }

    /**
     * This utility method is for subclasses to convert a single Item into
     * a {@code Map}, to return from the
     * {@code flow} methods. This
     * method should be used when the same output {@code Item} from the
     * flow is to be used for all edges leaving the node.
     *
     * @param i the {@code Item} to be placed in the returned
     *          {@code Map} as the value for every {@code EdgeKey} in
     *          {@code edgeKeys.}
     * @param edgeKeys the {@code Set} of {@code EdgeKey}s to be used
     *           as keys in the returned {@code Map}.
     * @return a {@code Map} containing a mapping from every
     *           {@code EdgeKey} in {@code edgeKeys} to the
     *           {@code Item i}.
     */
    public static final <FlowItem> Map<EdgeKey, FlowItem> itemToMap(
            FlowItem i, Set<EdgeKey> edgeKeys) {
        Map<EdgeKey, FlowItem> m = new HashMap<>();
        for (EdgeKey k : edgeKeys) {
            m.put(k, i);
        }
        return m;
    }

    /**
     * This utility method is for subclasses to convert Items into
     * a {@code Map}, to return from the
     * {@code flow} methods.
     *
     * @param trueItem the {@code Item} to be placed in the returned
     *          {@code Map} as the value for the
     *          {@code FlowGraph.EDGE_KEY_TRUE}, if that key is present in
     *          {@code edgeKeys.}
     * @param falseItem the {@code Item} to be placed in the returned
     *          {@code Map} as the value for the
     *          {@code FlowGraph.EDGE_KEY_FALSE}, if that key is present in
     *          {@code edgeKeys.}
     * @param remainingItem the {@code Item} to be placed in the returned
     *          {@code Map} as the value for any edge key other than
     *          {@code FlowGraph.EDGE_KEY_TRUE} or
     *          {@code FlowGraph.EDGE_KEY_FALSE}, if any happen to be
     *          present in
     *          {@code edgeKeys.}
     * @param edgeKeys the {@code Set} of {@code EdgeKey}s to be used
     *           as keys in the returned {@code Map}.
     * @return a {@code Map} containing a mapping from every
     *           {@code EdgeKey} in {@code edgeKeys} to the
     *           {@code Item i}.
     */
    protected static final <FlowItem> Map<EdgeKey, FlowItem> itemsToMap(
            FlowItem trueItem, FlowItem falseItem, FlowItem remainingItem, Set<EdgeKey> edgeKeys) {
        Map<EdgeKey, FlowItem> m = new HashMap<>();

        for (EdgeKey k : edgeKeys) {
            if (FlowGraph.EDGE_KEY_TRUE.equals(k)) {
                m.put(k, trueItem);
            } else if (FlowGraph.EDGE_KEY_FALSE.equals(k)) {
                m.put(k, falseItem);
            } else {
                m.put(k, remainingItem);
            }
        }
        return m;
    }

    /**
     * Filter a list of {@code Item}s to contain only {@code Item}s
     * that are not associated with error flows, that is, only
     * {@code Item}s whose associated {@code EdgeKey}s are not
     * {@code FlowGraph.ExceptionEdgeKey}s with a type that is a subclass
     * of {@code TypeSystem.Error()}.
     *
     * @param items List of Items to filter
     * @param itemKeys List of {@code EdgeKey}s corresponding
     *            to the edge keys for the {@code Item}s in {@code items}.
     * @return a filtered list of items, containing only those whose edge keys
     *            are not {@code FlowGraph.ExceptionEdgeKey}s with
     *            whose exception types are {@code Error}s.
     */
    protected final List<FlowItem> filterItemsNonError(
            List<FlowItem> items, List<EdgeKey> itemKeys) {
        List<FlowItem> filtered = new ArrayList<>(items.size());
        Iterator<FlowItem> i = items.iterator();
        Iterator<EdgeKey> j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            FlowItem item = i.next();
            EdgeKey key = j.next();

            if (!(key instanceof ExceptionEdgeKey
                    && ((ExceptionEdgeKey) key).type().isSubtype(ts.Error()))) {
                // the key is not an error edge key.
                filtered.add(item);
            }
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("item and item key lists " + "have different sizes.");
        }

        return filtered;
    }

    /**
     * Filter a list of {@code Item}s to contain only {@code Item}s
     * that are not associated with exception flows, that is, only
     * {@code Item}s whose associated {@code EdgeKey}s are not
     * {@code FlowGraph.ExceptionEdgeKey}s.
     *
     * @param items List of Items to filter
     * @param itemKeys List of {@code EdgeKey}s corresponding
     *            to the edge keys for the {@code Item}s in {@code items}.
     * @return a filtered list of items, containing only those whose edge keys
     *            are not {@code FlowGraph.ExceptionEdgeKey}s.
     */
    protected final List<FlowItem> filterItemsNonException(
            List<FlowItem> items, List<EdgeKey> itemKeys) {
        List<FlowItem> filtered = new ArrayList<>(items.size());
        Iterator<FlowItem> i = items.iterator();
        Iterator<EdgeKey> j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            FlowItem item = i.next();
            EdgeKey key = j.next();

            if (!(key instanceof ExceptionEdgeKey)) {
                // the key is not an exception edge key.
                filtered.add(item);
            }
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("item and item key lists " + "have different sizes.");
        }

        return filtered;
    }

    /**
     * Filter a list of {@code Item}s to contain only {@code Item}s
     * that are associated with exception flows, whose exception is a subclass
     * of {@code excType}. That is, only
     * {@code Item}s whose associated {@code EdgeKey}s are
     * {@code FlowGraph.ExceptionEdgeKey}s, with the type a subclass
     * of {@code excType}.
     *
     * @param items List of Items to filter
     * @param itemKeys List of {@code EdgeKey}s corresponding
     *            to the edge keys for the {@code Item}s in {@code items}.
     * @param excType an Exception {@code Type}.
     * @return a filtered list of items, containing only those whose edge keys
     *            are not {@code FlowGraph.ExceptionEdgeKey}s.
     */
    protected final List<FlowItem> filterItemsExceptionSubclass(
            List<FlowItem> items, List<EdgeKey> itemKeys, Type excType) {
        List<FlowItem> filtered = new ArrayList<>(items.size());
        Iterator<FlowItem> i = items.iterator();
        Iterator<EdgeKey> j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            FlowItem item = i.next();
            EdgeKey key = j.next();

            if (key instanceof ExceptionEdgeKey) {
                // the key is an exception edge key.
                ExceptionEdgeKey eek = (ExceptionEdgeKey) key;
                if (eek.type().isImplicitCastValid(excType)) {
                    filtered.add(item);
                }
            }
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("item and item key lists " + "have different sizes.");
        }

        return filtered;
    }

    /**
     * Filter a list of {@code Item}s to contain only {@code Item}s
     * that are associated with the given {@code EdgeKey}.
     *
     * @param items List of Items to filter
     * @param itemKeys List of {@code EdgeKey}s corresponding
     *            to the edge keys for the {@code Item}s in {@code items}.
     * @param filterEdgeKey the {@code EdgeKey} to use as a filter.
     * @return a filtered list of items, containing only those whose edge keys
     *            are the same as {@code filterEdgeKey}s.
     */
    protected final List<FlowItem> filterItems(
            List<FlowItem> items, List<EdgeKey> itemKeys, FlowGraph.EdgeKey filterEdgeKey) {
        List<FlowItem> filtered = new ArrayList<>(items.size());
        Iterator<FlowItem> i = items.iterator();
        Iterator<EdgeKey> j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            FlowItem item = i.next();
            EdgeKey key = j.next();

            if (filterEdgeKey.equals(key)) {
                // the key matches the filter
                filtered.add(item);
            }
        }

        if (i.hasNext() || j.hasNext()) {
            throw new InternalCompilerError("item and item key lists " + "have different sizes.");
        }

        return filtered;
    }

    /**
     * This utility method is for subclasses to determine if the node currently
     * under consideration has both true and false edges leaving it.  That is,
     * the flow graph at this node has successor edges with the
     * {@code EdgeKey}s {@code Edge_KEY_TRUE} and
     * {@code Edge_KEY_FALSE}.
     *
     * @param edgeKeys the {@code Set} of {@code EdgeKey}s of the
     * successor edges of a given node.
     * @return true if the {@code edgeKeys} contains both
     * {@code Edge_KEY_TRUE} and
     * {@code Edge_KEY_FALSE}
     */
    protected static final boolean hasTrueFalseBranches(Set<EdgeKey> edgeKeys) {
        return edgeKeys.contains(FlowGraph.EDGE_KEY_FALSE)
                && edgeKeys.contains(FlowGraph.EDGE_KEY_TRUE);
    }

    /**
     * This utility method is meant to be used by subclasses to help them
     * produce appropriate {@code Item}s for the
     * {@code FlowGraph.EDGE_KEY_TRUE} and
     * {@code FlowGraph.EDGE_KEY_FALSE} edges from a boolean condition.
     *
     * @param booleanCond the boolean condition that is used to branch on. The
     *              type of the expression must be boolean.
     * @param startingItem the {@code Item} at the start of the flow for
     *              the expression {@code booleanCond}.
     * @param succEdgeKeys the set of {@code EdgeKeys} of the successor
     *              nodes of the current node. Must contain both
     *              {@code FlowGraph.EDGE_KEY_TRUE}
     *              and {@code FlowGraph.EDGE_KEY_FALSE}.
     * @param navigator an instance of {@code ConditionNavigator} to be
     *              used to generate appropriate {@code Item}s from the
     *              boolean condition.
     * @return a {@code Map} containing mappings for all entries in
     *              {@code succEdgeKeys}.
     *              {@code FlowGraph.EDGE_KEY_TRUE} and
     *              {@code FlowGraph.EDGE_KEY_FALSE}
     *              map to {@code Item}s calculated for them using
     *              navigator, and all other objects in
     *              {@code succEdgeKeys} are mapped to
     *              {@code startingItem}.
     * @deprecated
     */
    @Deprecated
    protected static <FlowItem extends Item> Map<EdgeKey, FlowItem> constructItemsFromCondition(
            Expr booleanCond,
            FlowItem startingItem,
            Set<EdgeKey> succEdgeKeys,
            ConditionNavigator<FlowItem> navigator) {
        // check the arguments to make sure this method is used correctly
        if (!booleanCond.type().isBoolean()) {
            throw new IllegalArgumentException("booleanCond must be a boolean expression");
        }
        if (!hasTrueFalseBranches(succEdgeKeys)) {
            throw new IllegalArgumentException(
                    "succEdgeKeys does not have true and false branches.");
        }

        BoolItem<FlowItem> results = navigator.navigate(booleanCond, startingItem);

        Map<EdgeKey, FlowItem> m = new HashMap<>();
        m.put(FlowGraph.EDGE_KEY_TRUE, results.trueItem());
        m.put(FlowGraph.EDGE_KEY_FALSE, results.falseItem());

        // put the starting item in the map for any EdgeKeys other than
        // FlowGraph.EDGE_KEY_TRUE and FlowGraph.EDGE_KEY_FALSE
        for (EdgeKey e : succEdgeKeys) {
            if (!FlowGraph.EDGE_KEY_TRUE.equals(e) && !FlowGraph.EDGE_KEY_FALSE.equals(e)) {
                m.put(e, startingItem);
            }
        }

        return m;
    }

    /**
     * This class contains two {@code Item}s, one being the
     * {@code Item} that is used when an expression is true, the
     * other being the one that is used when an expression is false. It is used
     * by the {@code ConditionNavigator}.
     * @deprecated Use flowBooleanConditions
     */
    @Deprecated
    protected static class BoolItem<FlowItem extends Item> {
        public BoolItem(FlowItem trueItem, FlowItem falseItem) {
            this.trueItem = trueItem;
            this.falseItem = falseItem;
        }

        private FlowItem trueItem;
        private FlowItem falseItem;

        public FlowItem trueItem() {
            return trueItem;
        }

        public FlowItem falseItem() {
            return falseItem;
        }

        @Override
        public String toString() {
            return "[ true: " + trueItem + "; false: " + falseItem + " ]";
        }
    }

    /**
     * A {@code ConditionNavigator} is used to traverse boolean
     * expressions that are
     * used as conditions, such as in if statements, while statements,
     * left branches of && and ||. The {@code ConditionNavigator} is used
     * to generate
     * a finer-grained analysis, so that the branching flows from a
     * condition can take into account the fact that the condition is true or
     * false. For example, in the statement {@code if (cond) s1 else s2},
     * dataflow for {@code s1} can continue in the knowledge that
     * {@code cond} evaluated to true, and similarly, {@code s2}
     * can be analyzed using the knowledge that {@code cond} evaluated to
     * false.
     *
     * @deprecated
     */
    @Deprecated
    protected abstract static class ConditionNavigator<FlowItem extends Item> {
        /**
         * Navigate the expression {@code expr}, where the
         * {@code Item} at the start of evaluating the expression is
         * {@code startingItem}.
         *
         * A {@code BoolItem} is returned, containing the
         * {@code Item}s that are appropriate when {@code expr}
         * evaluates to true and false.
         */
        public BoolItem<FlowItem> navigate(Expr expr, FlowItem startingItem) {
            if (expr.type().isBoolean()) {
                if (expr instanceof Binary) {
                    Binary b = (Binary) expr;
                    if (Binary.COND_AND.equals(b.operator())
                            || Binary.BIT_AND.equals(b.operator())) {

                        BoolItem<FlowItem> leftRes = navigate(b.left(), startingItem);
                        FlowItem rightResStart = startingItem;
                        if (Binary.COND_AND.equals(b.operator())) {
                            // due to short circuiting, if the right
                            // branch is evaluated, the starting item is
                            // in fact the true part of the left result
                            rightResStart = leftRes.trueItem();
                        }
                        BoolItem<FlowItem> rightRes = navigate(b.right(), rightResStart);
                        return andResults(leftRes, rightRes, startingItem);
                    } else if (Binary.COND_OR.equals(b.operator())
                            || Binary.BIT_OR.equals(b.operator())) {

                        BoolItem<FlowItem> leftRes = navigate(b.left(), startingItem);
                        FlowItem rightResStart = startingItem;
                        if (Binary.COND_OR.equals(b.operator())) {
                            // due to short circuiting, if the right
                            // branch is evaluated, the starting item is
                            // in fact the false part of the left result
                            rightResStart = leftRes.falseItem();
                        }
                        BoolItem<FlowItem> rightRes = navigate(b.right(), rightResStart);
                        return orResults(leftRes, rightRes, startingItem);
                    }
                } else if (expr instanceof Unary) {
                    Unary u = (Unary) expr;
                    if (Unary.NOT.equals(u.operator())) {
                        BoolItem<FlowItem> res = navigate(u.expr(), startingItem);
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
        public BoolItem<FlowItem> andResults(
                BoolItem<FlowItem> left, BoolItem<FlowItem> right, FlowItem startingItem) {
            return new BoolItem<>(combine(left.trueItem(), right.trueItem()), startingItem);
        }

        /**
         * Combine the results of analyzing the left and right arms of
         * an OR boolean operator (either || or |).
         */
        public BoolItem<FlowItem> orResults(
                BoolItem<FlowItem> left, BoolItem<FlowItem> right, FlowItem startingItem) {
            return new BoolItem<>(startingItem, combine(left.falseItem(), right.falseItem()));
        }

        /**
         * Modify the results of analyzing the child of
         * a NEGATION boolean operator (a !).
         */
        public BoolItem<FlowItem> notResult(BoolItem<FlowItem> results) {
            return new BoolItem<>(results.falseItem(), results.trueItem());
        }

        /**
         * Combine two {@code Item}s together, when the information
         * contained in both items is true. Thus, for example, in a not-null
         * analysis, where {@code Item}s are sets of not-null variables,
         * combining them corresponds to unioning the sets. Note that this
         * could be a different operation to the confluence operation.
         */
        public abstract FlowItem combine(FlowItem item1, FlowItem item2);

        /**
         * Produce a {@code BoolItem} for an expression that is not
         * a boolean operator, such as &&, &, ||, | or !.
         */
        public abstract BoolItem<FlowItem> handleExpression(Expr expr, FlowItem startingItem);
    }

    protected static int flowCounter = 0;

    /**
     * Dump a flow graph, labeling edges with their flows, to aid in the
     * debugging of data flow.
     */
    protected void dumpFlowGraph(FlowGraph<FlowItem> graph, Term root) {
        String name = StringUtil.getShortNameComponent(this.getClass().getName());
        name += flowCounter++;

        String rootName = "";
        if (graph.root() instanceof CodeNode) {
            CodeNode cd = (CodeNode) graph.root();
            rootName = cd.codeInstance().toString();
            if (cd.codeInstance() instanceof MemberInstance) {
                rootName += " in " + ((MemberInstance) cd.codeInstance()).container().toString();
            }
        }

        Report.report(2, "digraph DataFlow" + name + " {");
        Report.report(
                2,
                "  label=\"Dataflow: "
                        + name
                        + "\\n"
                        + rootName
                        + "\"; fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");

        // Loop around the nodes...
        for (Peer<FlowItem> p : graph.peers()) {
            // dump out this node
            Report.report(
                    2,
                    p.hashCode()
                            + " [ label = \""
                            + StringUtil.escape(p.node.toString())
                            + "\\n("
                            + StringUtil.escape(
                                    StringUtil.getShortNameComponent(p.node.getClass().getName()))
                            + ")"
                            + (p.path_to_finally.isEmpty()
                                    ? ""
                                    : StringUtil.escape(p.path_to_finally.toString()))
                            + "\" ];");

            // dump out the successors.
            for (Edge<FlowItem> q : p.succs) {
                Report.report(
                        2,
                        q.getTarget().hashCode()
                                + " [ label = \""
                                + StringUtil.escape(q.getTarget().node.toString())
                                + " ("
                                + StringUtil.escape(
                                        StringUtil.getShortNameComponent(
                                                q.getTarget().node.getClass().getName()))
                                + ")"
                                + (q.getTarget().path_to_finally.isEmpty()
                                        ? ""
                                        : StringUtil.escape(
                                                q.getTarget().path_to_finally.toString()))
                                + "\" ];");
                String label = q.getKey().toString();
                if (p.outItems != null) {
                    label += "\\n" + p.outItems.get(q.getKey());
                } else {
                    label += "\\n[no dataflow available]";
                }
                Report.report(
                        2,
                        p.hashCode()
                                + " -> "
                                + q.getTarget().hashCode()
                                + " [label=\""
                                + label
                                + "\"];");
            }
        }
        Report.report(2, "}");
    }
}
// vim: ts=4 sw=4
