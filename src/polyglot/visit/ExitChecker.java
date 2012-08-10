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

import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.CodeNode;
import polyglot.ast.MethodDecl;
import polyglot.ast.NodeFactory;
import polyglot.ast.Return;
import polyglot.ast.Term;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.FlowGraph.EdgeKey;

/**
 * Visitor which checks that all (terminating) paths through a 
 * method must return.
 */
public class ExitChecker extends DataFlow<ExitChecker.DataFlowItem> {
    protected CodeNode code;

    public ExitChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf, false /* backward analysis */);
    }

    @Override
    protected FlowGraph<DataFlowItem> initGraph(CodeNode code, Term root) {
        this.code = code;

        if (code instanceof MethodDecl) {
            MethodDecl d = (MethodDecl) code;
            if (!d.methodInstance().returnType().isVoid()) {
                return super.initGraph(code, root);
            }
        }

        return null;
    }

    @Override
    public DataFlowItem createInitialItem(FlowGraph<DataFlowItem> graph,
            Term node, boolean entry) {
        return DataFlowItem.EXITS;
    }

    protected static class DataFlowItem extends DataFlow.Item {
        public final boolean exits; // whether all paths leaving this node lead to an exit 

        protected DataFlowItem(boolean exits) {
            this.exits = exits;
        }

        public static final DataFlowItem EXITS = new DataFlowItem(true);
        public static final DataFlowItem DOES_NOT_EXIT =
                new DataFlowItem(false);

        @Override
        public String toString() {
            return "exits=" + exits;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.exits == ((DataFlowItem) o).exits;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (exits ? 5235 : 8673);
        }

    }

    @Override
    public Map<EdgeKey, DataFlowItem> flow(DataFlowItem in,
            FlowGraph<DataFlowItem> graph, Term n, boolean entry,
            Set<EdgeKey> succEdgeKeys) {
        // If every path from the exit node to the entry goes through a return,
        // we're okay.  So make the exit bit false at exit and true at every return;
        // the confluence operation is &&. 
        // We deal with exceptions specially, and assume that any exception
        // edge to the exit node is OK.
        if (n instanceof Return) {
            return itemToMap(DataFlowItem.EXITS, succEdgeKeys);
        }

        if (n == graph.root() && !entry) {
            // all exception edges to the exit node are regarded as exiting
            // correctly. Make sure non-exception edges have the
            // exit bit false.
            Map<EdgeKey, DataFlowItem> m =
                    itemToMap(DataFlowItem.EXITS, succEdgeKeys);
            if (succEdgeKeys.contains(FlowGraph.EDGE_KEY_OTHER)) {
                m.put(FlowGraph.EDGE_KEY_OTHER, DataFlowItem.DOES_NOT_EXIT);
            }
            if (succEdgeKeys.contains(FlowGraph.EDGE_KEY_TRUE)) {
                m.put(FlowGraph.EDGE_KEY_TRUE, DataFlowItem.DOES_NOT_EXIT);
            }
            if (succEdgeKeys.contains(FlowGraph.EDGE_KEY_FALSE)) {
                m.put(FlowGraph.EDGE_KEY_FALSE, DataFlowItem.DOES_NOT_EXIT);
            }

            return m;
        }

        return itemToMap(in, succEdgeKeys);
    }

    @Override
    public DataFlowItem confluence(List<DataFlowItem> inItems, Term node,
            boolean entry, FlowGraph<DataFlowItem> graph) {
        // all paths must have an exit
        for (DataFlowItem item : inItems) {
            if (!item.exits) {
                return DataFlowItem.DOES_NOT_EXIT;
            }
        }
        return DataFlowItem.EXITS;
    }

    @Override
    public void check(FlowGraph<DataFlowItem> graph, Term n, boolean entry,
            DataFlowItem inItem, Map<EdgeKey, DataFlowItem> outItems)
            throws SemanticException {
        // Check for statements not on the path to exit; compound
        // statements are allowed to be off the path.  (e.g., "{ return; }"
        // or "while (true) S").  If a compound statement is truly
        // unreachable, one of its sub-statements will be also and we will
        // report an error there.
        if (n == graph.root() && entry) {
            if (outItems != null && !outItems.isEmpty()) {
                // due to the flow equations, all DataFlowItems in the outItems map
                // are the same, so just take the first one.
                DataFlowItem outItem = outItems.values().iterator().next();
                if (outItem != null && !outItem.exits) {
                    throw new SemanticException("Missing return statement.",
                                                code.position());
                }
            }
        }
    }
}
