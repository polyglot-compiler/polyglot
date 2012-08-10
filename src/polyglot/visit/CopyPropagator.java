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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.Expr;
import polyglot.ast.For;
import polyglot.ast.If;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Loop;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.ast.Unary;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;

/**
 * Visitor which performs copy propagation.
 */
public class CopyPropagator extends DataFlow<CopyPropagator.DataFlowItem> {
    public CopyPropagator(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf, true /* forward analysis */, true /* perform dataflow on entry to CodeDecls */);
    }

    protected static class DataFlowItem extends DataFlow.Item {
        // Map of LocalInstance -> CopyInfo.  The CopyInfo nodes form a forest
        // to represent copy information.
        private Map<LocalInstance, CopyInfo> map;

        /**
         * Constructor for creating an empty set.
         */
        protected DataFlowItem() {
            this.map = new HashMap<LocalInstance, CopyInfo>();
        }

        /**
         * Deep copy constructor.
         */
        protected DataFlowItem(DataFlowItem dfi) {
            map = new HashMap<LocalInstance, CopyInfo>(dfi.map.size());
            for (Map.Entry<LocalInstance, CopyInfo> e : dfi.map.entrySet()) {
                LocalInstance li = e.getKey();
                CopyInfo ci = e.getValue();
                if (ci.from != null) add(ci.from.li, li);
            }
        }

        protected static class CopyInfo {
            final public LocalInstance li; // Local instance this node pertains to.
            public CopyInfo from; // In edge.
            public Set<CopyInfo> to; // Out edges.
            public CopyInfo root; // Root CopyInfo node for this tree.

            protected CopyInfo(LocalInstance li) {
                if (li == null) {
                    throw new InternalCompilerError("Null local instance "
                            + "encountered during copy propagation.");
                }

                this.li = li;
                this.from = null;
                this.to = new HashSet<CopyInfo>();
                this.root = this;
            }

            protected void setRoot(CopyInfo root) {
                List<CopyInfo> worklist = new ArrayList<CopyInfo>();
                worklist.add(this);
                while (worklist.size() > 0) {
                    CopyInfo ci = worklist.remove(worklist.size() - 1);
                    worklist.addAll(ci.to);
                    ci.root = root;
                }
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof CopyInfo)) return false;
                CopyInfo ci = (CopyInfo) o;

                // Assume both are in consistent data structures, so only check
                // up pointers.  Also check root pointers because we can.
                return li == ci.li
                        && (from == null ? ci.from == null
                                : (ci.from != null && from.li == ci.from.li))
                        && root.li == ci.root.li;
            }

            @Override
            public int hashCode() {
                return li.hashCode()
                        + 31
                        * (from == null ? 0 : from.li.hashCode() + 31
                                * root.li.hashCode());
            }
        }

        protected void add(LocalInstance from, LocalInstance to) {
            // Get the 'to' node.
            boolean newTo = !map.containsKey(to);
            CopyInfo ciTo;
            if (newTo) {
                ciTo = new CopyInfo(to);
                map.put(to, ciTo);
            }
            else {
                ciTo = map.get(to);
            }

            // Get the 'from' node.
            CopyInfo ciFrom;
            if (map.containsKey(from)) {
                ciFrom = map.get(from);
            }
            else {
                ciFrom = new CopyInfo(from);
                map.put(from, ciFrom);
                ciFrom.root = ciFrom;
            }

            // Make sure ciTo doesn't already have a 'from' node.
            if (ciTo.from != null) {
                throw new InternalCompilerError("Error while copying dataflow "
                        + "item during copy propagation.");
            }

            // Link up.
            ciFrom.to.add(ciTo);
            ciTo.from = ciFrom;

            // Consistency fix-up.
            if (newTo) {
                ciTo.root = ciFrom.root;
            }
            else {
                ciTo.setRoot(ciFrom.root);
            }
        }

        protected void intersect(DataFlowItem dfi) {
            boolean modified = false;

            for (Iterator<Map.Entry<LocalInstance, CopyInfo>> it =
                    map.entrySet().iterator(); it.hasNext();) {
                Map.Entry<LocalInstance, CopyInfo> e = it.next();
                LocalInstance li = e.getKey();
                CopyInfo ci = e.getValue();

                if (!dfi.map.containsKey(li)) {
                    modified = true;

                    it.remove();

                    // Surgery.  Bypass and remove the node.  We'll fix
                    // consistency later.
                    if (ci.from != null) ci.from.to.remove(ci);
                    for (CopyInfo toCI : ci.to) {
                        toCI.from = null;
                    }

                    continue;
                }

                if (ci.from == null) continue;

                // Other DFI contains this key.
                // Make sure that ci and ci.from are also in the same tree in
                // the other DFI.  If not, break the link in the intersection
                // result.
                CopyInfo otherCI = dfi.map.get(li);
                CopyInfo otherCIfrom = dfi.map.get(ci.from.li);

                if (otherCIfrom == null || otherCI.root != otherCIfrom.root) {
                    modified = true;

                    // Remove the uplink.
                    ci.from.to.remove(ci);
                    ci.from = null;
                }
            }

            if (!modified) return;

            // Fix consistency.
            for (Iterator<Map.Entry<LocalInstance, CopyInfo>> it =
                    map.entrySet().iterator(); it.hasNext();) {
                Entry<LocalInstance, CopyInfo> e = it.next();
                CopyInfo ci = e.getValue();

                // Only work on roots.
                if (ci.from != null) continue;

                // Cut out singleton nodes.
                if (ci.to.isEmpty()) {
                    it.remove();
                    continue;
                }

                // Fix root.
                ci.setRoot(ci);
            }
        }

        public void kill(LocalInstance var) {
            if (!map.containsKey(var)) return;

            CopyInfo ci = map.get(var);
            map.remove(var);

            // Splice out 'ci' and fix consistency.
            if (ci.from != null) ci.from.to.remove(ci);
            for (CopyInfo toCI : ci.to) {
                toCI.from = ci.from;
                if (ci.from == null) {
                    toCI.setRoot(toCI);
                }
                else {
                    ci.from.to.add(toCI);
                }
            }
        }

        public LocalInstance getRoot(LocalInstance var) {
            if (!map.containsKey(var)) return null;
            return map.get(var).root.li;
        }

        private void die() {
            throw new InternalCompilerError("Copy propagation dataflow item "
                    + "consistency error.");
        }

        @SuppressWarnings("unused")
        private void consistencyCheck() {
            for (Map.Entry<LocalInstance, CopyInfo> e : map.entrySet()) {
                LocalInstance li = e.getKey();
                CopyInfo ci = e.getValue();

                if (li != ci.li) die();
                if (!map.containsKey(ci.root.li)) die();
                if (map.get(ci.root.li) != ci.root) die();

                if (ci.from == null) {
                    if (ci.root != ci) die();
                }
                else {
                    if (!map.containsKey(ci.from.li)) die();
                    if (map.get(ci.from.li) != ci.from) die();
                    if (ci.from.root != ci.root) die();
                    if (!ci.from.to.contains(ci)) die();
                }

                for (CopyInfo toCI : ci.to) {
                    if (!map.containsKey(toCI.li)) die();
                    if (map.get(toCI.li) != toCI) die();
                    if (toCI.root != ci.root) die();
                    if (toCI.from != ci) die();
                }
            }
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (Entry<LocalInstance, CopyInfo> e : map.entrySet()) {
                result = 31 * result + e.getKey().hashCode();
                result = 31 * result + e.getValue().hashCode();
            }

            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DataFlowItem)) return false;

            DataFlowItem dfi = (DataFlowItem) o;
            return map.equals(dfi.map);
        }

        @Override
        public String toString() {
            String result = "";
            boolean first = true;

            for (CopyInfo ci : map.values()) {
                if (ci.from != null) {
                    if (!first) result += ", ";
                    if (ci.root != ci.from) result += ci.root.li + " ->* ";
                    result += ci.from.li + " -> " + ci.li;
                    first = false;
                }
            }
            return "[" + result + "]";
        }
    }

    @Override
    public DataFlowItem createInitialItem(FlowGraph<DataFlowItem> graph,
            Term node, boolean entry) {
        return new DataFlowItem();
    }

    @Override
    public DataFlowItem confluence(List<DataFlowItem> inItems, Term node,
            boolean entry, FlowGraph<DataFlowItem> graph) {
        DataFlowItem result = null;
        for (DataFlowItem inItem : inItems) {
            if (result == null) {
                result = new DataFlowItem(inItem);
            }
            else {
                result.intersect(inItem);
            }
        }

        return result;
    }

    private void killDecl(DataFlowItem dfi, Stmt stmt) {
        if (stmt instanceof LocalDecl) {
            dfi.kill(((LocalDecl) stmt).localInstance());
        }
    }

    protected DataFlowItem flow(DataFlowItem in, FlowGraph<DataFlowItem> graph,
            Term t, boolean entry) {
        DataFlowItem result = new DataFlowItem(in);

        if (entry) {
            return result;
        }

        if (t instanceof Assign) {
            Assign n = (Assign) t;
            Assign.Operator op = n.operator();
            Expr left = n.left();
            Expr right = n.right();

            if (left instanceof Local) {
                LocalInstance to = ((Local) left).localInstance().orig();
                result.kill(to);

                if (right instanceof Local && op == Assign.ASSIGN) {
                    LocalInstance from = ((Local) right).localInstance().orig();
                    result.add(from, to);
                }
            }
        }
        else if (t instanceof Unary) {
            Unary n = (Unary) t;
            Unary.Operator op = n.operator();
            Expr expr = n.expr();

            if (expr instanceof Local
                    && (op == Unary.POST_INC || op == Unary.POST_DEC
                            || op == Unary.PRE_INC || op == Unary.PRE_DEC)) {

                result.kill(((Local) expr).localInstance().orig());
            }
        }
        else if (t instanceof LocalDecl) {
            LocalDecl n = (LocalDecl) t;

            LocalInstance to = n.localInstance();
            result.kill(to);

            // It's a copy if we're initializing a non-final local declaration
            // with a value from a local variable.  We only care about
            // non-final local declarations because final locals have special
            // use in local classes.
            if (!n.flags().isFinal() && n.init() instanceof Local) {
                LocalInstance from = ((Local) n.init()).localInstance().orig();
                result.add(from, to);
            }
        }
        else if (t instanceof Block) {
            // Kill locals that were declared in the block.
            Block n = (Block) t;
            for (Stmt stmt : n.statements()) {
                killDecl(result, stmt);
            }
        }
        else if (t instanceof Loop) {
            if (t instanceof For) {
                // Kill locals that were declared in the initializers.
                For n = (For) t;
                for (Stmt stmt : n.inits()) {
                    killDecl(result, stmt);
                }
            }

            // Kill locals that were declared in the body.
            killDecl(result, ((Loop) t).body());
        }
        else if (t instanceof Catch) {
            // Kill catch's formal.
            result.kill(((Catch) t).formal().localInstance());
        }
        else if (t instanceof If) {
            // Kill locals declared in consequent and alternative.
            If n = (If) t;
            killDecl(result, n.consequent());
            killDecl(result, n.alternative());
        }

        return result;
    }

    @Override
    public Map<EdgeKey, DataFlowItem> flow(DataFlowItem in,
            FlowGraph<DataFlowItem> graph, Term t, boolean entry,
            Set<EdgeKey> succEdgeKeys) {
        return itemToMap(flow(in, graph, t, entry), succEdgeKeys);
    }

    @Override
    public void post(FlowGraph<DataFlowItem> graph, Term root)
            throws SemanticException {
        // No need to do any checking.
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }
    }

    @Override
    public void check(FlowGraph<DataFlowItem> graph, Term n, boolean entry,
            DataFlowItem inItem, Map<EdgeKey, DataFlowItem> outItems)
            throws SemanticException {

        throw new InternalCompilerError("CopyPropagator.check should never be "
                + "called.");
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        if (n instanceof Local) {
            FlowGraph<DataFlowItem> g = currentFlowGraph();
            if (g == null) return n;

            Local l = (Local) n;
            Collection<Peer<DataFlowItem>> peers = g.peers(l, Term.EXIT);
            if (peers == null || peers.isEmpty()) return n;

            List<DataFlowItem> items = new ArrayList<DataFlowItem>();
            for (Peer<DataFlowItem> p : peers) {
                if (p.inItem() != null) items.add(p.inItem());
            }

            DataFlowItem in = confluence(items, l, false, g);
            if (in == null) return n;

            LocalInstance root = in.getRoot(l.localInstance().orig());
            if (root == null) return n;
            return l.name(root.name()).localInstance(root);
        }

        if (n instanceof Unary) {
            return old;
        }

        if (n instanceof Assign) {
            Assign oldAssign = (Assign) old;
            Assign newAssign = (Assign) n;
            return newAssign.left(oldAssign.left());
        }

        return n;
    }
}
