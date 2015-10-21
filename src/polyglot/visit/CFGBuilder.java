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
import java.util.Collections;
import java.util.List;

import polyglot.ast.Assert;
import polyglot.ast.Block;
import polyglot.ast.Branch;
import polyglot.ast.Catch;
import polyglot.ast.CodeNode;
import polyglot.ast.CompoundStmt;
import polyglot.ast.JLang;
import polyglot.ast.Labeled;
import polyglot.ast.Loop;
import polyglot.ast.Return;
import polyglot.ast.Stmt;
import polyglot.ast.Switch;
import polyglot.ast.Term;
import polyglot.ast.Try;
import polyglot.main.Report;
import polyglot.types.MemberInstance;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.util.CollectionUtil;
import polyglot.util.Copy;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;
import polyglot.visit.FlowGraph.Edge;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;

/**
 * Class used to construct a CFG.
 */
public class CFGBuilder<FlowItem extends DataFlow.Item>
        implements Copy<CFGBuilder<FlowItem>> {
    /** The language this CFGBuilder operates on. */
    private final JLang lang;

    /** The flow graph under construction. */
    protected FlowGraph<FlowItem> graph;

    /** The type system. */
    protected TypeSystem ts;

    /**
     * The outer CFGBuilder.  We create a new inner CFGBuilder when entering a
     * loop or try-block and when entering a finally block.
     */
    protected CFGBuilder<FlowItem> outer;

    /**
     * The innermost loop or try-block in lexical scope.  We maintain a stack
     * of loops and try-blocks in order to add edges for break and continue
     * statements and for exception throws.  When such a jump is encountered we
     * traverse the stack, searching for the target of the jump.
     */
    protected Stmt innermostTarget;

    /**
     * Nodes in finally blocks need to be analyzed multiple times, once for each
     * possible way to reach the finally block. The path_to_finally is used to
     * distinguish these different "copies" of nodes in the finally block, and is
     * a list of terms that attempted to complete normally that caused the finally
     * block to be reached. If this CFGBuilder is for an AST that is not nested inside
     * a finallyBlock, then path_to_finally will be empty.
     *
     * To explain by example, consider the following code (which is assumed to not
     * be nested inside a finally block, and assume that S1 does not contain any
     * try-finally block).
     *   {@code try { S1 } finally { S2 }}
     * Assume that term t1 in S1 may complete abruptly. The code for S2 will be
     * analyzed (at least) twice: once for normal termination of S1 (and so
     * path_to_finally will be empty) and once for the abrupt completion of t1
     * (and so path_to_finally will be [t1]).
     *
     * Consider the following code:
     *   {@code try { try { S1 } finally { S2 } } finally { S3 }}
     * Assume that terms t1 in S1 and t2 in S2 may complete abruptly.
     * Nodes in S2 will be analyzed (at least) twice, with path_to_finally empty
     * (for normal termination of S1) and with path_to_empty equals [t1] (for
     * abrupt completion of t1). S3 will be analyzed (at least) 3 times:
     * once with path_to_finally empty (for normal termination of S1 and S2)
     * once with path_to_finally equals [t1] (for abrupt completion of t1 and normal termination of S2), and
     * once with path_to_finally equals [t1, t2] (for (attempted) abrupt completion of t1 and subsequent
     * abrupt completion of t2).
     *
     * Consider the following code:
     *   {@code try { S1 } finally { try { S2 } finally { S3 } }}
     * Assume that terms t1 in S1 and t2 in S2 may complete abruptly.
     * Nodes in S2 will be analyzed (at least) twice, with path_to_finally empty
     * (for normal termination of S1) and with path_to_empty equals [t1] (for
     * abrupt completion of t1). S3 will be analyzed (at least) 3 times:
     * once with path_to_finally empty (for normal termination of S1 and S2)
     * once with path_to_finally equals [t1] (for abrupt completion of t1 and normal termination of S2), and
     * once with path_to_finally equals [t1, t2] (for (attempted) abrupt completion of t1 and subsequent
     * abrupt completion of t2).
     *
     */
    protected List<Term> path_to_finally;

    /** The data flow analysis for which we are constructing the graph. */
    protected DataFlow<FlowItem> df;

    /**
     * True if we should skip the catch blocks for the innermost try when
     * building edges for an exception throw.
     */
    protected boolean skipInnermostCatches;

    /**
     * True if we should skip dead if branches, such as S in "if (false) { S }".
     * Different dataflow analyses have different requirements.
     */
    protected boolean skipDeadIfBranches = false;

    /**
     * True if we should skip dead loop bodies, such as S in "while (false) { S }".
     * Different dataflow analyses have different requirements.
     */
    protected boolean skipDeadLoopBodies = false;

    /**
     * True if we should add edges for uncaught Errors to the exit node of the
     * graph.  By default, we do not, but subclasses can change this behavior
     * if needed.
     */
    protected boolean errorEdgesToExitNode = false;
    /**
     * True if we want to add implicit error edges from all statements
     */
    protected boolean trackImplicitErrors = false;

    /**
     * Should we add exception edges into finally blocks? If true, then the edge from
     * an AST node that throws an exception to a finally block will be labeled with the
     * appropriate exception; if false, then the edge will be labeled OTHER.
     * For backwards compatibility, the default value is false.
     */
    protected boolean exceptionEdgesToFinally;

    public CFGBuilder(JLang lang, TypeSystem ts, FlowGraph<FlowItem> graph,
            DataFlow<FlowItem> df) {
        this.lang = lang;
        this.ts = ts;
        this.graph = graph;
        this.df = df;
        this.path_to_finally = Collections.emptyList();
        this.outer = null;
        this.innermostTarget = null;
        this.skipInnermostCatches = false;
        this.errorEdgesToExitNode = false;
        this.exceptionEdgesToFinally = false;
    }

    public JLang lang() {
        return this.lang;
    }

    public FlowGraph<FlowItem> graph() {
        return graph;
    }

    public DataFlow<FlowItem> dataflow() {
        return df;
    }

    public CFGBuilder<FlowItem> outer() {
        return outer;
    }

    public Stmt innermostTarget() {
        return innermostTarget;
    }

    public boolean skipInnermostCatches() {
        return skipInnermostCatches;
    }

    /** Get the type system. */
    public TypeSystem typeSystem() {
        return ts;
    }

    /** Copy the CFGBuilder. */
    @Override
    public CFGBuilder<FlowItem> copy() {
        try {
            @SuppressWarnings("unchecked")
            CFGBuilder<FlowItem> clone = (CFGBuilder<FlowItem>) super.clone();
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    /**
     * Construct a new CFGBuilder with the a new innermost loop or
     * try-block {@code n}.
     */
    public CFGBuilder<FlowItem> push(Stmt n) {
        return push(n, false);
    }

    /**
     * Construct a new CFGBuilder with the a new innermost loop or
     * try-block {@code n}, optionally skipping innermost catch blocks.
     */
    public CFGBuilder<FlowItem> push(Stmt n, boolean skipInnermostCatches) {
        CFGBuilder<FlowItem> v = copy();
        v.outer = this;
        v.innermostTarget = n;
        v.skipInnermostCatches = skipInnermostCatches;
        return v;
    }

    /**
     * Visit edges for an assert statement.
     */
    public void visitAssert(Assert n) {
        // Add a bypass edge edge from entry to exit node.
        // This simulates the scenario when assert is disabled.
        edge(this, n, Term.ENTRY, n, Term.EXIT, FlowGraph.EDGE_KEY_OTHER);
    }

    /**
     * Visit edges from a branch.  Simulate breaking/continuing out of
     * the loop, visiting any finally blocks encountered.
     */
    public void visitBranchTarget(Branch b) {
        Peer<FlowItem> last_peer =
                graph.peer(b, this.path_to_finally, Term.EXIT);

        for (CFGBuilder<FlowItem> v = this; v != null; v = v.outer) {
            Term c = v.innermostTarget;

            if (c instanceof Try) {
                Try tr = (Try) c;
                if (tr.finallyBlock() != null) {
                    last_peer =
                            tryFinally(v,
                                       last_peer,
                                       last_peer.node == b,
                                       tr.finallyBlock());
                }
            }

            if (b.label() != null) {
                if (c instanceof Labeled) {
                    Labeled l = (Labeled) c;
                    if (l.label().equals(b.label())) {
                        if (b.kind() == Branch.BREAK) {
                            edge(last_peer,
                                 this.graph().peer(l, Term.EXIT),
                                 FlowGraph.EDGE_KEY_OTHER);
                        }
                        else {
                            Stmt s = l.statement();
                            if (s instanceof Loop) {
                                Loop loop = (Loop) s;
                                edge(last_peer,
                                     this.graph().peer(
                                                       lang().continueTarget(loop),
                                                       Term.ENTRY),
                                     FlowGraph.EDGE_KEY_OTHER);
                            }
                            else {
                                throw new CFGBuildError("Target of continue statement must "
                                        + "be a loop.", l.position());
                            }
                        }

                        return;
                    }
                }
            }
            else {
                if (c instanceof Loop) {
                    Loop l = (Loop) c;
                    if (b.kind() == Branch.CONTINUE) {
                        edge(last_peer,
                             this.graph().peer(lang().continueTarget(l),
                                               Term.ENTRY),
                             FlowGraph.EDGE_KEY_OTHER);
                    }
                    else {
                        edge(last_peer,
                             this.graph().peer(l, Term.EXIT),
                             FlowGraph.EDGE_KEY_OTHER);
                    }

                    return;
                }
                else if (c instanceof Switch && b.kind() == Branch.BREAK) {
                    edge(last_peer,
                         this.graph().peer(c, this.path_to_finally, Term.EXIT),
                         FlowGraph.EDGE_KEY_OTHER);
                    return;
                }
            }
        }

        throw new CFGBuildError("Target of branch statement not found.",
                                b.position());
    }

    /**
     * Visit edges for a return statement.  Simulate the return, visiting any
     * finally blocks encountered.
     */
    public void visitReturn(Return r) {
        Peer<FlowItem> last_peer =
                this.graph().peer(r, this.path_to_finally, Term.EXIT);

        for (CFGBuilder<FlowItem> v = this; v != null; v = v.outer) {
            Term c = v.innermostTarget;

            if (c instanceof Try) {
                Try tr = (Try) c;
                if (tr.finallyBlock() != null) {
                    last_peer =
                            tryFinally(v,
                                       last_peer,
                                       last_peer.node == r,
                                       tr.finallyBlock());
                }
            }
        }

        // Add an edge to the exit node.
        edge(last_peer, exitPeer(), FlowGraph.EDGE_KEY_OTHER);
    }

    protected static int counter = 0;

    /** Visit the AST, constructing the CFG. */
    public void visitGraph() {
        String name = StringUtil.getShortNameComponent(df.getClass().getName());
        name += counter++;

        if (Report.should_report(Report.cfg, 2)) {
            String rootName = "";
            if (graph.root() instanceof CodeNode) {
                CodeNode cd = (CodeNode) graph.root();
                rootName = cd.codeInstance().toString();
                if (cd.codeInstance() instanceof MemberInstance) {
                    rootName += " in "
                            + ((MemberInstance) cd.codeInstance()).container()
                                                                  .toString();
                }
            }

            Report.report(2, "digraph CFGBuild" + name + " {");
            Report.report(2,
                          "  label=\"CFGBuilder: " + name + "\\n" + rootName
                                  + "\"; fontsize=20; center=true; ratio=auto; size = \"8.5,11\";");
        }

        // create peers for the entry and exit nodes.
        entryPeer();
        exitPeer();

        this.visitCFG(graph.root(), Collections.<EdgeKeyTermPair> emptyList());

        if (Report.should_report(Report.cfg, 2)) Report.report(2, "}");
    }

    /**
     * Utility method to get the peer for the entry of the flow graph.
     */
    protected Peer<FlowItem> entryPeer() {
        return graph.peer(graph.root(),
                          Collections.<Term> emptyList(),
                          Term.ENTRY);
    }

    /**
     * Utility method to get the peer for the exit of the flow graph.
     */
    protected Peer<FlowItem> exitPeer() {
        return graph.peer(graph.root(),
                          Collections.<Term> emptyList(),
                          Term.EXIT);
    }

    /**
     * Utility function to visit all edges in a list.
     *
     * If {@code entry} is Term.ENTRY, the final successor is
     * {@code after}'s entry node; if it's Term.EXIT, it's
     * {@code after}'s exit.
     */
    public void visitCFGList(List<? extends Term> elements, Term after,
            int entry) {
        Term prev = null;

        for (Term c : elements) {
            if (prev != null) {
                visitCFG(prev, c, Term.ENTRY);
            }

            prev = c;
        }

        if (prev != null) {
            visitCFG(prev, after, entry);
        }
    }

    /**
     * Create an edge for a node {@code a} with a single successor
     * {@code succ}.
     *
     * The EdgeKey used for the edge from {@code a} to {@code succ}
     * will be FlowGraph.EDGE_KEY_OTHER.
     *
     * If {@code entry} is Term.ENTRY, the successor is {@code succ}'s
     * entry node; if it's Term.EXIT, it's {@code succ}'s exit.
     */
    public void visitCFG(Term a, Term succ, int entry) {
        visitCFG(a, FlowGraph.EDGE_KEY_OTHER, succ, entry);
    }

    /**
     * Create an edge for a node {@code a} with a single successor
     * {@code succ}, and EdgeKey {@code edgeKey}.
     *
     * If {@code entry} is Term.ENTRY, the successor is {@code succ}'s
     * entry node; if it's Term.EXIT, it's {@code succ}'s exit.
     */
    public void visitCFG(Term a, FlowGraph.EdgeKey edgeKey, Term succ,
            int entry) {
        visitCFG(a,
                 CollectionUtil.list(new EdgeKeyTermPair(edgeKey,
                                                         succ,
                                                         entry)));
    }

    /**
     * Create edges from node {@code a} to successors {@code succ1}
     * and {@code succ2} with EdgeKeys {@code edgeKey1} and
     * {@code edgeKey2} respectively.
     *
     * {@code entry1} and {@code entry2} determine whether the
     * successors are entry or exit nodes. They can be Term.ENTRY or Term.EXIT.
     */
    public void visitCFG(Term a, FlowGraph.EdgeKey edgeKey1, Term succ1,
            int entry1, FlowGraph.EdgeKey edgeKey2, Term succ2, int entry2) {
        visitCFG(a,
                 CollectionUtil.list(new EdgeKeyTermPair(edgeKey1,
                                                         succ1,
                                                         entry1),
                                     new EdgeKeyTermPair(edgeKey2,
                                                         succ2,
                                                         entry2)));
    }

    /**
     * Create edges from node {@code a} to all successors {@code succ}
     * with the EdgeKey {@code edgeKey} for all edges created.
     *
     * If {@code entry} is Term.ENTRY, all terms in {@code succ} are
     * treated as entry nodes; if it's Term.EXIT, they are treated as exit
     * nodes.
     */
    public void visitCFG(Term a, FlowGraph.EdgeKey edgeKey, List<Term> succ,
            int entry) {
        List<EdgeKeyTermPair> l = new ArrayList<>(succ.size());

        for (Term t : succ) {
            l.add(new EdgeKeyTermPair(edgeKey, t, entry));
        }

        visitCFG(a, l);
    }

    /**
     * Create edges from node {@code a} to all successors
     * {@code succ} with the EdgeKey {@code edgeKey} for all edges
     * created.
     *
     * The {@code entry} list must have the same size as
     * {@code succ}, and each corresponding element determines whether a
     * successor is an entry or exit node (using Term.ENTRY or Term.EXIT).
     */
    public void visitCFG(Term a, FlowGraph.EdgeKey edgeKey, List<Term> succ,
            List<Integer> entry) {
        if (succ.size() != entry.size()) {
            throw new IllegalArgumentException();
        }

        List<EdgeKeyTermPair> l = new ArrayList<>(succ.size());

        for (int i = 0; i < succ.size(); i++) {
            Term t = succ.get(i);
            l.add(new EdgeKeyTermPair(edgeKey, t, entry.get(i).intValue()));
        }

        visitCFG(a, l);
    }

    protected static class EdgeKeyTermPair {

        public final FlowGraph.EdgeKey edgeKey;
        public final Term term;
        public final int entry;

        public EdgeKeyTermPair(FlowGraph.EdgeKey edgeKey, Term term,
                int entry) {
            this.edgeKey = edgeKey;
            this.term = term;
            this.entry = entry;
        }

        @Override
        public String toString() {
            return "{edgeKey=" + edgeKey + ",term=" + term + ","
                    + (entry == Term.ENTRY ? "entry" : "exit") + "}";
        }

    }

    /**
     * Create edges for a node {@code a} with successors
     * {@code succs}.
     *
     * @param a the source node for the edges.
     * @param succs a list of {@code EdgeKeyTermPair}s
     */
    protected void visitCFG(Term a, List<EdgeKeyTermPair> succs) {
        Term child = lang().firstChild(a);

        if (child == null) {
            edge(this, a, Term.ENTRY, a, Term.EXIT, FlowGraph.EDGE_KEY_OTHER);
        }
        else {
            edge(this,
                 a,
                 Term.ENTRY,
                 child,
                 Term.ENTRY,
                 FlowGraph.EDGE_KEY_OTHER);
        }

        if (Report.should_report(Report.cfg, 2))
            Report.report(2, "// node " + a + " -> " + succs);

        succs = lang().acceptCFG(a, this, succs);

        for (EdgeKeyTermPair s : succs) {
            edge(a, s.term, s.entry, s.edgeKey);
        }

        visitThrow(a);
    }

    public void visitThrow(Term a) {
        for (Type type : lang().throwTypes(a, ts)) {
            visitThrow(a, Term.EXIT, type);
        }

        if (trackImplicitErrors) {
            // Every statement can throw an error.
            // This is probably too inefficient.
            if (a instanceof Stmt && !(a instanceof CompoundStmt)
                    || a instanceof Block
                            && ((Block) a).statements().isEmpty()) {

                visitThrow(a, Term.EXIT, ts.Error());
            }
        }
    }

    /**
     * Create edges for an exception thrown from term {@code t}.
     */
    public void visitThrow(Term t, int entry, Type type) {
        Peer<FlowItem> last_peer =
                this.graph.peer(t, this.path_to_finally, entry);

        for (CFGBuilder<FlowItem> v = this; v != null; v = v.outer) {
            Term c = v.innermostTarget;

            if (c instanceof Try) {
                Try tr = (Try) c;

                if (!v.skipInnermostCatches) {
                    boolean definiteCatch = false;

                    for (Catch cb : tr.catchBlocks()) {
                        // definite catch
                        if (type.isImplicitCastValid(cb.catchType())) {
                            edge(last_peer,
                                 this.graph.peer(cb,
                                                 this.path_to_finally,
                                                 Term.ENTRY),
                                 new FlowGraph.ExceptionEdgeKey(type));
                            definiteCatch = true;
                        }
                        // possible catch
                        else if (cb.catchType().isImplicitCastValid(type)) {
                            edge(last_peer,
                                 this.graph.peer(cb,
                                                 this.path_to_finally,
                                                 Term.ENTRY),
                                 new FlowGraph.ExceptionEdgeKey(cb.catchType()));
                        }
                    }
                    if (definiteCatch) {
                        // the exception has definitely been caught.
                        // we can stop recursing to outer try-catch blocks
                        return;
                    }
                }

                if (tr.finallyBlock() != null) {
                    if (exceptionEdgesToFinally) {
                        last_peer = tryFinally(v,
                                               last_peer,
                                               last_peer.node == t,
                                               new FlowGraph.ExceptionEdgeKey(type),
                                               tr.finallyBlock());
                    }
                    else {
                        last_peer = tryFinally(v,
                                               last_peer,
                                               last_peer.node == t,
                                               tr.finallyBlock());
                    }
                }
            }
        }

        // If not caught, insert a node from the thrower to exit.
        if (errorEdgesToExitNode || !type.isSubtype(ts.Error())) {
            edge(last_peer, exitPeer(), new FlowGraph.ExceptionEdgeKey(type));
        }
    }

    /**
     * Create edges for the finally block of a try-finally construct.
     *
     * @param v v.innermostTarget is the Try term that the finallyBlock is associated with.
     * @param last is the last peer visited before the finally block is entered.
     * @param abruptCompletion is true if and only if the finally block is being entered
     *        due to the (attempted) abrupt completion of the term {@code last.node}.
     * @param edgeKeyToFinally the EdgeKey to use for the edge going to the entry of the finally block. If null, then FlowGraph.EDGE_KEY_OTHER will be used.
     * @param finallyBlock the finally block associated with a try finally block.
     */
    protected static <FlowItem extends DataFlow.Item> Peer<FlowItem> tryFinally(
            CFGBuilder<FlowItem> v, Peer<FlowItem> last,
            boolean abruptCompletion, EdgeKey edgeKeyToFinally,
            Block finallyBlock) {
        CFGBuilder<FlowItem> v_ = v.outer.enterFinally(last, abruptCompletion);

        Peer<FlowItem> finallyBlockEntryPeer =
                v_.graph.peer(finallyBlock, v_.path_to_finally, Term.ENTRY);

        if (edgeKeyToFinally == null) {
            edgeKeyToFinally = FlowGraph.EDGE_KEY_OTHER;
        }
        v_.edge(last, finallyBlockEntryPeer, edgeKeyToFinally);

        // visit the finally block.
        v_.visitCFG(finallyBlock, Collections.<EdgeKeyTermPair> emptyList());

        // the exit peer for the finally block.
        Peer<FlowItem> finallyBlockExitPeer =
                v_.graph.peer(finallyBlock, v_.path_to_finally, Term.EXIT);
        return finallyBlockExitPeer;
    }

    /**
     * Create edges for the finally block of a try-finally construct.
     *
     * @param v v.innermostTarget is the Try term that the finallyBlock is associated with.
     * @param last is the last peer visited before the finally block is entered.
     * @param abruptCompletion is true if and only if the finally block is being entered
     *        due to the (attempted) abrupt completion of the term {@code last.node}.
     * @param finallyBlock the finally block associated with a try finally block.
     */
    protected static <FlowItem extends DataFlow.Item> Peer<FlowItem> tryFinally(
            CFGBuilder<FlowItem> v, Peer<FlowItem> last,
            boolean abruptCompletion, Block finallyBlock) {
        return tryFinally(v,
                          last,
                          abruptCompletion,
                          FlowGraph.EDGE_KEY_OTHER,
                          finallyBlock);
    }

    /**
     * Enter a finally block. This method returns a new CFGBuilder
     * with the path_to_finally set appropriately.
     * If we are entering the finally block because peer {@code from} is
     * (attempting to) complete abruptly, then the path_to_finally will have
     * Term {@code from} appended to the path_to_finally list
     * of {@code from}. Otherwise, {@code from} is not attempting
     * to complete abruptly, and the path_to_finally will be the same as
     * {@code from.path_to_finally}.
     *
     */
    protected CFGBuilder<FlowItem> enterFinally(Peer<FlowItem> from,
            boolean abruptCompletion) {
        if (abruptCompletion) {
            CFGBuilder<FlowItem> v = this.copy();
            v.path_to_finally =
                    new ArrayList<>(from.path_to_finally.size() + 1);
            v.path_to_finally.addAll(from.path_to_finally);
            v.path_to_finally.add(from.node);
            return v;
        }
        else {
            if (CollectionUtil.equals(this.path_to_finally,
                                      from.path_to_finally)) {
                return this;
            }
            CFGBuilder<FlowItem> v = this.copy();
            v.path_to_finally = new ArrayList<>(from.path_to_finally);
            return v;
        }
    }

    /**
     * Add an edge to the CFG from the exit of {@code p} to either the
     * entry or exit of {@code q}.
     */
    public void edge(Term p, Term q, int qEntry) {
        edge(this, p, q, qEntry, FlowGraph.EDGE_KEY_OTHER);
    }

    /**
     * Add an edge to the CFG from the exit of {@code p} to either the
     * entry or exit of {@code q}.
     */
    public void edge(Term p, Term q, int qEntry, FlowGraph.EdgeKey edgeKey) {
        edge(this, p, q, qEntry, edgeKey);
    }

    /**
     * Add an edge to the CFG from the exit of {@code p} to either the
     * entry or exit of {@code q}.
     */
    public void edge(CFGBuilder<FlowItem> p_visitor, Term p, Term q, int qEntry,
            FlowGraph.EdgeKey edgeKey) {
        edge(p_visitor, p, Term.EXIT, q, qEntry, edgeKey);
    }

    /**
     * Add an edge to the CFG from the exit of {@code p} to peer pq.
     */
    public void edge(CFGBuilder<FlowItem> p_visitor, Term p, Peer<FlowItem> pq,
            FlowGraph.EdgeKey edgeKey) {
        Peer<FlowItem> pp = graph.peer(p, p_visitor.path_to_finally, Term.EXIT);
        edge(pp, pq, edgeKey);
    }

    /**
     * @param p_visitor The visitor used to create p ("this" is the visitor
     *                  that created q)
     * @param p The predecessor node in the forward graph
     * @param pEntry whether we are working with the entry or exit of p. Can be
     *      Term.ENTRY or Term.EXIT.
     * @param q The successor node in the forward graph
     * @param qEntry whether we are working with the entry or exit of q. Can be
     *      Term.ENTRY or Term.EXIT.
     */
    public void edge(CFGBuilder<FlowItem> p_visitor, Term p, int pEntry, Term q,
            int qEntry, FlowGraph.EdgeKey edgeKey) {

        Peer<FlowItem> pp = graph.peer(p, p_visitor.path_to_finally, pEntry);
        Peer<FlowItem> pq = graph.peer(q, path_to_finally, qEntry);
        edge(pp, pq, edgeKey);
    }

    protected void edge(Peer<FlowItem> pp, Peer<FlowItem> pq,
            FlowGraph.EdgeKey edgeKey) {
        if (Report.should_report(Report.cfg, 2))
            Report.report(2, "//     edge " + pp.node() + " -> " + pq.node());

        if (Report.should_report(Report.cfg, 3)) {
            // at level 3, use Peer.toString() as the label for the nodes
            Report.report(2,
                          pp.hashCode() + " [ label = \""
                                  + StringUtil.escape(pp.toString()) + "\" ];");
            Report.report(2,
                          pq.hashCode() + " [ label = \""
                                  + StringUtil.escape(pq.toString()) + "\" ];");
        }
        else if (Report.should_report(Report.cfg, 2)) {
            // at level 2, use Node.toString() as the label for the nodes
            // which is more readable than Peer.toString(), but not as unique.
            Report.report(2,
                          pp.hashCode() + " [ label = \""
                                  + StringUtil.escape(pp.node.toString())
                                  + "\" ];");
            Report.report(2,
                          pq.hashCode() + " [ label = \""
                                  + StringUtil.escape(pq.node.toString())
                                  + "\" ];");
        }

        if (graph.forward()) {
            if (Report.should_report(Report.cfg, 2)) {
                Report.report(2,
                              pp.hashCode() + " -> " + pq.hashCode()
                                      + " [label=\"" + edgeKey + "\"];");
            }
            pp.succs.add(new Edge<>(edgeKey, pq));
            pq.preds.add(new Edge<>(edgeKey, pp));
        }
        else {
            if (Report.should_report(Report.cfg, 2)) {
                Report.report(2,
                              pq.hashCode() + " -> " + pp.hashCode()
                                      + " [label=\"" + edgeKey + "\"];");
            }
            pq.succs.add(new Edge<>(edgeKey, pp));
            pp.preds.add(new Edge<>(edgeKey, pq));
        }
    }

    /**
     * Should the CFG construction skip dead if branches? (e.g., should statement s be
     * skipped in the following: if (false) { S } ). Different dataflow analyses require
     * different behavior.
     */
    public boolean skipDeadIfBranches() {
        return this.skipDeadIfBranches;
    }

    public CFGBuilder<FlowItem> skipDeadIfBranches(boolean b) {
        if (b == this.skipDeadIfBranches) {
            return this;
        }
        CFGBuilder<FlowItem> v = copy();
        v.skipDeadIfBranches = b;
        return v;
    }

    /**
     * Should the CFG construction skip dead loop bodies? (e.g., should statement s be
     * skipped in the following: while (false) { S } ). Different dataflow analyses require
     * different behavior.
     */
    public boolean skipDeadLoopBodies() {
        return this.skipDeadLoopBodies;
    }

    public CFGBuilder<FlowItem> skipDeadLoopBodies(boolean b) {
        if (b == this.skipDeadLoopBodies) {
            return this;
        }
        CFGBuilder<FlowItem> v = copy();
        v.skipDeadLoopBodies = b;
        return v;
    }

    public CFGBuilder<FlowItem> errorEdgesToExitNode(boolean b) {
        if (b == this.errorEdgesToExitNode) {
            return this;
        }
        CFGBuilder<FlowItem> v = copy();
        v.errorEdgesToExitNode = b;
        return v;
    }

    public CFGBuilder<FlowItem> exceptionEdgesToFinally(boolean b) {
        if (b == this.exceptionEdgesToFinally) {
            return this;
        }
        CFGBuilder<FlowItem> v = copy();
        v.exceptionEdgesToFinally = b;
        return v;
    }

    public CFGBuilder<FlowItem> trackImplicitErrors(boolean b) {
        if (b == this.trackImplicitErrors) {
            return this;
        }
        CFGBuilder<FlowItem> v = copy();
        v.trackImplicitErrors = b;
        return v;
    }

}
