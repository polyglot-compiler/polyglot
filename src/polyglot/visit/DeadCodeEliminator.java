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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Assign;
import polyglot.ast.Block;
import polyglot.ast.CompoundStmt;
import polyglot.ast.Do;
import polyglot.ast.Empty;
import polyglot.ast.Eval;
import polyglot.ast.Expr;
import polyglot.ast.For;
import polyglot.ast.If;
import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureCall;
import polyglot.ast.Stmt;
import polyglot.ast.Switch;
import polyglot.ast.Term;
import polyglot.ast.Unary;
import polyglot.ast.While;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Pair;
import polyglot.util.Position;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;

/**
 * Visitor which performs dead code elimination.  (Note that "dead code" is not
 * unreachable code, but is actually code that has no effect.)
 */
public class DeadCodeEliminator extends
        DataFlow<DeadCodeEliminator.DataFlowItem> {
    public DeadCodeEliminator(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf, false /* backward analysis */, true /* perform dataflow on entry to CodeDecls */);
    }

    protected static class DataFlowItem extends polyglot.visit.DataFlow.Item {
        // Set of LocalInstances of live variables.
        private Set<LocalInstance> liveVars;

        // Set of LocalInstances of live declarations.  A LocalDecl is live if
        // the declared local is ever live.
        private Set<LocalInstance> liveDecls;

        /**
         * Constructor for creating an empty set.
         */
        protected DataFlowItem() {
            this.liveVars = new HashSet<LocalInstance>();
            this.liveDecls = new HashSet<LocalInstance>();
        }

        /**
         * Deep copy constructor.
         */
        protected DataFlowItem(DataFlowItem dfi) {
            liveVars = new HashSet<LocalInstance>(dfi.liveVars);
            liveDecls = new HashSet<LocalInstance>(dfi.liveDecls);
        }

        public void add(LocalInstance li) {
            liveVars.add(li);
            liveDecls.add(li);
        }

        public void addAll(Set<LocalInstance> lis) {
            liveVars.addAll(lis);
            liveDecls.addAll(lis);
        }

        public void remove(LocalInstance li) {
            liveVars.remove(li);
        }

        public void removeAll(Set<LocalInstance> lis) {
            liveVars.removeAll(lis);
        }

        public void removeDecl(LocalInstance li) {
            liveVars.remove(li);
            liveDecls.remove(li);
        }

        public void union(DataFlowItem dfi) {
            liveVars.addAll(dfi.liveVars);
            liveDecls.addAll(dfi.liveDecls);
        }

        protected boolean needDecl(LocalInstance li) {
            return liveDecls.contains(li);
        }

        protected boolean needDef(LocalInstance li) {
            return liveVars.contains(li);
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (LocalInstance li : liveVars) {
                result = 31 * result + li.hashCode();
            }

            for (LocalInstance li : liveDecls) {
                result = 31 * result + li.hashCode();
            }

            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DataFlowItem)) return false;

            DataFlowItem dfi = (DataFlowItem) o;
            return liveVars.equals(dfi.liveVars)
                    && liveDecls.equals(dfi.liveDecls);
        }

        @Override
        public String toString() {
            return "<vars=" + liveVars + " ; decls=" + liveDecls + ">";
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
                result.union(inItem);
            }
        }

        return result;
    }

    @Override
    public Map<EdgeKey, DataFlowItem> flow(DataFlowItem in,
            FlowGraph<DataFlowItem> graph, Term t, boolean entry,
            Set<EdgeKey> succEdgeKeys) {
        return itemToMap(flow(in, graph, t, entry), succEdgeKeys);
    }

    protected DataFlowItem flow(DataFlowItem in, FlowGraph<DataFlowItem> graph,
            Term t, boolean entry) {
        DataFlowItem result = new DataFlowItem(in);

        if (entry) {
            return result;
        }

        Pair<Set<LocalInstance>, Set<LocalInstance>> du = null;

        if (t instanceof LocalDecl) {
            LocalDecl n = (LocalDecl) t;

            LocalInstance to = n.localInstance();
            result.removeDecl(to);

            du = getDefUse(n.init());
        }
        else if (t instanceof Stmt && !(t instanceof CompoundStmt)) {
            du = getDefUse(t);
        }
        else if (t instanceof CompoundStmt) {
            if (t instanceof If) {
                du = getDefUse(((If) t).cond());
            }
            else if (t instanceof Switch) {
                du = getDefUse(((Switch) t).expr());
            }
            else if (t instanceof Do) {
                du = getDefUse(((Do) t).cond());
            }
            else if (t instanceof For) {
                du = getDefUse(((For) t).cond());
            }
            else if (t instanceof While) {
                du = getDefUse(((While) t).cond());
            }
        }

        if (du != null) {
            result.removeAll(du.part1());
            result.addAll(du.part2());
        }

        return result;
    }

    @Override
    public void post(FlowGraph<DataFlowItem> graph, Term root)
            throws SemanticException {
        // No need to do any checking.
        if (Report.should_report(Report.cfg, 2)) {
            dumpFlowGraph(graph, root);
        }
    }

    /**
     * @throws SemanticException  
     */
    @Override
    public void check(FlowGraph<DataFlowItem> graph, Term n, boolean entry,
            DataFlowItem inItem, Map<EdgeKey, DataFlowItem> outItems)
            throws SemanticException {

        throw new InternalCompilerError("DeadCodeEliminator.check should "
                + "never be called.");
    }

    private DataFlowItem getItem(Term n) {
        FlowGraph<DataFlowItem> g = currentFlowGraph();
        if (g == null) return null;

        Collection<Peer<DataFlowItem>> peers = g.peers(n, Term.EXIT);
        if (peers == null || peers.isEmpty()) return null;

        List<DataFlowItem> items = new ArrayList<DataFlowItem>();
        for (Peer<DataFlowItem> p : peers) {
            if (p.inItem() != null) items.add(p.inItem());
        }

        return confluence(items, n, false, g);
    }

    @Override
    public Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {

        if (n instanceof LocalDecl) {
            LocalDecl ld = (LocalDecl) n;
            DataFlowItem in = getItem(ld);
            if (in == null || in.needDecl(ld.localInstance())) return n;
            return getEffects(ld.init());
        }

        if (n instanceof Eval) {
            Eval eval = (Eval) n;
            Expr expr = eval.expr();
            Local local;
            Expr right = null;

            if (expr instanceof Assign) {
                Assign assign = (Assign) expr;
                Expr left = assign.left();
                right = assign.right();

                if (!(left instanceof Local)) return n;
                local = (Local) left;
            }
            else if (expr instanceof Unary) {
                Unary unary = (Unary) expr;
                expr = unary.expr();
                if (!(expr instanceof Local)) return n;
                local = (Local) expr;
            }
            else {
                return n;
            }

            DataFlowItem in = getItem(eval);
            if (in == null || in.needDef(local.localInstance().orig()))
                return n;

            if (right != null) {
                return getEffects(right);
            }

            return nf.Empty(Position.compilerGenerated());
        }

        if (n instanceof Block) {
            // Get rid of empty statements.
            Block b = (Block) n;
            List<Stmt> stmts = new ArrayList<Stmt>(b.statements());
            for (Iterator<Stmt> it = stmts.iterator(); it.hasNext();) {
                if (it.next() instanceof Empty) it.remove();
            }

            return b.statements(stmts);
        }

        return n;
    }

    /**
     * Returns pair of sets of local instances.
     * Element 0 is the set of local instances DEFined by the node.
     * Element 1 is the set of local instances USEd by the node.
     */
    protected Pair<Set<LocalInstance>, Set<LocalInstance>> getDefUse(Node n) {
        final Set<LocalInstance> def = new HashSet<LocalInstance>();
        final Set<LocalInstance> use = new HashSet<LocalInstance>();

        if (n != null) {
            n.visit(createDefUseFinder(def, use));
        }

        return new Pair<Set<LocalInstance>, Set<LocalInstance>>(def, use);
    }

    protected NodeVisitor createDefUseFinder(Set<LocalInstance> def,
            Set<LocalInstance> use) {
        return new DefUseFinder(def, use);
    }

    protected static class DefUseFinder extends HaltingVisitor {
        protected Set<LocalInstance> def;
        protected Set<LocalInstance> use;

        public DefUseFinder(Set<LocalInstance> def, Set<LocalInstance> use) {
            this.def = def;
            this.use = use;
        }

        @Override
        public NodeVisitor enter(Node n) {
            if (n instanceof LocalAssign) {
                return bypass(((Assign) n).left());
            }

            return super.enter(n);
        }

        @Override
        public Node leave(Node old, Node n, NodeVisitor v) {
            if (n instanceof Local) {
                use.add(((Local) n).localInstance().orig());
            }
            else if (n instanceof Assign) {
                Expr left = ((Assign) n).left();
                if (left instanceof Local) {
                    def.add(((Local) left).localInstance().orig());
                }
            }

            return n;
        }
    }

    /**
     * Returns a statement that is side-effect-equivalent to the given
     * expression.
     */
    protected Stmt getEffects(Expr expr) {
        Stmt empty = nf.Empty(Position.compilerGenerated());
        if (expr == null) return empty;

        final List<Stmt> result = new LinkedList<Stmt>();
        final Position pos = Position.compilerGenerated();

        NodeVisitor v = new HaltingVisitor() {
            @Override
            public NodeVisitor enter(Node n) {
                if (n instanceof Assign || n instanceof ProcedureCall) {
                    return bypassChildren(n);
                }

                // XXX Cast

                if (n instanceof Unary) {
                    Unary.Operator op = ((Unary) n).operator();
                    if (op == Unary.POST_INC || op == Unary.POST_DEC
                            || op == Unary.PRE_INC || op == Unary.PRE_INC) {

                        return bypassChildren(n);
                    }
                }

                return this;
            }

            @Override
            public Node leave(Node old, Node n, NodeVisitor v) {
                if (n instanceof Assign || n instanceof ProcedureCall) {
                    result.add(nf.Eval(pos, (Expr) n));
                }
                else if (n instanceof Unary) {
                    Unary.Operator op = ((Unary) n).operator();
                    if (op == Unary.POST_INC || op == Unary.POST_DEC
                            || op == Unary.PRE_INC || op == Unary.PRE_INC) {

                        result.add(nf.Eval(pos, (Expr) n));
                    }
                }

                // XXX Cast

                return n;
            }
        };

        expr.visit(v);

        if (result.isEmpty()) return empty;
        if (result.size() == 1) return result.get(0);
        return nf.Block(Position.compilerGenerated(), result);
    }
}
