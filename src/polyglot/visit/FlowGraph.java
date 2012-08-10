/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.types.Type;
import polyglot.util.CollectionUtil;
import polyglot.util.IdentityKey;

public class FlowGraph<FlowItem extends DataFlow.Item> {

    /**
     * Maps from AST nodes to path maps and hence to <code>Peer</code>s that
     * represent occurrences of the AST node in the flow graph. In particular,
     * <code>peerMap</code> maps <code>IdentityKey(Node)</code>s to path
     * maps. A path map is a map from paths (<code>ListKey(List of Terms)</code>)
     * to <code>Peer</code>s. In particular, if <code>n</code> is an AST
     * node in a finally block, then there will be a <code>Peer</code> of
     * <code>n</code> for each possible path to the finally block, and the
     * path map records which <code>Peer</code> corresponds to which path. If
     * <code>n</code> does not occur in a finally block, then the path map
     * should have only a single entry, from an empty list to the unique
     * <code>Peer</code> for <code>n</code>.
     * 
     * <p>
     * <b>WARNING</b>: the AST must be a tree, not a DAG. Otherwise the same
     * peer may be used for a node that appears at multiple points in the AST.
     * These points may have different data flows.
     * </p>
     */
    protected Map<IdentityKey, Map<PeerKey, Peer<FlowItem>>> peerMap;

    /**
     * The root of the AST that this is a flow graph for.
     */
    protected Term root;

    /**
     * Is the flow in this flow graph forward or backward?
     */
    protected boolean forward;

    FlowGraph(Term root, boolean forward) {
        this.root = root;
        this.forward = forward;
        this.peerMap = new HashMap<IdentityKey, Map<PeerKey, Peer<FlowItem>>>();
    }

    public Term root() {
        return root;
    }

    public boolean forward() {
        return forward;
    }

    public Collection<Peer<FlowItem>> entryPeers() {
        return peers(root, Term.ENTRY);
    }

    public Collection<Peer<FlowItem>> exitPeers() {
        return peers(root, Term.EXIT);
    }

    public Collection<Peer<FlowItem>> startPeers() {
        return forward ? entryPeers() : exitPeers();
    }

    public Collection<Peer<FlowItem>> finishPeers() {
        return forward ? exitPeers() : entryPeers();
    }

    public Collection<Map<PeerKey, Peer<FlowItem>>> pathMaps() {
        return peerMap.values();
    }

    public Map<PeerKey, Peer<FlowItem>> pathMap(Node n) {
        return peerMap.get(new IdentityKey(n));
    }

    /**
     * Return a collection of all <code>Peer</code>s in this flow graph.
     */
    public Collection<Peer<FlowItem>> peers() {
        Collection<Peer<FlowItem>> c = new ArrayList<Peer<FlowItem>>();
        for (Map<PeerKey, Peer<FlowItem>> m : peerMap.values()) {
            for (Peer<FlowItem> p : m.values()) {
                c.add(p);
            }
        }
        return c;
    }

    /**
     * Retrieve the entry or exit <code>Peer</code> for the
     * <code>Term n</code>, where <code>n</code> does not appear in a
     * finally block. If no such Peer exists, then one will be created.
     * 
     * <code>entry</code> can be Term.ENTRY or Term.EXIT.
     */
    public Peer<FlowItem> peer(Term n, int entry) {
        return peer(n, Collections.<Term> emptyList(), entry);
    }

    /**
     * Return a collection of all of the entry or exit <code>Peer</code>s for
     * the given <code>Term n</code>.
     * 
     * <code>entry</code> can be Term.ENTRY or Term.EXIT.
     */
    public Collection<Peer<FlowItem>> peers(Term n, int entry) {
        IdentityKey k = new IdentityKey(n);
        Map<PeerKey, Peer<FlowItem>> pathMap = peerMap.get(k);

        if (pathMap == null) {
            return Collections.emptyList();
        }

        Collection<Peer<FlowItem>> peers = pathMap.values();
        List<Peer<FlowItem>> l = new ArrayList<Peer<FlowItem>>(peers.size());

        for (Peer<FlowItem> p : peers) {
            if (p.entry == entry) {
                l.add(p);
            }
        }

        return l;
    }

    /**
     * Retrieve the <code>Peer</code> for the <code>Term n</code> that is
     * associated with the given path to the finally block. (A term that occurs
     * in a finally block has one Peer for each possible path to that finally
     * block.) If no such Peer exists, then one will be created.
     * 
     * <code>entry</code> can be Term.ENTRY or Term.EXIT.
     */
    public Peer<FlowItem> peer(Term n, List<Term> path_to_finally, int entry) {
        PeerKey lk = new PeerKey(path_to_finally, entry);
        return peer(n, lk);
    }

    /**
     * Retrieve the <code>Peer</code> for the <code>Term n</code> that is
     * associated with the given PeerKey.
     */
    public Peer<FlowItem> peer(Term n, PeerKey peerKey) {
        IdentityKey k = new IdentityKey(n);
        Map<PeerKey, Peer<FlowItem>> pathMap = peerMap.get(k);

        if (pathMap == null) {
            pathMap = new HashMap<PeerKey, Peer<FlowItem>>();
            peerMap.put(k, pathMap);
        }

        Peer<FlowItem> p = pathMap.get(peerKey);

        if (p == null) {
            p = new Peer<FlowItem>(n, peerKey.list, peerKey.entry);
            pathMap.put(peerKey, p);
        }

        return p;
    }

    /**
     * This class provides an identifying label for edges in the flow graph.
     * Thus, the condition of an if statement will have at least two edges
     * leaving it (in a forward flow graph): one will have the EdgeKey
     * FlowGraph.EDGE_KEY_TRUE, and is the flow that is taken when the condition
     * evaluates to true, and one will have the EdgeKey FlowGraph.EDGE_KEY_FALSE,
     * and is the flow that is taken when the condition evaluates to false. 
     * 
     * The differentiation of the flow graph edges allows for a finer grain
     * data flow analysis, as the dataflow equations can incorporate the 
     * knowledge that a condition is true or false on certain flow paths.
     */
    public static class EdgeKey {
        protected Object o;

        protected EdgeKey(Object o) {
            this.o = o;
        }

        @Override
        public int hashCode() {
            return o.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof EdgeKey)
                    && (((EdgeKey) other).o.equals(this.o));
        }

        @Override
        public String toString() {
            return o.toString();
        }
    }

    /**
     * This class extends EdgeKey and is the key for edges that are
     * taken when an exception of type t is thrown. Thus, the flow from
     * line 2 in the example below to the catch block (line 4) would have an
     * ExceptionEdgeKey constructed with the Type representing 
     * NullPointerExceptions.
     * 
     * <pre>
     * ...
     * try {                                      // line 1
     *   o.foo();                                 // line 2
     * }                                          // line 3
     * catch (NullPointerException e) {           // line 4
     *   ...
     * }
     * ...
     * </pre>
     */
    public static class ExceptionEdgeKey extends EdgeKey {
        public ExceptionEdgeKey(Type t) {
            super(t);
        }

        public Type type() {
            return (Type) o;
        }

        @Override
        public String toString() {
            return (type().isClass() ? type().toClass().name()
                    : type().toString());
        }
    }

    /**
     * This EdgeKey is the EdgeKey for edges where the expression evaluates
     * to true.
     */
    public static final EdgeKey EDGE_KEY_TRUE = new EdgeKey("true");

    /**
     * This EdgeKey is the EdgeKey for edges where the expression evaluates
     * to false.
     */
    public static final EdgeKey EDGE_KEY_FALSE = new EdgeKey("false");

    /**
     * This EdgeKey is the EdgeKey for edges where the flow is not suitable 
     * for EDGE_KEY_TRUE, EDGE_KEY_FALSE or an 
     * ExceptionEdgeKey, such as the edges from a switch
     * statement to its cases and
     * the flow from a sink node in the control flow graph.
     */
    public static final EdgeKey EDGE_KEY_OTHER = new EdgeKey("other");

    /**
     * This class represents an edge in the flow graph. The target of the edge
     * is either the head or the tail of the edge, depending on how the Edge is 
     * used. Thus, the target field in Edges in the collection Peer.preds is the
     * source Peer, while the target field in Edges in the collection Peer.succs 
     * is the destination Peer of edges.
     * 
     * Each Edge has an EdgeKey, which identifies when flow uses that edge in 
     * the flow graph. See EdgeKey for more information.
     */
    public static class Edge<FlowItem extends DataFlow.Item> {
        public Edge(EdgeKey key, Peer<FlowItem> target) {
            this.key = key;
            this.target = target;
        }

        public EdgeKey getKey() {
            return key;
        }

        public Peer<FlowItem> getTarget() {
            return target;
        }

        protected EdgeKey key;
        protected Peer<FlowItem> target;

        @Override
        public String toString() {
            return "(" + key + ")" + target;
        }

    }

    /**
     * A <code>Peer</code> is an occurrence of an AST node in a flow graph. 
     * For most AST nodes, there will be only one Peer for each AST node. 
     * However, if the AST node occurs in a finally block, then there will be
     * multiple <code>Peer</code>s for that AST node, one for each possible
     * path to the finally block. This is because flow graphs for finally blocks 
     * are copied, one copy for each possible path to the finally block.
     */
    public static class Peer<FlowItem extends DataFlow.Item> {
        protected FlowItem inItem; // Input Item for dataflow analysis
        protected Map<EdgeKey, FlowItem> outItems; // Output Items for dataflow analysis, a map from EdgeKeys to DataFlowlItems
        protected Term node; // The AST node that this peer is an occurrence of.
        protected List<Edge<FlowItem>> succs; // List of successor Edges 
        protected List<Edge<FlowItem>> preds; // List of predecessor Edges 
        /**
         * the path to the finally block that uniquely distinguishes this Peer
         * from the other Peers for the AST node. See documentation for CFGBuilder
         * for more information on the contents on path_to_finally.
         */
        protected List<Term> path_to_finally;

        protected int entry; // Term.ENTRY or Term.EXIT

        /**
         * Set of all the different EdgeKeys that occur in the Edges in the 
         * succs. This Set is lazily constructed, as needed, by the 
         * method succEdgeKeys()
         */
        private Set<EdgeKey> succEdgeKeys;

        public Peer(Term node, List<Term> path_to_finally, int entry) {
            this.node = node;
            this.path_to_finally = path_to_finally;
            this.inItem = null;
            this.outItems = null;
            this.succs = new ArrayList<Edge<FlowItem>>();
            this.preds = new ArrayList<Edge<FlowItem>>();
            this.entry = entry;
            this.succEdgeKeys = null;
        }

        /** The successor Edges. */
        public List<Edge<FlowItem>> succs() {
            return succs;
        }

        /** The predecessor Edges. */
        public List<Edge<FlowItem>> preds() {
            return preds;
        }

        /** The node for which this is a peer. */
        public Term node() {
            return node;
        }

        /** Create a PeerKey for this Peer */
        public PeerKey peerKey() {
            return new PeerKey(path_to_finally, entry);
        }

        /**
         * The input data flow item.  Should only be called
         * after data flow analysis is performed.
         */
        public FlowItem inItem() {
            return inItem;
        }

        /**
         * The output item for a particular EdgeKey.  Should only be called
         * after data flow analysis is performed.
         */
        public FlowItem outItem(EdgeKey key) {
            if (outItems == null) return null;
            return outItems.get(key);
        }

        @Override
        public String toString() {
            return (entry == Term.ENTRY ? "entry: " : "") + node
                    + path_to_finally;
        }

        public boolean isEntry() {
            return entry == Term.ENTRY;
        }

        public Set<EdgeKey> succEdgeKeys() {
            if (this.succEdgeKeys == null) {
                // the successor edge keys have not yet been calculated. do it
                // now.
                this.succEdgeKeys = new HashSet<EdgeKey>();
                for (Edge<FlowItem> e : this.succs) {
                    this.succEdgeKeys.add(e.getKey());
                }
                if (this.succEdgeKeys.isEmpty()) {
                    // There are no successors for this node. Add in the OTHER
                    // edge key, so that there is something to map the output
                    // item from...
                    this.succEdgeKeys.add(FlowGraph.EDGE_KEY_OTHER);
                }
            }
            return this.succEdgeKeys;
        }
    }

    /**
     * Class to be used for inserting Lists in hashtables using collection
     * equality (as defined in
     * {@link polyglot.util.CollectionUtil CollectionUtil}).
     */
    public static class PeerKey {

        protected final List<Term> list;
        protected final int entry;

        public PeerKey(List<Term> list, int entry) {
            this.list = list;
            this.entry = entry;
        }

        @Override
        public int hashCode() {
            return list.hashCode() ^ entry;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PeerKey) {
                PeerKey k = (PeerKey) other;
                return CollectionUtil.equals(list, k.list) && entry == k.entry;
            }
            else {
                return false;
            }
        }

    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        Set<Peer<FlowItem>> todo = new HashSet<Peer<FlowItem>>(this.peers());
        LinkedList<Peer<FlowItem>> queue =
                new LinkedList<Peer<FlowItem>>(startPeers());

        while (!queue.isEmpty()) {
            Peer<FlowItem> p = queue.removeFirst();
            todo.remove(p);
//        sb.append(StringUtil.getShortNameComponent(p.node.getClass().getName()) + " ["+p.node+"]" + "\n");
            sb.append(p.node + " (" + p.node.position() + ")\n");
            for (Edge<FlowItem> e : p.succs) {
                Peer<FlowItem> q = e.getTarget();
                sb.append("    -> " + q.node + " (" + q.node.position() + ")\n");
                //sb.append("  " + StringUtil.getShortNameComponent(q.node.getClass().getName()) + " ["+q.node+"]" + "\n");
                if (todo.contains(q) && !queue.contains(q)) {
                    queue.addLast(q);
                }
            }

            if (queue.isEmpty() && !todo.isEmpty()) {
                sb.append("\n\n***UNREACHABLE***\n");
                queue.addAll(todo);
                todo = Collections.emptySet();
            }
        }

        return sb.toString();
    }
}
