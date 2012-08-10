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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Block;
import polyglot.ast.CompoundStmt;
import polyglot.ast.Initializer;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Stmt;
import polyglot.ast.Term;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;

/**
 * Visitor which checks that all statements must be reachable
 */
public class ReachChecker extends DataFlow<ReachChecker.DataFlowItem> {
    public ReachChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf, true /* forward analysis */, true /* perform dataflow on entry to CodeDecls */);
    }

    protected static class DataFlowItem extends DataFlow.Item {
        public final boolean reachable;
        public final boolean normalReachable;

        protected DataFlowItem(boolean reachable, boolean normalReachable) {
            this.reachable = reachable;
            this.normalReachable = normalReachable;
        }

        // terms that are reachable through normal control flow
        public static final DataFlowItem REACHABLE = new DataFlowItem(true,
                                                                      true);

        // terms that are reachable only through exception control flow, but
        // not by normal control flow. 
        public static final DataFlowItem REACHABLE_EX_ONLY =
                new DataFlowItem(true, false);

        // terms that are not reachable 
        public static final DataFlowItem NOT_REACHABLE =
                new DataFlowItem(false, false);

        @Override
        public String toString() {
            return (reachable ? "" : "not ") + "reachable"
                    + (normalReachable ? "" : " by exceptions only");
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.reachable == ((DataFlowItem) o).reachable
                        && this.normalReachable == ((DataFlowItem) o).normalReachable;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (reachable ? 5423 : 5753) + (normalReachable ? 31 : -2);
        }
    }

    @Override
    public DataFlowItem createInitialItem(FlowGraph<DataFlowItem> graph,
            Term node, boolean entry) {
        if (node == graph.root() && entry) {
            return DataFlowItem.REACHABLE;
        }
        else {
            return DataFlowItem.NOT_REACHABLE;
        }
    }

    @Override
    public Map<EdgeKey, DataFlowItem> flow(DataFlowItem in,
            FlowGraph<DataFlowItem> graph, Term n, boolean entry,
            Set<EdgeKey> succEdgeKeys) {
        if (in == DataFlowItem.NOT_REACHABLE) {
            return itemToMap(in, succEdgeKeys);
        }

        // in is either REACHABLE or REACHABLE_EX_ONLY.
        // return a map where all exception edges are REACHABLE_EX_ONLY,
        // and all non-exception edges are REACHABLE.
        Map<EdgeKey, DataFlowItem> m =
                itemToMap(DataFlowItem.REACHABLE_EX_ONLY, succEdgeKeys);

        if (succEdgeKeys.contains(FlowGraph.EDGE_KEY_OTHER)) {
            m.put(FlowGraph.EDGE_KEY_OTHER, DataFlowItem.REACHABLE);
        }
        if (succEdgeKeys.contains(FlowGraph.EDGE_KEY_TRUE)) {
            m.put(FlowGraph.EDGE_KEY_TRUE, DataFlowItem.REACHABLE);
        }
        if (succEdgeKeys.contains(FlowGraph.EDGE_KEY_FALSE)) {
            m.put(FlowGraph.EDGE_KEY_FALSE, DataFlowItem.REACHABLE);
        }

        return m;
    }

    @Override
    public DataFlowItem confluence(List<DataFlowItem> inItems, Term node,
            boolean entry, FlowGraph<DataFlowItem> graph) {
        throw new InternalCompilerError("Should never be called.");
    }

    @Override
    public DataFlowItem confluence(List<DataFlowItem> inItems,
            List<EdgeKey> itemKeys, Term node, boolean entry,
            FlowGraph<DataFlowItem> graph) {
        // if any predecessor is reachable, so is this one, and if any
        // predecessor is normal reachable, and the edge key is not an 
        // exception edge key, then so is this one.

        List<DataFlowItem> l = this.filterItemsNonException(inItems, itemKeys);
        for (DataFlowItem i : l) {
            if (i == DataFlowItem.REACHABLE) {
                // this term is reachable via a non-exception edge
                return DataFlowItem.REACHABLE;
            }
        }

        // If we fall through to here, then there were
        // no non-exception edges that were normally reachable.        
        // We now need to determine if this node is
        // reachable via an exception edge key, or if 
        // it is not reachable at all.
        for (DataFlowItem i : inItems) {
            if (i.reachable) {
                // this term is reachable, but only through an
                // exception edge.
                return DataFlowItem.REACHABLE_EX_ONLY;
            }
        }

        return DataFlowItem.NOT_REACHABLE;
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        // check for reachability.
        if (n instanceof Term) {
            n = checkReachability((Term) n);
            if (!((Term) n).reachable()) {
                // Do we throw an exception or not?

                // Compound statements are allowed to be unreachable
                // (e.g., "{ return; }" or "while (true) S").  If a compound
                // statement is truly unreachable, one of its sub-statements
                // will be also and we will report an error there.

                if ((n instanceof Block && ((Block) n).statements().isEmpty())
                        || (n instanceof Stmt && !(n instanceof CompoundStmt))) {
                    throw new SemanticException("Unreachable statement.",
                                                n.position());
                }
            }

        }

        return super.leaveCall(old, n, v);
    }

    protected Node checkReachability(Term n) throws SemanticException {
        FlowGraph<DataFlowItem> g = currentFlowGraph();
        if (g != null) {
            Collection<Peer<DataFlowItem>> peers = g.peers(n, Term.EXIT);
            if (peers != null && !peers.isEmpty()) {
                boolean isReachable = false;
                boolean isNormalReachable = false;

                for (Peer<DataFlowItem> p : peers) {
                    // the peer is reachable if at least one of its out items
                    // is reachable. This would cover all cases, except that some
                    // peers may have no successors (e.g. peers that throw an
                    // an exception that is not caught by the method). So we need 
                    // to also check the inItem.
                    if (p.inItem() != null) {
                        DataFlowItem dfi = p.inItem();
                        // there will only be one peer for an initializer,
                        // as it cannot occur in a finally block.
                        if (dfi.reachable) {
                            isReachable = true;
                        }
                        if (dfi.normalReachable) {
                            isNormalReachable = true;
                            // no point in doing more.
                            break;
                        }
                    }

                    if (p.outItems != null) {
                        for (DataFlowItem item : p.outItems.values()) {
                            if (item != null && item.reachable) {
                                // n is reachable.
                                isReachable = true;
                                break;
                            }
                        }
                    }
                }

                if (!isNormalReachable && n instanceof Initializer) {
                    throw new SemanticException("Initializers must be able to complete normally.",
                                                n.position());
                }

                n = n.reachable(isReachable);
            }
        }
        return n;
    }

    @Override
    public void post(FlowGraph<DataFlowItem> graph, Term root)
            throws SemanticException {
        // There is no need to do any checking in this method, as this will
        // be handled by leaveCall and checkReachability.
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }
    }

    @Override
    public void check(FlowGraph<DataFlowItem> graph, Term n, boolean entry,
            DataFlowItem inItem, Map<EdgeKey, DataFlowItem> outItems)
            throws SemanticException {
        throw new InternalCompilerError("ReachChecker.check should "
                + "never be called.");
    }

}
