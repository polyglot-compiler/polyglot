package polyglot.ext.coffer.visit;

import polyglot.ext.coffer.types.*;
import polyglot.ext.coffer.ast.*;
import polyglot.ext.coffer.extension.*;
import polyglot.ext.coffer.Topics;
import polyglot.main.Report;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import polyglot.visit.*;
import java.util.*;

/**
 * Data flow analysis to compute and check held key sets.
 */
public class KeyChecker extends DataFlow
{
    public KeyChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf, true /* forward analysis */);
        CofferTypeSystem vts = (CofferTypeSystem) ts;
        EMPTY = vts.emptyKeySet(Position.COMPILER_GENERATED);
    }

    public Item createInitialItem(FlowGraph graph) {
        ProcedureDecl decl = (ProcedureDecl) graph.root();
        CofferProcedureInstance pi = (CofferProcedureInstance)
            decl.procedureInstance();

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
        
        public String toString() {
            return "held_keys(must_held=" + must_held + ", " +
                             "may_held=" + may_held + ", " +
                             "must_stored=" + must_stored + ", " +
                             "may_stored=" + may_stored + ")";
        }
        
        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.must_held.equals(((DataFlowItem)o).must_held)
                    && this.may_held.equals(((DataFlowItem)o).may_held)
                    && this.must_stored.equals(((DataFlowItem)o).must_stored)
                    && this.may_stored.equals(((DataFlowItem)o).may_stored);
            }
            return false;
        }

        public int hashCode() {
            return must_held.hashCode() + may_held.hashCode() +
                   must_stored.hashCode() + may_stored.hashCode();
        }
    }

    public Map flow(Item in, FlowGraph graph, Term n, Set succEdgeKeys) {
        DataFlowItem df = (DataFlowItem) in;

        if (n.ext() instanceof CofferExt) {
            CofferExt ext = (CofferExt) n.ext();

            Map m = new HashMap();

            for (Iterator i = succEdgeKeys.iterator(); i.hasNext(); ) {
                FlowGraph.EdgeKey e = (FlowGraph.EdgeKey) i.next();
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

                DataFlowItem newdf = new DataFlowItem(must_held, may_held,
                                                      must_stored, may_stored);

                if (Report.should_report(Topics.keycheck, 2)) {
                    Report.report(2, "flow(" + n + "):");
                    Report.report(2, "   " + df);
                    Report.report(2, " ->" + newdf);
                }

                m.put(e, newdf);
            }

            return m;
        }

        return itemToMap(in, succEdgeKeys);
    }

    public Item confluence(List inItems, Term node) {
        CofferTypeSystem vts = (CofferTypeSystem) KeyChecker.this.ts;

        DataFlowItem outItem = null;

        for (Iterator i = inItems.iterator(); i.hasNext(); ) {
            DataFlowItem df = (DataFlowItem) i.next();

            if (outItem == null) {
                outItem = df;
                continue;
            }

            outItem.must_held = outItem.must_held.retainAll(df.must_held);
            outItem.may_held = outItem.may_held.addAll(df.may_held);

            outItem.must_stored = outItem.must_stored.retainAll(df.must_stored);
            outItem.may_stored = outItem.may_stored.addAll(df.may_stored);

            outItem.must_stored = outItem.must_stored.retainAll(outItem.must_held);
            outItem.may_stored = outItem.may_stored.retainAll(outItem.may_held);
        }

        if (outItem == null)
            throw new InternalCompilerError("confluence called with insufficient input items.");

        if (Report.should_report(Topics.keycheck, 2)) {
            Report.report(2, "confluence(" + node + "):");

            for (Iterator i = inItems.iterator(); i.hasNext(); ) {
                DataFlowItem df = (DataFlowItem) i.next();
                Report.report(2, "   " + df);
            }

            Report.report(2, " ->" + outItem);
        }

        return outItem;
    }

    public void check(FlowGraph graph, Term n, Item inItem, Map outItems)
        throws SemanticException
    {
        DataFlowItem df = (DataFlowItem) inItem;

        if (df == null)
            return;

        if (Report.should_report(Topics.keycheck, 2)) {
            Report.report(2, "check(" + n + "):");
            Report.report(2, "   " + df);
        }

        if (! df.must_held.containsAll(df.may_held)) {
            KeySet s = df.may_held.removeAll(df.must_held);
            throw new SemanticException("Keys " + s + " may not be held.",
                                        n.position());
        }

        if (! df.must_stored.containsAll(df.may_stored)) {
            KeySet s = df.may_stored.removeAll(df.must_stored);
            throw new SemanticException("Keys " + s + " may not be saved" +
                                        " in a local variable.", n.position());
        }

        if (n.ext() instanceof CofferExt) {
            CofferExt ext = (CofferExt) n.ext();
            ext.checkHeldKeys(df.must_held, df.must_stored);
        }
    }
}
