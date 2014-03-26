/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package coffer.visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.ProcedureDecl;
import polyglot.ast.Term;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeObject;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.DataFlow;
import polyglot.visit.FlowGraph;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.ExceptionEdgeKey;
import polyglot.visit.FlowGraph.Peer;
import coffer.Topics;
import coffer.extension.CofferExt;
import coffer.extension.ProcedureDeclExt_c;
import coffer.types.CofferClassType;
import coffer.types.CofferProcedureInstance;
import coffer.types.CofferTypeSystem;
import coffer.types.KeySet;
import coffer.types.ThrowConstraint;

/**
 * Data flow analysis to compute and check held key sets.
 */
public class KeyChecker extends DataFlow<DataFlow.Item> {
    public KeyChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf, true /* forward analysis */);
        CofferTypeSystem vts = (CofferTypeSystem) ts;
        EMPTY = vts.emptyKeySet(Position.COMPILER_GENERATED);
    }

    @Override
    public Item createInitialItem(FlowGraph<Item> graph, Term node,
            boolean entry) {
        ProcedureDecl decl = (ProcedureDecl) graph.root();
        CofferProcedureInstance pi =
                (CofferProcedureInstance) decl.procedureInstance();

        CofferClassType t = (CofferClassType) pi.container();

        KeySet held = pi.entryKeys();
        KeySet stored = EMPTY;

        if (t.key() != null) {
            stored = stored.add(t.key());
            stored = stored.retainAll(held);
        }

        return new DataFlowItem(held, held, stored, stored);
    }

    KeySet EMPTY;

    class ExitTermItem extends Item {
        DataFlowItem nonExItem;
        Map<ExceptionEdgeKey, DataFlowItem> excEdgesToItems; // map from ExceptionEdgeKeys to DataFlowItems

        public ExitTermItem(DataFlowItem nonExItem,
                Map<ExceptionEdgeKey, DataFlowItem> excItems) {
            this.nonExItem = nonExItem;
            this.excEdgesToItems = excItems;
        }

        @Override
        public boolean equals(Object i) {
            if (i instanceof ExitTermItem) {
                ExitTermItem that = (ExitTermItem) i;
                return this.excEdgesToItems.equals(that.excEdgesToItems)
                        && this.nonExItem.equals(that.nonExItem);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return nonExItem.hashCode() + excEdgesToItems.hashCode();
        }
    }

    class DataFlowItem extends Item {
        // keys that must/may be held at this point
        KeySet must_held;
        KeySet may_held;

        // keys that must/may be stored at this point
        KeySet must_stored;
        KeySet may_stored;

        private DataFlowItem() {
            this(EMPTY, EMPTY, EMPTY, EMPTY);
        }

        private DataFlowItem(KeySet must_held, KeySet may_held,
                KeySet must_stored, KeySet may_stored) {
            this.must_held = must_held;
            this.may_held = may_held;
            this.must_stored = must_stored;
            this.may_stored = may_stored;
        }

        @Override
        public String toString() {
            return "held_keys(must_held=" + must_held + ", " + "may_held="
                    + may_held + ", " + "must_stored=" + must_stored + ", "
                    + "may_stored=" + may_stored + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                DataFlowItem that = (DataFlowItem) o;
                return this.must_held.equals(that.must_held)
                        && this.may_held.equals(that.may_held)
                        && this.must_stored.equals(that.must_stored)
                        && this.may_stored.equals(that.may_stored);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return must_held.hashCode() + may_held.hashCode()
                    + must_stored.hashCode() + may_stored.hashCode();
        }
    }

    @Override
    public Map<EdgeKey, Item> flow(Item in, FlowGraph<Item> graph,
            Peer<Item> peer) {
        if (peer.isEntry()) return itemToMap(in, peer.succEdgeKeys());
        if (in instanceof ExitTermItem) {
            return itemToMap(in, peer.succEdgeKeys());
        }

        DataFlowItem df = (DataFlowItem) in;
        Node n = peer.node();
        if (n.ext() instanceof CofferExt) {
            CofferExt ext = (CofferExt) n.ext();

            Map<EdgeKey, Item> m = new HashMap<EdgeKey, Item>();

            for (EdgeKey e : peer.succEdgeKeys()) {
                Type t = null;

                if (e instanceof FlowGraph.ExceptionEdgeKey) {
                    t = ((FlowGraph.ExceptionEdgeKey) e).type();
                }

                KeySet must_held = ext.keyFlow(df.must_held, t);
                KeySet may_held = ext.keyFlow(df.may_held, t);
                KeySet must_stored = ext.keyAlias(df.must_stored, t);
                KeySet may_stored = ext.keyAlias(df.may_stored, t);

                must_stored = must_stored.retainAll(must_held);
                may_stored = may_stored.retainAll(may_held);

                DataFlowItem newdf =
                        new DataFlowItem(must_held,
                                         may_held,
                                         must_stored,
                                         may_stored);

                if (Report.should_report(Topics.keycheck, 2)) {
                    Report.report(2, "flow(" + n + "):");
                    Report.report(2, "   " + df);
                    Report.report(2, " ->" + newdf);
                }

                m.put(e, newdf);
            }

            return m;
        }

        return itemToMap(in, peer.succEdgeKeys());
    }

    @Override
    protected Item safeConfluence(List<Item> items, List<EdgeKey> itemKeys,
            Peer<Item> peer, FlowGraph<Item> graph) {
        if (!peer.isEntry() && graph.root().equals(peer.node())) {
            return confluenceExitTerm(items, itemKeys, graph);
        }
        return super.safeConfluence(items, itemKeys, peer, graph);
    }

    @Override
    protected Item confluence(List<Item> items, List<EdgeKey> itemKeys,
            Peer<Item> peer, FlowGraph<Item> graph) {
        if (!peer.isEntry() && graph.root().equals(peer.node())) {
            return confluenceExitTerm(items, itemKeys, graph);
        }
        return confluence(items, peer, graph);
    }

    protected Item confluenceExitTerm(List<Item> items, List<EdgeKey> itemKeys,
            FlowGraph<Item> graph) {
        List<Item> nonExcItems = filterItemsNonException(items, itemKeys);
        DataFlowItem nonExc;

        if (nonExcItems.isEmpty()) {
            nonExc = new DataFlowItem();
        }
        else {
            nonExc =
                    confluence(nonExcItems,
                               graph.peer(graph.root(), false),
                               graph);
        }

        Map<ExceptionEdgeKey, List<Item>> excItemLists =
                new HashMap<ExceptionEdgeKey, List<Item>>();
        Iterator<Item> i = items.iterator();
        Iterator<EdgeKey> j = itemKeys.iterator();
        while (i.hasNext() && j.hasNext()) {
            FlowGraph.EdgeKey key = j.next();
            DataFlowItem item = (DataFlowItem) i.next();
            if (key instanceof ExceptionEdgeKey) {
                List<Item> l = excItemLists.get(key);
                if (l == null) {
                    l = new ArrayList<Item>();
                    excItemLists.put((ExceptionEdgeKey) key, l);
                }
                l.add(item);
            }
        }

        Map<ExceptionEdgeKey, DataFlowItem> excItems =
                new HashMap<ExceptionEdgeKey, DataFlowItem>(excItemLists.size());
        for (Entry<ExceptionEdgeKey, List<Item>> e : excItemLists.entrySet()) {
            excItems.put(e.getKey(),
                         confluence(e.getValue(),
                                    graph.peer(graph.root(), false),
                                    graph));
        }
        return new ExitTermItem(nonExc, excItems);
    }

    @Override
    protected DataFlowItem confluence(List<Item> inItems, Peer<Item> peer,
            FlowGraph<Item> graph) {
        DataFlowItem outItem = null;

        for (Item item : inItems) {
            DataFlowItem df = (DataFlowItem) item;

            if (outItem == null) {
                outItem =
                        new DataFlowItem(df.must_held,
                                         df.may_held,
                                         df.must_stored,
                                         df.may_stored);
                continue;
            }

            outItem.must_held = outItem.must_held.retainAll(df.must_held);
            outItem.may_held = outItem.may_held.addAll(df.may_held);

            outItem.must_stored = outItem.must_stored.retainAll(df.must_stored);
            outItem.may_stored = outItem.may_stored.addAll(df.may_stored);

            outItem.must_stored =
                    outItem.must_stored.retainAll(outItem.must_held);
            outItem.may_stored = outItem.may_stored.retainAll(outItem.may_held);
        }

        if (outItem == null)
            throw new InternalCompilerError("confluence called with insufficient input items.");

        if (Report.should_report(Topics.keycheck, 2)) {
            Report.report(2, "confluence(" + peer.node() + "):");

            for (Item df : inItems) {
                Report.report(2, "   " + df);
            }

            Report.report(2, " ->" + outItem);
        }

        return outItem;
    }

    @Override
    public void check(FlowGraph<Item> graph, Term n, boolean entry,
            Item inItem, Map<EdgeKey, Item> outItems) throws SemanticException {
        if (!entry) {
            if (graph.root().equals(n)) {
                checkExitTerm(graph, (ExitTermItem) inItem);
            }
            else {
                DataFlowItem df = (DataFlowItem) inItem;
                check(n, df, true);
            }
        }
    }

    private void check(Term n, DataFlowItem df, boolean checkHeldKeys)
            throws SemanticException {
        if (df == null) {
            return;
        }

        if (Report.should_report(Topics.keycheck, 2)) {
            Report.report(2, "check(" + n + "):");
            Report.report(2, "   " + df);
        }

        if (!df.must_held.containsAll(df.may_held)) {
            KeySet s = df.may_held.removeAll(df.must_held);
            throw new SemanticException("Keys " + s + " may not be held.",
                                        n.position());
        }

        if (!df.must_stored.containsAll(df.may_stored)) {
            KeySet s = df.may_stored.removeAll(df.must_stored);
            throw new SemanticException("Keys " + s + " may not be saved"
                    + " in a local variable.", n.position());
        }

        if (checkHeldKeys && n.ext() instanceof CofferExt) {
            CofferExt ext = (CofferExt) n.ext();
            ext.checkHeldKeys(df.must_held, df.must_stored);
        }
    }

    private void checkExitTerm(FlowGraph<Item> graph, ExitTermItem item)
            throws SemanticException {
        check(graph.root(), item.nonExItem, true);

        List<TypeObject> excepts;
        ProcedureDeclExt_c ext = null;

        if (graph.root() instanceof ProcedureDecl) {
            ProcedureDecl pd = (ProcedureDecl) graph.root();
            CofferProcedureInstance pi =
                    (CofferProcedureInstance) pd.procedureInstance();
            excepts = new ArrayList<TypeObject>(pi.throwConstraints());
            ext = (ProcedureDeclExt_c) pd.ext();
        }
        else {
            excepts = new ArrayList<TypeObject>();
            for (ExceptionEdgeKey key : item.excEdgesToItems.keySet()) {
                excepts.add(key.type());
            }
        }

        List<EdgeKey> excKeys = new ArrayList<EdgeKey>();
        List<Item> excItems = new ArrayList<Item>();

        for (Entry<ExceptionEdgeKey, DataFlowItem> e : item.excEdgesToItems.entrySet()) {
            excKeys.add(e.getKey());
            excItems.add(e.getValue());
        }

        for (TypeObject t : excepts) {
            ThrowConstraint tc = null;
            Type excType = null;
            if (t instanceof ThrowConstraint) {
                tc = (ThrowConstraint) t;
                excType = tc.throwType();
            }
            else {
                excType = (Type) t;
            }
            List<Item> matchingExc =
                    filterItemsExceptionSubclass(excItems, excKeys, excType);
            if (!matchingExc.isEmpty()) {
                DataFlowItem df =
                        confluence(matchingExc,
                                   graph.peer(graph.root(), false),
                                   graph);
                check(graph.root(), df, false);

                if (ext != null && tc != null) {
                    ext.checkHeldKeysThrowConstraint(tc,
                                                     df.must_held,
                                                     df.must_stored);
                }

            }
        }

    }
}
