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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import polyglot.ast.Assign;
import polyglot.ast.Binary;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;
import polyglot.ast.CodeNode;
import polyglot.ast.Conditional;
import polyglot.ast.ConstructorCall;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldAssign;
import polyglot.ast.FieldDecl;
import polyglot.ast.Formal;
import polyglot.ast.Initializer;
import polyglot.ast.Local;
import polyglot.ast.LocalAssign;
import polyglot.ast.LocalDecl;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.Term;
import polyglot.ast.Unary;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.ReferenceType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;

/**
 * A visitor that checks that variables are initialized correctly.
 */
public abstract class AbstractAssignmentChecker<
                CBI extends AbstractAssignmentChecker.ClassBodyInfo<CBI>,
                FI extends AbstractAssignmentChecker.FlowItem>
        extends DataFlow<FI> {
    public AbstractAssignmentChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(
                job,
                ts,
                nf,
                true /* forward analysis */,
                false /* perform dataflow when leaving CodeDecls, not when entering */);
    }

    protected CBI curCBI = null;

    /**
     * A data structure containing relevant information needed for performing
     * initialization checking of a class declaration. These objects form a
     * stack, since class declarations can be nested.
     */
    public abstract static class ClassBodyInfo<This extends ClassBodyInfo<This>> {
        /**
         * The {@link ClassBodyInfo} for the outer {@link ClassBody}. This forms
         * a stack.
         */
        public final This outer;

        /**
         * The current {@link CodeNode} being processed by the dataflow
         * equations.
         */
        public CodeNode curCodeDecl;

        /**
         * The current class being processed.
         */
        public final ClassType curClass;

        /**
         * Each field in the class currently being processed is mapped to its
         * corresponding {@link AssignmentStatus}. Used as the basis for the
         * maps returned in {@link #createInitialItem()}.
         */
        public final Map<FieldInstance, AssignmentStatus> curClassFieldAsgtStatuses =
                new HashMap<>();

        /**
         * List of all constructors declared in the current class. These will be
         * checked once all the initializer blocks have been processed.
         */
        public final List<ConstructorDecl> allConstructors = new ArrayList<>();

        /**
         * The constructors for the current class that call {@code this()}.
         */
        public final Set<ConstructorDecl> constructorsCallingThis = new HashSet<>();

        /**
         * The constructors for the current class that cannot terminate
         * normally. This is a subset of {@link #allConstructors}.
         */
        public final Set<ConstructorDecl> constructorsCannotTerminateNormally = new HashSet<>();

        /**
         * Maps {@link ConstructorInstance}s to the {@link FieldInstances} of
         * all instance fields initialized by the constructor.
         */
        public final Map<ConstructorInstance, Set<FieldInstance>> fieldsConstructorInitializes =
                new HashMap<>();

        /**
         * The {@link LocalInstance}s from the outer class body that are used
         * during the declaration of the current class. This is used to populate
         * {@link #localsUsedInClassBodies}.
         */
        public final Set<LocalInstance> outerLocalsUsed = new HashSet<>();

        /**
         * Maps {@link ClassBody}s to the {@link LocalInstance}s of those local
         * variables declared in the current {@link CodeNode} that are used by
         * inner classes.
         * <p>
         * If {@link #outerLocalsUsed} maps C to S, then the class body C is an
         * inner class declared in the current code declaration, and S is the
         * set of {@link LocalInstance}s that are defined in the current code
         * declaration and used in the declaration of the class C.
         * <p>
         * This information is used to ensure that local variables are
         * definitely assigned before the class declaration of C.
         */
        public final Map<ClassBody, Set<LocalInstance>> localsUsedInClassBodies = new HashMap<>();

        /**
         * The {@link LocalInstance}s that we have seen declarations for in the
         * current class. This set allows us to determine which local instances
         * are being used before they are declared (e.g., if they are used in
         * their own initialization) or are locals declared in an enclosing
         * class.
         */
        public final Set<LocalInstance> localDeclarations = new HashSet<>();

        public ClassBodyInfo(This outer, ClassType curClass) {
            this.outer = outer;
            this.curCodeDecl = null;
            this.curClass = curClass;
        }
    }

    /**
     * Represents the initialization status of a variable. A variable may be
     * definitely assigned (or not), and definitely unassigned (or not).
     */
    protected static enum AssignmentStatus {
        BOTH(true, true),
        ASSIGNED(true, false),
        UNASSIGNED(false, true),
        NEITHER(false, false);

        public final boolean definitelyAssigned;
        public final boolean definitelyUnassigned;

        AssignmentStatus(boolean definitelyAssigned, boolean definitelyUnassigned) {
            this.definitelyAssigned = definitelyAssigned;
            this.definitelyUnassigned = definitelyUnassigned;
        }

        @Override
        public String toString() {
            return "["
                    + (definitelyAssigned ? "definitely assigned " : "")
                    + (definitelyUnassigned ? "definitely unassigned " : "")
                    + "]";
        }

        public static AssignmentStatus join(AssignmentStatus as1, AssignmentStatus as2) {
            if (as1 == null) return as2;
            if (as2 == null) return as1;
            boolean defAss = as1.definitelyAssigned && as2.definitelyAssigned;
            boolean defUnass = as1.definitelyUnassigned && as2.definitelyUnassigned;
            return construct(defAss, defUnass);
        }

        private static AssignmentStatus construct(boolean defAss, boolean defUnass) {
            for (AssignmentStatus as : AssignmentStatus.values()) {
                if (as.definitelyAssigned == defAss && as.definitelyUnassigned == defUnass)
                    return as;
            }

            throw new InternalCompilerError(
                    "Unable to construct assignment "
                            + "status from (defAss="
                            + defAss
                            + ", defUnass="
                            + defUnass
                            + ")");
        }
    }

    /**
     * Represents the dataflow items for this dataflow. Items are maps of {@link
     * VarInstance}s to their corresponding {@link AssignmentStatus}. This class
     * is immutable.
     */
    public static class FlowItem extends DataFlow.Item {
        public final Map<VarInstance, AssignmentStatus> assignmentStatus;

        /**
         * Indicates whether the current path can terminate normally.
         */
        public final boolean normalTermination;

        protected FlowItem(Map<VarInstance, AssignmentStatus> map, boolean canTerminateNormally) {
            assignmentStatus = Collections.unmodifiableMap(new HashMap<>(map));
            normalTermination = canTerminateNormally;
        }

        @Override
        public String toString() {
            return assignmentStatus.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FlowItem) {
                // XXX Why is normalTermination ignored here?
                return assignmentStatus.equals(((FlowItem) o).assignmentStatus);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return assignmentStatus.hashCode();
        }
    }

    /**
     * Factory method for creating a new flow item from the given map.
     */
    protected FI newFlowItem(Map<VarInstance, AssignmentStatus> map) {
        return newFlowItem(map, true);
    }

    /**
     * Factory method for creating a new flow item from the given map.
     *
     * @param canTerminateNormally indicates whether the current path for the
     *         flow item can terminate normally.
     */
    protected abstract FI newFlowItem(
            Map<VarInstance, AssignmentStatus> map, boolean canTerminateNormally);

    /**
     * Reconstructs a flow item by replacing the assignment-status map.
     *
     * @param fi the flow item to reconstruct.
     * @param map the assignment-status map to use in the reconstructed object.
     */
    protected FI reconstructFlowItem(FI fi, Map<VarInstance, AssignmentStatus> map) {
        return newFlowItem(map, fi.normalTermination);
    }

    protected final FI BOTTOM = BOTTOM();

    /**
     * Constructs the flow item at the bottom of the lattice.
     */
    protected FI BOTTOM() {
        return newFlowItem(Collections.<VarInstance, AssignmentStatus>emptyMap());
    }

    @Override
    protected FlowGraph<FI> initGraph(CodeNode code, Term root) {
        curCBI.curCodeDecl = code;
        return new FlowGraph<>(root, forward);
    }

    /**
     * Sets up the state that must be tracked during a {@link ClassDecl}.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected NodeVisitor enterCall(Node parent, Node n) throws SemanticException {
        if (n instanceof ClassBody) {
            // Starting to process a class declaration, but haven't done any of
            // the dataflow analysis yet.

            // Set up the new CBI.
            ClassType ct = null;
            if (parent instanceof ClassDecl) {
                ct = ((ClassDecl) parent).type();
            } else if (parent instanceof New) {
                ct = ((New) parent).anonType();
            }

            if (ct == null) {
                throw new InternalCompilerError(
                        "ClassBody found but cannot " + "find the class.", n.position());
            }

            setupClassBody(ct, (ClassBody) n);
        }

        return super.enterCall(n);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof ConstructorDecl) {
            // Postpone checking of constructors until the end of the class
            // declaration, to ensure that all the initializer blocks are
            // processed first.
            curCBI.allConstructors.add((ConstructorDecl) n);
            return n;
        }

        if (n instanceof ClassBody) {
            ClassBody cb = (ClassBody) n;

            // Now that we are at the end of the class declaration, and can be
            // sure that all initializer blocks have been processed, we can now
            // process the constructors.
            try {
                for (ConstructorDecl cd : curCBI.allConstructors) {
                    // Rely on the fact that our dataflow does not change the
                    // AST, so we can discard the result at the end of this
                    // call.
                    dataflow(cd);
                }

                leaveClassBody(cb);

                // copy the locals used to the outer scope
                if (curCBI.outer != null) {
                    curCBI.outer.localsUsedInClassBodies.put(cb, curCBI.outerLocalsUsed);
                }
            } finally {
                // Pop the stack.
                curCBI = curCBI.outer;
            }
        }

        return super.leaveCall(old, n, v);
    }

    /**
     * Performs checks on the given {@link ClassBody}. This is invoked while
     * leaving the class body, after performing dataflow on the class's
     * constructors.
     */
    protected abstract void leaveClassBody(ClassBody cb) throws SemanticException;

    /**
     * Factory method for creating {@link CBI}s.
     */
    protected abstract CBI newCBI(CBI prevCBI, ClassType ct);

    /**
     * Pushes a new {@link CBI} onto the stack, and sets up {@link
     * ClassBodyInfo#curClassFieldAsgtStatuses} of the new {@link CBI} to
     * contain mappings for all the fields of the class.
     */
    protected void setupClassBody(ClassType ct, ClassBody n) throws SemanticException {
        curCBI = newCBI(curCBI, ct);

        // Set up curClassfieldAsgtStatuses to contain mappings for all fields
        // declared in the class.
        for (ClassMember cm : n.members()) {
            if (!(cm instanceof FieldDecl)) continue;

            FieldDecl fd = (FieldDecl) cm;
            AssignmentStatus asgtStatus =
                    fd.init() == null ? AssignmentStatus.UNASSIGNED : AssignmentStatus.ASSIGNED;

            // do dataflow over the initialization expression
            // to pick up any uses of outer local variables.
            if (fd.init() != null && curCBI.outer != null) dataflow(fd.init());

            curCBI.curClassFieldAsgtStatuses.put(fd.fieldInstance().orig(), asgtStatus);
        }
    }

    /**
     * Construct a flow graph for the {@code Expr} provided, and call
     * {@code dataflow(FlowGraph)}. Is also responsible for calling
     * {@code post(FlowGraph, Term)} after
     * {@code dataflow(FlowGraph)} has been called.
     * There is no need to push a CFG onto the stack, as dataflow is not
     * performed on entry in this analysis.
     */
    protected void dataflow(Expr root) throws SemanticException {
        // Build the control flow graph.
        FlowGraph<FI> g = new FlowGraph<>(root, forward);
        CFGBuilder<FI> v = createCFGBuilder(ts, g);
        v.visitGraph();
        dataflow(g);
        post(g, root);
    }

    @Override
    public FI createInitialItem(FlowGraph<FI> graph, Term node, boolean entry) {
        if (node == graph.root() && entry) {
            return createInitDFI();
        }
        return BOTTOM;
    }

    /**
     * Creates the initial dataflow item from {@link
     * ClassBodyInfo#curClassFieldAsgtStatuses}.
     */
    protected FI createInitDFI() {
        return newFlowItem(
                new HashMap<VarInstance, AssignmentStatus>(curCBI.curClassFieldAsgtStatuses));
    }

    @Override
    protected CFGBuilder<FI> createCFGBuilder(TypeSystem ts, FlowGraph<FI> g) {
        CFGBuilder<FI> v = new CFGBuilder<>(lang(), ts, g, this);
        // skip dead loops bodies and dead if branches. See JLS 2nd edition, Section 16.
        //        v = v.skipDeadIfBranches(true);
        //        v = v.skipDeadLoopBodies(true);
        return v;
    }

    /**
     * The confluence operator for {@link Initializer}s and {@link Constructor}s
     * needs to be a little special, as we are only concerned with
     * non-exceptional flows in these cases.
     * This method ensures that a slightly different confluence is performed for
     * these {@link Term}s, otherwise
     * {@link #confluence(List, Peer, FlowGraph)} is called instead.
     */
    @Override
    protected FI confluence(
            List<FI> items, List<EdgeKey> itemKeys, Peer<FI> peer, FlowGraph<FI> graph) {
        Node node = peer.node();
        if (node instanceof Initializer || node instanceof ConstructorDecl) {
            List<FI> filtered = filterItemsNonException(items, itemKeys);
            if (filtered.isEmpty()) {
                // Record the fact that this dataflow item was not produced for
                // a node that can be reached by normal termination.
                return newFlowItem(
                        new HashMap<VarInstance, AssignmentStatus>(
                                curCBI.curClassFieldAsgtStatuses),
                        false);
            }

            if (filtered.size() == 1) {
                return filtered.get(0);
            }

            return confluence(filtered, peer, graph);
        }
        return confluence(items, peer, graph);
    }

    /**
     * The confluence operator is essentially the union of all of the
     * inItems. However, if two or more of the AssignmentStatus maps from
     * the inItems each have a DefiniteAssignments entry for the same
     * VarInstance, the conflict must be resolved, by using the
     * minimum of all mins and the maximum of all maxes.
     */
    @Override
    protected FI confluence(List<FI> inItems, Peer<FI> peer, FlowGraph<FI> graph) {
        // Resolve any conflicts pairwise.
        Map<VarInstance, AssignmentStatus> m = null;
        for (FI itm : inItems) {
            if (itm == BOTTOM) continue;
            if (m == null) {
                m = new HashMap<>(itm.assignmentStatus);
            } else {
                Map<VarInstance, AssignmentStatus> n = itm.assignmentStatus;
                for (Entry<VarInstance, AssignmentStatus> e : n.entrySet()) {
                    VarInstance v = e.getKey();
                    AssignmentStatus as1 = m.get(v);
                    AssignmentStatus as2 = e.getValue();
                    m.put(v, AssignmentStatus.join(as1, as2));
                }
            }
        }

        if (m == null) return BOTTOM;

        return newFlowItem(m);
    }

    @Override
    protected Map<EdgeKey, FI> flow(
            List<FI> inItems, List<EdgeKey> inItemKeys, FlowGraph<FI> graph, Peer<FI> peer) {
        return flowToBooleanFlow(inItems, inItemKeys, graph, peer);
    }

    /**
     * Performs the appropriate flow operations for the given peer. For
     * modularity, this method delegates to other appropriate methods in this
     * class.
     * <p>
     * To summarize:
     * <ul>
     * <li>{@link Formal}: declaration of a formal parameter. Just insert a new
     * definite assignment for the {@link LocalInstance}.</li>
     * <li>{@link LocalDecl}: declaration of a local variable. If the
     * declaration has an initializer, insert a new definite assignment for the
     * {@link LocalInstance}.</li>
     * <li>{@link Assign}: if the LHS of the assignment is a local variable or a
     * field, then increment the min and max counts for that variable.</li>
     * </ul>
     */
    @Override
    protected Map<EdgeKey, FI> flow(
            FI trueItem, FI falseItem, FI otherItem, FlowGraph<FI> graph, Peer<FI> peer) {
        FI inItem =
                safeConfluence(
                        trueItem,
                        FlowGraph.EDGE_KEY_TRUE,
                        falseItem,
                        FlowGraph.EDGE_KEY_FALSE,
                        otherItem,
                        FlowGraph.EDGE_KEY_OTHER,
                        peer,
                        graph);

        Node n = peer.node();
        if (peer.isEntry()) {
            if (n instanceof LocalDecl) {
                LocalDecl ld = (LocalDecl) n;
                if (inItem.assignmentStatus.containsKey(ld.localInstance())) {
                    Map<VarInstance, AssignmentStatus> newAsgtStatus =
                            new HashMap<>(inItem.assignmentStatus);

                    newAsgtStatus.remove(ld.localInstance());
                    inItem = newFlowItem(newAsgtStatus);
                }
            }
            return itemToMap(inItem, peer.succEdgeKeys());
        }

        if (inItem == BOTTOM) {
            return itemToMap(BOTTOM, peer.succEdgeKeys());
        }

        FI inDFItem = inItem;
        Map<EdgeKey, FI> ret = null;
        if (n instanceof Formal) {
            // formal argument declaration.
            ret = flowFormal(inDFItem, graph, (Formal) n, peer.succEdgeKeys());
        } else if (n instanceof LocalDecl) {
            // local variable declaration.
            ret = flowLocalDecl(inDFItem, graph, (LocalDecl) n, peer.succEdgeKeys());
        } else if (n instanceof LocalAssign) {
            // assignment to a local variable
            ret = flowLocalAssign(inDFItem, graph, (LocalAssign) n, peer.succEdgeKeys());
        } else if (n instanceof FieldAssign) {
            // assignment to a field
            ret = flowFieldAssign(inDFItem, graph, (FieldAssign) n, peer.succEdgeKeys());
        } else if (n instanceof ConstructorCall) {
            // call to another constructor.
            ret = flowConstructorCall(inDFItem, graph, (ConstructorCall) n, peer.succEdgeKeys());
        } else if (n instanceof Expr
                && ((Expr) n).type().isBoolean()
                && (n instanceof Binary || n instanceof Conditional || n instanceof Unary)) {
            if (trueItem == null) trueItem = inDFItem;
            if (falseItem == null) falseItem = inDFItem;
            ret = flowBooleanConditions(trueItem, falseItem, inDFItem, graph, peer);
        } else {
            ret = flowOther(inDFItem, graph, n, peer.succEdgeKeys());
        }
        if (ret == null) {
            ret = itemToMap(inItem, peer.succEdgeKeys());
        }
        if (n instanceof Expr) {
            Expr e = (Expr) n;
            if (lang().isConstant(e, lang()) && e.type().isBoolean()) {
                if (Boolean.TRUE.equals(lang().constantValue(e, lang()))) {
                    // the false branch is dead
                    ret = remap(ret, FlowGraph.EDGE_KEY_FALSE, AssignmentStatus.BOTH);
                } else {
                    // the true branch is dead
                    ret = remap(ret, FlowGraph.EDGE_KEY_TRUE, AssignmentStatus.BOTH);
                }
            }
        }
        return ret;
    }

    private Map<EdgeKey, FI> remap(Map<EdgeKey, FI> m, EdgeKey ek, AssignmentStatus asgtStatus) {
        FI fi = m.get(ek);
        if (fi == null) {
            return m;
        }

        Map<VarInstance, AssignmentStatus> assignmentStatus = new HashMap<>();
        for (VarInstance vi : fi.assignmentStatus.keySet()) {
            assignmentStatus.put(vi, asgtStatus);
        }

        FI newFI = reconstructFlowItem(fi, assignmentStatus);
        Map<EdgeKey, FI> newM = new HashMap<>(m);
        newM.put(ek, newFI);
        return newM;
    }

    /**
     * Performs the appropriate flow operations for a {@link Formal}.
     */
    protected Map<EdgeKey, FI> flowFormal(
            FI inItem, FlowGraph<FI> graph, Formal f, Set<EdgeKey> succEdgeKeys) {
        Map<VarInstance, AssignmentStatus> m = new HashMap<>(inItem.assignmentStatus);
        // a formal argument is always defined.
        m.put(f.localInstance().orig(), AssignmentStatus.ASSIGNED);

        // record the fact that we have seen the formal declaration
        curCBI.localDeclarations.add(f.localInstance().orig());

        return DataFlow.<FI>itemToMap(reconstructFlowItem(inItem, m), succEdgeKeys);
    }

    /**
     * Performs the appropriate flow operations for a {@link LocalDecl}.
     */
    protected Map<EdgeKey, FI> flowLocalDecl(
            FI inItem, FlowGraph<FI> graph, LocalDecl ld, Set<EdgeKey> succEdgeKeys) {
        Map<VarInstance, AssignmentStatus> m = new HashMap<>(inItem.assignmentStatus);

        AssignmentStatus asgtStatus =
                ld.init() == null ? AssignmentStatus.UNASSIGNED : AssignmentStatus.ASSIGNED;
        m.put(ld.localInstance().orig(), asgtStatus);

        // record the fact that we have seen a local declaration
        curCBI.localDeclarations.add(ld.localInstance());

        return DataFlow.<FI>itemToMap(reconstructFlowItem(inItem, m), succEdgeKeys);
    }

    /**
     * Performs the appropriate flow operations for a {@link LocalAssign}.
     */
    protected Map<EdgeKey, FI> flowLocalAssign(
            FI inItem, FlowGraph<FI> graph, LocalAssign a, Set<EdgeKey> succEdgeKeys) {
        Local l = a.left();
        Map<VarInstance, AssignmentStatus> m = new HashMap<>(inItem.assignmentStatus);
        m.put(l.localInstance().orig(), AssignmentStatus.ASSIGNED);

        return DataFlow.<FI>itemToMap(reconstructFlowItem(inItem, m), succEdgeKeys);
    }

    /**
     * Performs the appropriate flow operations for a {@link FieldAssign}.
     */
    protected Map<EdgeKey, FI> flowFieldAssign(
            FI inItem, FlowGraph<FI> graph, FieldAssign a, Set<EdgeKey> succEdgeKeys) {
        Field f = a.left();
        FieldInstance fi = f.fieldInstance();

        // Ignore this assignment if the field's target is not appropriate for
        // what we are interested in.
        if (!isFieldsTargetAppropriate(graph, f)) return null;

        Map<VarInstance, AssignmentStatus> m = new HashMap<>(inItem.assignmentStatus);

        // m.get(fi.orig()) may be null if the field is defined in an
        // outer class. If so, ignore this assignment.
        if (m.get(fi.orig()) == null) return null;

        // The field is now definitely assigned.
        m.put(fi.orig(), AssignmentStatus.ASSIGNED);
        return DataFlow.<FI>itemToMap(reconstructFlowItem(inItem, m), succEdgeKeys);
    }

    /**
     * Performs the appropriate flow operations for a {@link ConstructorCall}.
     */
    protected Map<EdgeKey, FI> flowConstructorCall(
            FI inItem, FlowGraph<FI> graph, ConstructorCall cc, Set<EdgeKey> succEdgeKeys) {
        if (ConstructorCall.THIS.equals(cc.kind())) {
            // currCodeDecl must be a ConstructorDecl, as that
            // is the only place constructor calls are allowed
            // record the fact that the current constructor calls the other
            // constructor
            ConstructorDecl cd = (ConstructorDecl) curCBI.curCodeDecl;
            curCBI.constructorsCallingThis.add(cd);

            // Set all final non-static fields as assigned.
            Map<VarInstance, AssignmentStatus> m = new HashMap<>(inItem.assignmentStatus);
            ReferenceType container = cd.constructorInstance().container();
            for (FieldInstance fi : container.fields())
                if (fi.flags().isFinal() && !fi.flags().isStatic())
                    m.put(fi.orig(), AssignmentStatus.ASSIGNED);
            return DataFlow.itemToMap(reconstructFlowItem(inItem, m), succEdgeKeys);
        }
        return null;
    }

    /**
     * Allows subclasses to override if necessary.
     */
    protected Map<EdgeKey, FI> flowOther(
            FI inItem, FlowGraph<FI> graph, Node n, Set<EdgeKey> succEdgeKeys) {
        return null;
    }

    /**
     * Determines whether we are interested in this field on the basis of the
     * target of the field. To wit, if the field is static, then the target of
     * the field must be the current class; if the field is not static then the
     * target must be "this".
     */
    protected boolean isFieldsTargetAppropriate(FlowGraph<FI> graph, Field f) {
        ClassType containingClass = curCBI.curClass;

        if (f.fieldInstance().flags().isStatic()) {
            return containingClass.equals(f.fieldInstance().orig().container());
        } else {
            if (f.target() instanceof Special) {
                Special s = (Special) f.target();
                if (Special.THIS.equals(s.kind())) {
                    return s.qualifier() == null || containingClass.equals(s.qualifier().type());
                }
            }
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method is also responsible for maintaining state between the
     * dataflows over {@link Initializer}s, by copying back the appropriate
     * {@link AssignmentStatus}es to the map {@link
     * ClassBodyInfo#curClassFieldAsgtStatuses}.
     */
    @Override
    protected void check(
            FlowGraph<FI> graph, Term n, boolean entry, FI inItem, Map<EdgeKey, FI> outItems)
            throws SemanticException {
        FI dfIn = inItem;
        if (dfIn == null) {
            // There is no input data flow item. This can happen if we are
            // checking an unreachable term, and so no Items have flowed
            // through the term. For example, in the code fragment:
            //     a: do { break a; } while (++i < 10);
            // the expression "++i < 10" is unreachable, but since there is
            // no unreachable statement, the Java Language Spec permits it.

            // Set inItem to a default Item
            dfIn = createInitDFI();
        }

        FI dfOut = null;
        if (!entry && outItems != null && !outItems.isEmpty()) {
            // due to the flow equations, all DataFlowItems in the outItems map
            // are the same, so just take the first one.
            dfOut = outItems.values().iterator().next();
            if (n instanceof Field) {
                checkField(graph, (Field) n, dfIn);
            } else if (n instanceof Local) {
                checkLocal(graph, (Local) n, dfIn);
            } else if (n instanceof LocalAssign) {
                checkLocalAssign(
                        graph, ((LocalAssign) n).left().localInstance(), n.position(), dfIn);
            } else if (n instanceof LocalDecl) {
                checkLocalAssign(graph, ((LocalDecl) n).localInstance(), n.position(), dfIn);
            } else if (n instanceof FieldAssign) {
                checkFieldAssign(graph, (FieldAssign) n, dfIn);
            } else if (n instanceof ClassBody) {
                checkClassBody(graph, (ClassBody) n, dfIn, dfOut);
            } else {
                checkOther(graph, n, dfIn, dfOut);
            }
        } else {
            // this local assign node has not had data flow performed over it.
            // probably a node in a finally block. Just ignore it.
        }

        if (n == graph.root() && !entry) {
            if (curCBI.curCodeDecl instanceof FieldDecl) {
                finishFieldDecl(graph, (FieldDecl) curCBI.curCodeDecl, dfIn, dfOut);
            }
            if (curCBI.curCodeDecl instanceof ConstructorDecl) {
                finishConstructorDecl(graph, (ConstructorDecl) curCBI.curCodeDecl, dfIn, dfOut);
            }
            if (curCBI.curCodeDecl instanceof Initializer) {
                finishInitializer(graph, (Initializer) curCBI.curCodeDecl, dfIn, dfOut);
            }
        }
    }

    /**
     * Checks that the given {@link Field} is used correctly.
     */
    protected abstract void checkField(FlowGraph<FI> graph, Field f, FI dfIn)
            throws SemanticException;

    /**
     * Checks that the given {@link Local} is used correctly.
     */
    protected abstract void checkLocal(FlowGraph<FI> graph, Local l, FI dfIn)
            throws SemanticException;

    /**
     * Checks that an assignment to the given {@link LocalInstance} is correct.
     */
    protected abstract void checkLocalAssign(
            FlowGraph<FI> graph, LocalInstance li, Position pos, FI dfIn) throws SemanticException;

    /**
     * Checks that the given {@link FieldAssign} is correct.
     */
    protected abstract void checkFieldAssign(FlowGraph<FI> graph, FieldAssign a, FI dfIn)
            throws SemanticException;

    /**
     * Checks that the usage of locals in the given {@link ClassBody} (which
     * represents an inner class) is correct.
     */
    protected void checkClassBody(FlowGraph<FI> graph, ClassBody cb, FI dfIn, FI dfOut)
            throws SemanticException {
        // we need to check that the locals used inside this class body
        // have all been defined at this point.
        Set<LocalInstance> localsUsed = curCBI.localsUsedInClassBodies.get(cb);

        if (localsUsed != null) {
            checkLocalsUsedByInnerClass(graph, cb, localsUsed, dfIn, dfOut);
        }
    }

    /**
     * Checks that the given set of {@link LocalInstance} (which is the set of
     * locals used in the inner class having the given {@link ClassBody})
     * are initialized before the class declaration.
     */
    protected abstract void checkLocalsUsedByInnerClass(
            FlowGraph<FI> graph, ClassBody cb, Set<LocalInstance> localsUsed, FI dfIn, FI dfOut)
            throws SemanticException;

    /**
     * Allow subclasses to override the checking of other nodes, if needed.
     * @throws SemanticException
     */
    protected void checkOther(FlowGraph<FI> graph, Node n, FI dfIn, FI dfOut)
            throws SemanticException {}

    /**
     * Performs the necessary actions upon finishing the checking of the given
     * {@link FieldDecl}.
     * <p>
     * The default implementation copies the {@link AssignmentStatus} of any
     * fields into {@link ClassBodyInfo#curClassFieldAsgtStatuses} so that they
     * are correct for the next field declaration, initializer, or constructor.
     */
    protected void finishFieldDecl(FlowGraph<FI> graph, FieldDecl fd, FI dfIn, FI dfOut) {
        for (Entry<VarInstance, AssignmentStatus> e : dfOut.assignmentStatus.entrySet()) {
            if (!(e.getKey() instanceof FieldInstance)) continue;

            FieldInstance fi = (FieldInstance) e.getKey();
            if (fi.flags().isFinal()) {
                // we don't need to join the init counts, as all
                // dataflows will go through all of the
                // initializers
                curCBI.curClassFieldAsgtStatuses.put(fi.orig(), e.getValue());
            }
        }
    }

    /**
     * Performs the necessary actions upon finishing the checking of the given
     * {@link ConstructorDecl}.
     * <p>
     * The default implementation updates the state of {@link
     * ClassBodyInfo#fieldsConstructorInitializes}.
     */
    protected void finishConstructorDecl(
            FlowGraph<FI> graph, ConstructorDecl cd, FI dfIn, FI dfOut) {
        ConstructorInstance ci = cd.constructorInstance();

        // We need to set currCBI.fieldsConstructorInitializes correctly. It is
        // meant to contain all the non-static fields that the constructor ci
        // initializes.
        //
        // Note that dfOut.initStatus contains only the AssignmentStatuses for
        // _normal_ termination of the constructor (see the method confluence).
        // This means that if dfOut says the min count of the initialization for
        // a final non-static field is one, and that is different from what is
        // recorded in curCBI.curClassFieldAsgtStatuses (which is the statuses
        // resulting from the initializers), then the constructor does indeed
        // initialize the field.

        Set<FieldInstance> s = new HashSet<>();

        // Go through every non-static field in dfOut.assignmentStatus.
        for (Entry<VarInstance, AssignmentStatus> e : dfOut.assignmentStatus.entrySet()) {
            if (!(e.getKey() instanceof FieldInstance)) continue;

            FieldInstance fi = (FieldInstance) e.getKey();
            if (fi.flags().isStatic()) continue;

            AssignmentStatus asgtStatus = e.getValue();
            AssignmentStatus origAsgtStatus = curCBI.curClassFieldAsgtStatuses.get(fi);
            if (asgtStatus.definitelyAssigned && !origAsgtStatus.definitelyAssigned) {
                // the constructor initialized this field
                s.add(fi);
            }
        }

        if (!s.isEmpty()) {
            curCBI.fieldsConstructorInitializes.put(ci.orig(), s);
        }

        if (!dfIn.normalTermination) {
            // this ci cannot terminate normally. Record this fact.
            curCBI.constructorsCannotTerminateNormally.add(cd);
        }
    }

    /**
     * Perform necessary actions upon finishing the checking of the given
     * {@link Initializer}.
     * <p>
     * The default implementation copies the {@link AssignmentStatus} of any
     * fields into {@link ClassBodyInfo#curClassFieldAsgtStatuses} so that they
     * are correct for the next field declaration, initializer, or constructor.
     */
    protected void finishInitializer(
            FlowGraph<FI> graph, Initializer initializer, FI dfIn, FI dfOut) {
        for (Entry<VarInstance, AssignmentStatus> e : dfOut.assignmentStatus.entrySet()) {
            if (!(e.getKey() instanceof FieldInstance)) continue;

            FieldInstance fi = (FieldInstance) e.getKey();
            // we don't need to join the init counts, as all
            // dataflows will go through all of the
            // initializers
            curCBI.curClassFieldAsgtStatuses.put(fi.orig(), e.getValue());
        }
    }
}
