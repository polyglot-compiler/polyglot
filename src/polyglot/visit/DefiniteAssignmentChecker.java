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
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.visit.FlowGraph.EdgeKey;
import polyglot.visit.FlowGraph.Peer;

/**
 * Visitor which checks that all local variables must be defined before use, 
 * and that final variables and fields are initialized correctly.
 * 
 * The checking of the rules is implemented in the methods leaveCall(Node)
 * and check(FlowGraph, Term, Item, Item).
 * 
 * If language extensions have new constructs that use local variables, they can
 * override the method {@code checkOther} to check that the uses of these
 * local variables are correctly initialized. (The implementation of the method will
 * probably call checkLocalInstanceInit to see if the local used is initialized).
 * 
 * If language extensions have new constructs that assign to local variables,
 * they can override the method {@code flowOther} to capture the way 
 * the new construct's initialization behavior.
 * 
 */
public class DefiniteAssignmentChecker extends
        DataFlow<DefiniteAssignmentChecker.FlowItem> {
    public DefiniteAssignmentChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf, true /* forward analysis */, false /* perform dataflow when leaving CodeDecls, not when entering */);
    }

    protected ClassBodyInfo currCBI = null;

    /**
     * This class is just a data structure containing relevant information
     * needed for performing initialization checking of a class declaration.
     * 
     * These objects form a stack, since class declarations can be nested.
     */
    protected static class ClassBodyInfo {
        /** 
         * The info for the outer ClassBody. The {@code ClassBodyInfo}s
         * form a stack. 
         */
        public ClassBodyInfo outer = null;

        /** The current CodeNode being processed by the dataflow equations */
        public CodeNode currCodeDecl = null;

        /** The current class being processed. */
        public ClassType currClass = null;

        /** 
         * A Map of all the final fields in the class currently being processed
         * to DefiniteAssignments. This Map is used as the basis for the Maps returned
         * in createInitialItem(). 
         * */
        public Map<FieldInstance, AssignmentStatus> currClassFinalFieldAssStatuses =
                new HashMap<>();
        /**
         * List of all the constructors. These will be checked once all the
         * initializer blocks have been processed.
         */
        public List<ConstructorDecl> allConstructors = new ArrayList<>();

        /**
         * Set of all constructors that cannot terminate normally. This is a subset
         * of the complete list of constructors, i.e. of this.allConstructors.
         */
        public Set<ConstructorDecl> constructorsCannotTerminateNormally =
                new HashSet<>();

        /**
         * Map from ConstructorInstances to ConstructorInstances detailing
         * which constructors call which constructors.
         * This is used in checking the initialization of final fields.
         */
        public Map<ConstructorInstance, ConstructorInstance> constructorCalls =
                new HashMap<>();

        /**
         * Map from ConstructorInstances to Sets of FieldInstances, detailing
         * which final non-static fields each constructor initializes. 
         * This is used in checking the initialization of final fields.
         */
        public Map<ConstructorInstance, Set<FieldInstance>> fieldsConstructorInitializes =
                new HashMap<>();

        /**
         * Set of LocalInstances from the outer class body that were used
         * during the declaration of this class. We need to track this
         * in order to correctly populate {@code localsUsedInClassBodies}
         */
        public Set<LocalInstance> outerLocalsUsed = new HashSet<>();

        /**
         * Map from {@code ClassBody}s to {@code Set}s of 
         * {@code LocalInstance}s. If localsUsedInClassBodies(C) = S, then
         * the class body C is an inner class declared in the current code 
         * declaration, and S is the set of LocalInstances that are defined
         * in the current code declaration, but are used in the declaration
         * of the class C. We need this information in order to ensure that
         * these local variables are definitely assigned before the class
         * declaration of C. 
         */
        public Map<ClassBody, Set<LocalInstance>> localsUsedInClassBodies =
                new HashMap<>();

        /**
         * Set of LocalInstances that we have seen declarations for in this 
         * class. This set allows us to determine which local instances
         * are simply being used before they are declared (if they are used in
         * their own initialization) or are locals declared in an enclosing 
         * class.
         */
        public Set<LocalInstance> localDeclarations = new HashSet<>();
    }

    /**
     * Class representing the initialization counts of variables. The
     * different values of the counts that we are interested in are ZERO,
     * ONE and MANY.
     */
    protected static class AssignmentStatus {
        public static final AssignmentStatus ASS_UNASS =
                new AssignmentStatus(true, true);
        public static final AssignmentStatus ASS = new AssignmentStatus(true,
                                                                        false);
        public static final AssignmentStatus UNASS =
                new AssignmentStatus(false, true);
        public static final AssignmentStatus NEITHER =
                new AssignmentStatus(false, false);

        public final boolean definitelyAssigned;
        public final boolean definitelyUnassigned;

        protected AssignmentStatus(boolean definitelyAssigned,
                boolean definitelyUnassigned) {
            this.definitelyAssigned = definitelyAssigned;
            this.definitelyUnassigned = definitelyUnassigned;
        }

        @Override
        public int hashCode() {
            return Boolean.valueOf(definitelyAssigned).hashCode()
                    ^ Boolean.valueOf(definitelyUnassigned).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof AssignmentStatus) {
                return this.definitelyAssigned == ((AssignmentStatus) o).definitelyAssigned
                        && this.definitelyUnassigned == ((AssignmentStatus) o).definitelyUnassigned;
            }
            return false;
        }

        @Override
        public String toString() {
            return "["
                    + (this.definitelyAssigned ? "definitely assigned " : "")
                    + (this.definitelyUnassigned
                            ? "definitely unassigned " : "") + "]";
        }

        public static AssignmentStatus join(AssignmentStatus as1,
                AssignmentStatus as2) {
            if (as1 == null) return as2;
            if (as2 == null) return as1;
            boolean defAss = as1.definitelyAssigned && as2.definitelyAssigned;
            boolean defUnass =
                    as1.definitelyUnassigned && as2.definitelyUnassigned;
            return construct(defAss, defUnass);
        }

        private static AssignmentStatus construct(boolean defAss,
                boolean defUnass) {
            if (defAss && defUnass) return ASS_UNASS;
            if (defAss && !defUnass) return ASS;
            if (!defAss && defUnass) return UNASS;
            return NEITHER;
        }
    }

    /**
     * Dataflow items for this dataflow are maps of VarInstances to counts
     * of the min and max number of times those variables/fields have
     * been initialized. These min and max counts are then used to determine
     * if variables have been initialized before use, and that final variables
     * are not initialized too many times.
     * 
     * This class is immutable.
     */
    protected static class FlowItem extends DataFlow.Item {
        public Map<VarInstance, AssignmentStatus> assignmentStatus;
        public final boolean normalTermination;

        FlowItem(Map<VarInstance, AssignmentStatus> m) {
            this.assignmentStatus = Collections.unmodifiableMap(m);
            this.normalTermination = true;
        }

        FlowItem(Map<VarInstance, AssignmentStatus> m,
                boolean canTerminateNormally) {
            this.assignmentStatus = Collections.unmodifiableMap(m);
            this.normalTermination = canTerminateNormally;
        }

        @Override
        public String toString() {
            return assignmentStatus.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FlowItem) {
                return this.assignmentStatus.equals(((FlowItem) o).assignmentStatus);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (assignmentStatus.hashCode());
        }

    }

    protected static final FlowItem BOTTOM =
            new FlowItem(Collections.<VarInstance, AssignmentStatus> emptyMap());

    /**
     * Initialize the FlowGraph to be used in the dataflow analysis.
     * @return null if no dataflow analysis should be performed for this
     *         code declaration; otherwise, an appropriately initialized
     *         FlowGraph.
     */
    @Override
    protected FlowGraph<FlowItem> initGraph(CodeNode code, Term root) {
        currCBI.currCodeDecl = code;
        return new FlowGraph<>(root, forward);
    }

    /**
     * Overridden superclass method.
     * 
     * Set up the state that must be tracked during a Class Declaration.
     */
    @Override
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        if (n instanceof ClassBody) {
            // we are starting to process a class declaration, but have yet
            // to do any of the dataflow analysis.

            // set up the new ClassBodyInfo, and make sure that it forms
            // a stack.
            ClassType ct = null;
            if (parent instanceof ClassDecl) {
                ct = ((ClassDecl) parent).type();
            }
            else if (parent instanceof New) {
                ct = ((New) parent).anonType();
            }
            if (ct == null) {
                throw new InternalCompilerError("ClassBody found but cannot find the class.",
                                                n.position());
            }
            setupClassBody(ct, (ClassBody) n);
        }

        return super.enterCall(n);
    }

    /**
     * Postpone the checking of constructors until the end of the class 
     * declaration is encountered, to ensure that all initializers are 
     * processed first.
     * 
     * Also, at the end of the class declaration, check that all static final
     * fields have been initialized at least once, and that for each constructor
     * all non-static final fields must have been initialized at least once,
     * taking into account the constructor calls.
     * 
     */
    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof ConstructorDecl) {
            // postpone the checking of the constructors until all the 
            // initializer blocks have been processed.
            currCBI.allConstructors.add((ConstructorDecl) n);
            return n;
        }

        if (n instanceof ClassBody) {
            // Now that we are at the end of the class declaration, and can
            // be sure that all of the initializer blocks have been processed,
            // we can now process the constructors.

            try {
                for (ConstructorDecl cd : currCBI.allConstructors) {
                    // rely on the fact that our dataflow does not change the
                    // AST, so we can discard the result of this call.

                    dataflow(cd);
                }

                // check that all static fields have been initialized exactly
                // once 
                checkStaticFinalFieldsInit((ClassBody) n);

                // check that at the end of each constructor all non-static
                // final fields are initialized.
                checkNonStaticFinalFieldsInit((ClassBody) n);

                // copy the locals used to the outer scope
                if (currCBI.outer != null) {
                    currCBI.outer.localsUsedInClassBodies.put((ClassBody) n,
                                                              currCBI.outerLocalsUsed);
                }
            }
            finally {
                // pop the stack
                currCBI = currCBI.outer;
            }
        }

        return super.leaveCall(old, n, v);
    }

    protected void setupClassBody(ClassType ct, ClassBody n)
            throws SemanticException {
        ClassBodyInfo newCDI = new ClassBodyInfo();
        newCDI.outer = currCBI;
        newCDI.currClass = ct;
        currCBI = newCDI;

        // set up currClassFinalFieldAssStatuses to contain mappings
        // for all the final fields of the class.            
        for (ClassMember cm : n.members()) {
            if (cm instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl) cm;
                if (fd.flags().isFinal()) {
                    AssignmentStatus assStatus;
                    if (fd.init() != null) {
                        // the field has an initializer
                        assStatus = AssignmentStatus.ASS;

                        // do dataflow over the initialization expression
                        // to pick up any uses of outer local variables.
                        if (currCBI.outer != null) dataflow(fd.init());
                    }
                    else {
                        // the field does not have an initializer
                        assStatus = AssignmentStatus.UNASS;
                    }
                    newCDI.currClassFinalFieldAssStatuses.put(fd.fieldInstance()
                                                                .orig(),
                                                              assStatus);
                }
            }
        }
    }

    /**
     * Check that each static final field is initialized exactly once.
     * 
     * @param cb The ClassBody of the class declaring the fields to check.
     * @throws SemanticException
     */
    protected void checkStaticFinalFieldsInit(ClassBody cb)
            throws SemanticException {
        // check that all static fields have been initialized exactly once.             
        for (Entry<FieldInstance, AssignmentStatus> e : currCBI.currClassFinalFieldAssStatuses.entrySet()) {
            FieldInstance fi = e.getKey();
            if (fi.flags().isStatic() && fi.flags().isFinal()) {
                AssignmentStatus defAss = e.getValue();
                if (!defAss.definitelyAssigned) {
                    throw new SemanticException("Final field \""
                                                        + fi.name()
                                                        + "\" might not have been initialized",
                                                cb.position());
                }
            }
        }
    }

    /**
     * Check that each non static final field has been initialized exactly once,
     * taking into account the fact that constructors may call other 
     * constructors. 
     * 
     * @param cb The ClassBody of the class declaring the fields to check.
     * @throws SemanticException
     */
    protected void checkNonStaticFinalFieldsInit(ClassBody cb)
            throws SemanticException {
        // for each non-static final field instance, check that all 
        // constructors initialize it exactly once, taking into account constructor calls.
        for (FieldInstance fi : currCBI.currClassFinalFieldAssStatuses.keySet()) {
            if (fi.flags().isFinal() && !fi.flags().isStatic()) {
                // the field is final and not static
                // it must be initialized exactly once.
                // navigate up through all of the the constructors
                // that this constructor calls.

                boolean fieldInitializedBeforeConstructors = false;
                AssignmentStatus ic =
                        currCBI.currClassFinalFieldAssStatuses.get(fi.orig());
                if (ic != null && ic.definitelyAssigned) {
                    fieldInitializedBeforeConstructors = true;
                }

                for (ConstructorDecl cd : currCBI.allConstructors) {
                    ConstructorInstance ciStart = cd.constructorInstance();
                    ConstructorInstance ci = ciStart;

                    boolean isInitialized = fieldInitializedBeforeConstructors;

                    while (ci != null) {
                        Set<FieldInstance> s =
                                currCBI.fieldsConstructorInitializes.get(ci.orig());
                        if (s != null && s.contains(fi)) {
                            if (isInitialized) {
                                throw new SemanticException("Final field \""
                                                                    + fi.name()
                                                                    + "\" might have already been initialized",
                                                            cd.position());
                            }
                            isInitialized = true;
                        }
                        ci = currCBI.constructorCalls.get(ci.orig());
                    }
                    if (!isInitialized) {
                        // check whether this constructor can terminate normally.
                        if (!currCBI.constructorsCannotTerminateNormally.contains(cd)) {
                            throw new SemanticException("Final field \""
                                                                + fi.name()
                                                                + "\" might not have been initialized",
                                                        ciStart.position());
                        }
                        else {
                            // Even though the final field may not be initialized, the constructor
                            // cannot terminate normally. For compatibility with javac, we will
                            // not protest.
                        }
                    }
                }
            }
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
        FlowGraph<FlowItem> g = new FlowGraph<>(root, forward);
        CFGBuilder<FlowItem> v = createCFGBuilder(ts, g);
        v.visitGraph();
        dataflow(g);
        post(g, root);
    }

    /**
     * The initial item to be given to the entry point of the dataflow contains
     * the init counts for the final fields.
     */
    @Override
    public FlowItem createInitialItem(FlowGraph<FlowItem> graph, Term node,
            boolean entry) {
        if (node == graph.root() && entry) {
            return createInitDFI();
        }
        return BOTTOM;
    }

    private FlowItem createInitDFI() {
        return new FlowItem(new HashMap<VarInstance, AssignmentStatus>(currCBI.currClassFinalFieldAssStatuses));
    }

    @Override
    protected CFGBuilder<FlowItem> createCFGBuilder(TypeSystem ts,
            FlowGraph<FlowItem> g) {
        CFGBuilder<FlowItem> v = new CFGBuilder<>(lang(), ts, g, this);
        // skip dead loops bodies and dead if branches. See JLS 2nd edition, Section 16.
//        v = v.skipDeadIfBranches(true);
//        v = v.skipDeadLoopBodies(true);
        return v;
    }

    /**
     * The confluence operator for {@code Initializer}s and 
     * {@code Constructor}s needs to be a 
     * little special, as we are only concerned with non-exceptional flows in 
     * these cases.
     * This method ensures that a slightly different confluence is performed
     * for these {@code Term}s, otherwise 
     * {@code confluence(List, Term)} is called instead. 
     */
    @Override
    protected FlowItem confluence(List<FlowItem> items, List<EdgeKey> itemKeys,
            Peer<FlowItem> peer, FlowGraph<FlowItem> graph) {
        Node node = peer.node();
        if (node instanceof Initializer || node instanceof ConstructorDecl) {
            List<FlowItem> filtered = filterItemsNonException(items, itemKeys);
            if (filtered.isEmpty()) {
                // record the fact that this dataflow item was not produced for a node
                // that can be reached by normal termination.
                return new FlowItem(new HashMap<VarInstance, AssignmentStatus>(currCBI.currClassFinalFieldAssStatuses),
                                    false);
            }
            else if (filtered.size() == 1) {
                return filtered.get(0);
            }
            else {
                return confluence(filtered, peer, graph);
            }
        }
        return confluence(items, peer, graph);
    }

    /**
     * The confluence operator is essentially the union of all of the
     * inItems. However, if two or more of the AssignmentStatus maps from
     * the inItems each have a DefiniteAssignments entry for the same
     * VarInstance, the conflict must be resolved, by using the
     * minimum of all mins and the maximum of all maxs. 
     */
    @Override
    public FlowItem confluence(List<FlowItem> inItems, Peer<FlowItem> peer,
            FlowGraph<FlowItem> graph) {
        // Resolve any conflicts pairwise.
        Map<VarInstance, AssignmentStatus> m = null;
        for (FlowItem itm : inItems) {
            if (itm == BOTTOM) continue;
            if (m == null) {
                m = new HashMap<>(itm.assignmentStatus);
            }
            else {
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

        return new FlowItem(m);
    }

    @Override
    protected Map<EdgeKey, FlowItem> flow(List<FlowItem> inItems,
            List<EdgeKey> inItemKeys, FlowGraph<FlowItem> graph,
            Peer<FlowItem> peer) {
        return this.flowToBooleanFlow(inItems, inItemKeys, graph, peer);
    }

    /**
     * Perform the appropriate flow operations for the Terms. This method
     * delegates to other appropriate methods in this class, for modularity.
     * 
     * To summarize:
     * - Formals: declaration of a Formal param, just insert a new 
     *              DefiniteAssignment for the LocalInstance.
     * - LocalDecl: a declaration of a local variable, just insert a new 
     *              DefiniteAssignment for the LocalInstance as appropriate
     *              based on whether the declaration has an initializer or not.
     * - Assign: if the LHS of the assign is a local var or a field that we
     *              are interested in, then increment the min and max counts
     *              for that local var or field.   
     */
    @Override
    public Map<EdgeKey, FlowItem> flow(FlowItem trueItem, FlowItem falseItem,
            FlowItem otherItem, FlowGraph<FlowItem> graph, Peer<FlowItem> peer) {
        FlowItem inItem =
                safeConfluence(trueItem,
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
                    Map<VarInstance, AssignmentStatus> newAssStatus =
                            new HashMap<>(inItem.assignmentStatus);

                    newAssStatus.remove(ld.localInstance());
                    inItem = new FlowItem(newAssStatus);
                }
            }
            return itemToMap(inItem, peer.succEdgeKeys());
        }

        if (inItem == BOTTOM) {
            return itemToMap(BOTTOM, peer.succEdgeKeys());
        }

        FlowItem inDFItem = inItem;
        Map<EdgeKey, FlowItem> ret = null;
        if (n instanceof Formal) {
            // formal argument declaration.
            ret = flowFormal(inDFItem, graph, (Formal) n, peer.succEdgeKeys());
        }
        else if (n instanceof LocalDecl) {
            // local variable declaration.
            ret =
                    flowLocalDecl(inDFItem,
                                  graph,
                                  (LocalDecl) n,
                                  peer.succEdgeKeys());
        }
        else if (n instanceof LocalAssign) {
            // assignment to a local variable
            ret =
                    flowLocalAssign(inDFItem,
                                    graph,
                                    (LocalAssign) n,
                                    peer.succEdgeKeys());
        }
        else if (n instanceof FieldAssign) {
            // assignment to a field
            ret =
                    flowFieldAssign(inDFItem,
                                    graph,
                                    (FieldAssign) n,
                                    peer.succEdgeKeys());
        }
        else if (n instanceof ConstructorCall) {
            // call to another constructor.
            ret =
                    flowConstructorCall(inDFItem,
                                        graph,
                                        (ConstructorCall) n,
                                        peer.succEdgeKeys());
        }
        else if (n instanceof Expr
                && ((Expr) n).type().isBoolean()
                && (n instanceof Binary || n instanceof Conditional || n instanceof Unary)) {
            if (trueItem == null) trueItem = inDFItem;
            if (falseItem == null) falseItem = inDFItem;
            ret =
                    flowBooleanConditions(trueItem,
                                          falseItem,
                                          inDFItem,
                                          graph,
                                          peer);
        }
        else {
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
                    ret =
                            remap(ret,
                                  FlowGraph.EDGE_KEY_FALSE,
                                  AssignmentStatus.ASS_UNASS);
                }
                else {
                    // the true branch is dead
                    ret =
                            remap(ret,
                                  FlowGraph.EDGE_KEY_TRUE,
                                  AssignmentStatus.ASS_UNASS);
                }
            }
        }
        return ret;
    }

    private static Map<EdgeKey, FlowItem> remap(Map<EdgeKey, FlowItem> m,
            EdgeKey ek, AssignmentStatus assStatus) {
        FlowItem fi = m.get(ek);
        if (fi == null) {
            return m;
        }

        Map<VarInstance, AssignmentStatus> assignmentStatus = new HashMap<>();
        for (VarInstance vi : fi.assignmentStatus.keySet()) {
            assignmentStatus.put(vi, assStatus);
        }

        FlowItem newFI = new FlowItem(assignmentStatus, fi.normalTermination);
        Map<EdgeKey, FlowItem> newM = new HashMap<>(m);
        newM.put(ek, newFI);
        return newM;
    }

    /**
     * Perform the appropriate flow operations for declaration of a formal 
     * parameter
     */
    protected Map<EdgeKey, FlowItem> flowFormal(FlowItem inItem,
            FlowGraph<FlowItem> graph, Formal f, Set<EdgeKey> succEdgeKeys) {
        Map<VarInstance, AssignmentStatus> m =
                new HashMap<>(inItem.assignmentStatus);
        // a formal argument is always defined.            
        m.put(f.localInstance().orig(), AssignmentStatus.ASS);

        // record the fact that we have seen the formal declaration
        currCBI.localDeclarations.add(f.localInstance().orig());

        return DataFlow.<FlowItem> itemToMap(new FlowItem(m), succEdgeKeys);
    }

    /**
     * Perform the appropriate flow operations for declaration of a local 
     * variable
     */
    protected Map<EdgeKey, FlowItem> flowLocalDecl(FlowItem inItem,
            FlowGraph<FlowItem> graph, LocalDecl ld, Set<EdgeKey> succEdgeKeys) {
        Map<VarInstance, AssignmentStatus> m =
                new HashMap<>(inItem.assignmentStatus);
        AssignmentStatus assStatus = m.get(ld.localInstance().orig());
        if (ld.init() != null) {
            // declaration of local var with initialization.
            assStatus = AssignmentStatus.ASS;
        }
        else {
            // declaration of local var with no initialization.
            assStatus = AssignmentStatus.UNASS;
        }

        m.put(ld.localInstance().orig(), assStatus);
//        }
//        else {
        // the initCount is not null. We now have a problem. Why is the
        // initCount not null? Has this variable been assigned in its own
        // initialization, or is this a declaration inside a loop body?
        // XXX@@@ THIS IS A BUG THAT NEEDS TO BE FIXED.
        // Currently, the declaration "final int i = (i=5);" will 
        // not be rejected, as we cannot distinguish between that and
        // "while (true) {final int i = 4;}"
//        }

        // record the fact that we have seen a local declaration
        currCBI.localDeclarations.add(ld.localInstance());

        return DataFlow.<FlowItem> itemToMap(new FlowItem(m), succEdgeKeys);
    }

    /**
     * Perform the appropriate flow operations for assignment to a local 
     * variable
     */
    protected Map<EdgeKey, FlowItem> flowLocalAssign(FlowItem inItem,
            FlowGraph<FlowItem> graph, LocalAssign a, Set<EdgeKey> succEdgeKeys) {
        Local l = a.left();
        Map<VarInstance, AssignmentStatus> m =
                new HashMap<>(inItem.assignmentStatus);
        AssignmentStatus initCount = m.get(l.localInstance().orig());

        initCount = AssignmentStatus.ASS;

        m.put(l.localInstance().orig(), initCount);
        return DataFlow.<FlowItem> itemToMap(new FlowItem(m), succEdgeKeys);
    }

    /**
     * Perform the appropriate flow operations for assignment to a field
     */
    protected Map<EdgeKey, FlowItem> flowFieldAssign(FlowItem inItem,
            FlowGraph<FlowItem> graph, FieldAssign a, Set<EdgeKey> succEdgeKeys) {
        Field f = a.left();
        FieldInstance fi = f.fieldInstance();

        if (fi.flags().isFinal() && isFieldsTargetAppropriate(f)) {
            // this field is final and the target for this field is 
            // appropriate for what we are interested in.
            Map<VarInstance, AssignmentStatus> m =
                    new HashMap<>(inItem.assignmentStatus);
            // m.get(fi.orig()) may be null if the field is defined in an
            // outer class. If so, ignore this assignment.
            if (m.get(fi.orig()) != null) {
                m.put(fi.orig(), AssignmentStatus.ASS);
                return DataFlow.<FlowItem> itemToMap(new FlowItem(m),
                                                     succEdgeKeys);
            }
        }
        return null;
    }

    /**
     * Perform the appropriate flow operations for a constructor call
     */
    protected Map<EdgeKey, FlowItem> flowConstructorCall(FlowItem inItem,
            FlowGraph<FlowItem> graph, ConstructorCall cc,
            Set<EdgeKey> succEdgeKeys) {
        if (ConstructorCall.THIS.equals(cc.kind())) {
            // currCodeDecl must be a ConstructorDecl, as that
            // is the only place constructor calls are allowed
            // record the fact that the current constructor calls the other
            // constructor
            currCBI.constructorCalls.put(((ConstructorDecl) currCBI.currCodeDecl).constructorInstance()
                                                                                 .orig(),
                                         cc.constructorInstance().orig());
        }
        return null;
    }

    /**
     * Allow subclasses to override if necessary.
     */
    protected Map<EdgeKey, FlowItem> flowOther(FlowItem inItem,
            FlowGraph<FlowItem> graph, Node n, Set<EdgeKey> succEdgeKeys) {
        return null;
    }

    /**
     * Determine if we are interested in this field on the basis of the
     * target of the field. To wit, if the field
     * is static, then the target of the field must be the current class; if
     * the field is not static then the target must be "this".
     */
    protected boolean isFieldsTargetAppropriate(Field f) {
        ClassType containingClass = currCBI.currClass;

        if (f.fieldInstance().flags().isStatic()) {
            return containingClass.equals(f.fieldInstance().orig().container());
        }
        else {
            if (f.target() instanceof Special) {
                Special s = (Special) f.target();
                if (Special.THIS.equals(s.kind())) {
                    return s.qualifier() == null
                            || containingClass.equals(s.qualifier().type());
                }
            }
            return false;
        }
    }

    /**
     * Check that the conditions of initialization are not broken.
     * 
     * To summarize the conditions:
     * - Local variables must be initialized before use, (i.e. min count > 0)
     * - Final local variables (including Formals) cannot be assigned to more 
     *               than once (i.e. max count <= 1)
     * - Final non-static fields whose target is this cannot be assigned to
     *               more than once 
     * - Final static fields whose target is the current class cannot be 
     *               assigned to more than once
     *               
     * 
     * This method is also responsible for maintaining state between the 
     * dataflows over Initializers, by copying back the appropriate 
     * DefiniteAssignments to the map currClassFinalFieldInitCounts.
     */
    @Override
    public void check(FlowGraph<FlowItem> graph, Term n, boolean entry,
            FlowItem inItem, Map<EdgeKey, FlowItem> outItems)
            throws SemanticException {
        FlowItem dfIn = inItem;
        if (dfIn == null) {
            // There is no input data flow item. This can happen if we are 
            // checking an unreachable term, and so no Items have flowed 
            // through the term. For example, in the code fragment:
            //     a: do { break a; } while (++i < 10);
            // the expression "++i < 10" is unreachable, but the as there is
            // no unreachable statement, the Java Language Spec permits it.

            // Set inItem to a default Item
            dfIn = createInitDFI();
        }

        FlowItem dfOut = null;
        if (!entry && outItems != null && !outItems.isEmpty()) {
            // due to the flow equations, all DataFlowItems in the outItems map
            // are the same, so just take the first one.
            dfOut = outItems.values().iterator().next();
            if (n instanceof Field) {
                checkField(graph, (Field) n, dfIn);
            }
            else if (n instanceof Local) {
                checkLocal(graph, (Local) n, dfIn);
            }
            else if (n instanceof LocalAssign) {
                checkLocalAssign(graph,
                                 ((LocalAssign) n).left().localInstance(),
                                 n.position(),
                                 dfIn);
            }
            else if (n instanceof LocalDecl) {
                checkLocalAssign(graph,
                                 ((LocalDecl) n).localInstance(),
                                 n.position(),
                                 dfIn);
            }
            else if (n instanceof FieldAssign) {
                checkFieldAssign(graph, (FieldAssign) n, dfIn);
            }
            else if (n instanceof ClassBody) {
                checkClassBody(graph, (ClassBody) n, dfIn, dfOut);
            }
            else {
                checkOther(graph, n, dfIn);
            }
        }
        else {
            // this local assign node has not had data flow performed over it.
            // probably a node in a finally block. Just ignore it.
        }

        if (n == graph.root() && !entry) {
            if (currCBI.currCodeDecl instanceof FieldDecl) {
                finishFieldDecl(graph,
                                (FieldDecl) currCBI.currCodeDecl,
                                dfIn,
                                dfOut);
            }
            if (currCBI.currCodeDecl instanceof ConstructorDecl) {
                finishConstructorDecl(graph,
                                      (ConstructorDecl) currCBI.currCodeDecl,
                                      dfIn,
                                      dfOut);
            }
            if (currCBI.currCodeDecl instanceof Initializer) {
                finishInitializer(graph,
                                  (Initializer) currCBI.currCodeDecl,
                                  dfIn,
                                  dfOut);
            }
        }
    }

    /**
     * Perform necessary actions upon seeing the FieldDecl 
     * {@code fd}.
     */
    protected void finishFieldDecl(FlowGraph<FlowItem> graph, FieldDecl fd,
            FlowItem dfIn, FlowItem dfOut) {
        // We are finishing the checking of a field declaration.
        // We need to copy back the init counts of any fields back into
        // currClassFinalFieldInitCounts, so that the counts are 
        // correct for the next field declaration, initializer, or constructor.
        for (Entry<VarInstance, AssignmentStatus> e : dfOut.assignmentStatus.entrySet()) {
            if (e.getKey() instanceof FieldInstance) {
                FieldInstance fi = (FieldInstance) e.getKey();
                if (fi.flags().isFinal()) {
                    // we don't need to join the init counts, as all
                    // dataflows will go through all of the 
                    // initializers
                    currCBI.currClassFinalFieldAssStatuses.put(fi.orig(),
                                                               e.getValue());
                }
            }
        }
    }

    /**
     * Perform necessary actions upon seeing the ConstructorDecl 
     * {@code cd}.
     */
    protected void finishConstructorDecl(FlowGraph<FlowItem> graph,
            ConstructorDecl cd, FlowItem dfIn, FlowItem dfOut) {
        ConstructorInstance ci = cd.constructorInstance();

        // we need to set currCBI.fieldsConstructorInitializes correctly.
        // It is meant to contain the non-static final fields that the
        // constructor ci initializes.
        //
        // Note that dfOut.initStatus contains only the DefiniteAssignments
        // for _normal_ termination of the constructor (see the
        // method confluence). This means that if dfOut says the min
        // count of the initialization for a final non-static field
        // is one, and that is different from what is recorded in
        // currCBI.currClassFinalFieldInitCounts (which is the counts
        // of the initializations performed by initializers), then
        // the constructor does indeed initialize the field.

        Set<FieldInstance> s = new HashSet<>();

        // go through every final non-static field in dfOut.initStatus
        for (Entry<VarInstance, AssignmentStatus> e : dfOut.assignmentStatus.entrySet()) {
            if (e.getKey() instanceof FieldInstance
                    && ((FieldInstance) e.getKey()).flags().isFinal()
                    && !((FieldInstance) e.getKey()).flags().isStatic()) {
                // we have a final non-static field                           
                FieldInstance fi = (FieldInstance) e.getKey();
                AssignmentStatus initCount = e.getValue();
                AssignmentStatus origInitCount =
                        currCBI.currClassFinalFieldAssStatuses.get(fi);
                if (initCount.definitelyAssigned
                        && !origInitCount.definitelyAssigned) {
                    // the constructor initialized this field
                    s.add(fi);
                }
            }
        }
        if (!s.isEmpty()) {
            currCBI.fieldsConstructorInitializes.put(ci.orig(), s);
        }
        if (!dfIn.normalTermination) {
            // this ci cannot terminate normally. Record this fact.
            currCBI.constructorsCannotTerminateNormally.add(cd);
        }
    }

    /**
     * Perform necessary actions upon seeing the Initializer 
     * {@code initializer}.
     */
    protected void finishInitializer(FlowGraph<FlowItem> graph,
            Initializer initializer, FlowItem dfIn, FlowItem dfOut) {
        // We are finishing the checking of an initializer.
        // We need to copy back the init counts of any fields back into
        // currClassFinalFieldInitCounts, so that the counts are 
        // correct for the next field declaration, initializer, or constructor.
        for (Entry<VarInstance, AssignmentStatus> e : dfOut.assignmentStatus.entrySet()) {
            if (e.getKey() instanceof FieldInstance) {
                FieldInstance fi = (FieldInstance) e.getKey();
                if (fi.flags().isFinal()) {
                    // we don't need to join the init counts, as all
                    // dataflows will go through all of the 
                    // initializers
                    currCBI.currClassFinalFieldAssStatuses.put(fi.orig(),
                                                               e.getValue());
                }
            }
        }
    }

    /**
     * Check that the field {@code f} is used correctly.
     * See JLS 2nd Ed. | 16: Every blank final field must have a definitely
     * assigned value when any access of its value occurs.
     */
    protected void checkField(FlowGraph<FlowItem> graph, Field f, FlowItem dfIn)
            throws SemanticException {
        FieldInstance fi = f.fieldInstance();
        // Use of blank final field only needs to be checked when the use
        // occurs inside the same class as the field's container.
        if (fi.flags().isFinal()
                && ts.typeEquals(currCBI.currClass, fi.container())) {
            if ((currCBI.currCodeDecl instanceof FieldDecl
                    || currCBI.currCodeDecl instanceof ConstructorDecl || currCBI.currCodeDecl instanceof Initializer)
                    && isFieldsTargetAppropriate(f)) {
                AssignmentStatus initCount =
                        dfIn.assignmentStatus.get(fi.orig());
                if (initCount == null || !initCount.definitelyAssigned) {
                    throw new SemanticException("Final field \""
                                                        + f.name()
                                                        + "\" might not have been initialized",
                                                f.position());
                }
            }
        }
    }

    /**
     * Check that the local variable {@code l} is used correctly.
     */
    protected void checkLocal(FlowGraph<FlowItem> graph, Local l, FlowItem dfIn)
            throws SemanticException {
        if (!currCBI.localDeclarations.contains(l.localInstance().orig())) {
            // it's a local variable that has not been declared within
            // this scope. The only way this can arise is from an
            // inner class that is not a member of a class (typically
            // a local class, or an anonymous class declared in a method,
            // constructor or initializer).
            // We need to check that it is a final local, and also
            // keep track of it, to ensure that it has been definitely
            // assigned at this point.
            currCBI.outerLocalsUsed.add(l.localInstance().orig());
        }
        else {
            AssignmentStatus initCount =
                    dfIn.assignmentStatus.get(l.localInstance().orig());
            if (initCount == null || !initCount.definitelyAssigned) {
                // the local variable may not have been initialized. 
                // However, we only want to complain if the local is reachable
                if (l.reachable()) {
                    throw new SemanticException("Local variable \"" + l.name()
                            + "\" may not have been initialized", l.position());
                }
            }
        }
    }

    protected void checkLocalInstanceInit(LocalInstance li, FlowItem dfIn,
            Position pos) throws SemanticException {
        AssignmentStatus initCount = dfIn.assignmentStatus.get(li.orig());
        if (initCount != null && !initCount.definitelyAssigned) {
            // the local variable may not have been initialized. 
            throw new SemanticException("Local variable \"" + li.name()
                    + "\" may not have been initialized", pos);
        }
    }

    /**
     * Check that the assignment to a local variable is correct.
     */
    protected void checkLocalAssign(FlowGraph<FlowItem> graph,
            LocalInstance li, Position pos, FlowItem dfIn)
            throws SemanticException {
        if (!currCBI.localDeclarations.contains(li.orig())) {
            throw new SemanticException("Final local variable \"" + li.name()
                    + "\" cannot be assigned to in an inner class.", pos);
        }

        AssignmentStatus initCount = dfIn.assignmentStatus.get(li.orig());

        if (li.flags().isFinal() && initCount != null
                && !initCount.definitelyUnassigned) {
            throw new SemanticException("Final variable \"" + li.name()
                    + "\" might already have been initialized", pos);
        }
    }

    /**
     * Check that the assignment to a field is correct.
     */
    protected void checkFieldAssign(FlowGraph<FlowItem> graph, FieldAssign a,
            FlowItem dfIn) throws SemanticException {

        Field f = a.left();
        FieldInstance fi = f.fieldInstance();
        if (fi.flags().isFinal()) {
            if ((currCBI.currCodeDecl instanceof FieldDecl
                    || currCBI.currCodeDecl instanceof ConstructorDecl || currCBI.currCodeDecl instanceof Initializer)
                    && isFieldsTargetAppropriate(f)) {
                // we are in a constructor or initializer block and 
                // if the field is static then the target is the class
                // at hand, and if it is not static then the
                // target of the field is this. 
                // So a final field in this situation can be 
                // assigned to at most once.                    
                AssignmentStatus initCount =
                        dfIn.assignmentStatus.get(fi.orig());
                if (initCount == null) {
                    // This should not happen.
                    throw new InternalCompilerError("Dataflow information not found for field \""
                                                            + fi.name() + "\".",
                                                    a.position());
                }
                if (!initCount.definitelyUnassigned) {
                    throw new SemanticException("Final field \""
                                                        + fi.name()
                                                        + "\" might already have been initialized",
                                                a.position());
                }
            }
            else {
                // not in a constructor or initializer, or the target is
                // not appropriate. So we cannot assign 
                // to a final field at all.
                throw new SemanticException("Cannot assign a value "
                        + "to final field \"" + fi.name() + "\" of \""
                        + fi.orig().container() + "\".", a.position());
            }
        }
    }

    /**
     * Check that the set of {@code LocalInstance}s 
     * {@code localsUsed}, which is the set of locals used in the inner 
     * class declared by {@code cb}
     * are initialized before the class declaration.
     * @throws SemanticException
     */
    protected void checkClassBody(FlowGraph<FlowItem> graph, ClassBody cb,
            FlowItem dfIn, FlowItem dfOut) throws SemanticException {
        // we need to check that the locals used inside this class body
        // have all been defined at this point.
        Set<LocalInstance> localsUsed = currCBI.localsUsedInClassBodies.get(cb);

        if (localsUsed != null) {
            checkLocalsUsedByInnerClass(graph, cb, localsUsed, dfIn, dfOut);
        }
    }

    /**
     * Check that the set of {@code LocalInstance}s 
     * {@code localsUsed}, which is the set of locals used in the inner 
     * class declared by {@code cb}
     * are initialized before the class declaration.
     */
    protected void checkLocalsUsedByInnerClass(FlowGraph<FlowItem> graph,
            ClassBody cb, Set<LocalInstance> localsUsed, FlowItem dfIn,
            FlowItem dfOut) throws SemanticException {
        for (LocalInstance li : localsUsed) {
            AssignmentStatus initCount = dfOut.assignmentStatus.get(li.orig());
            if (!currCBI.localDeclarations.contains(li.orig())) {
                // the local wasn't defined in this scope.
                currCBI.outerLocalsUsed.add(li.orig());
            }
            else if (initCount == null || !initCount.definitelyAssigned) {
                // initCount will in general not be null, as the local variable
                // li is declared in the current class; however, if the inner
                // class is declared in the initializer of the local variable
                // declaration, then initCount could in fact be null, as we 
                // leave the inner class before we have performed flowLocalDecl
                // for the local variable declaration.

                throw new SemanticException("Local variable \"" + li.name()
                        + "\" must be initialized before the class "
                        + "declaration.", cb.position());
            }
        }
    }

    /**
     * Allow subclasses to override the checking of other nodes, if needed.
     * @throws SemanticException 
     */
    protected void checkOther(FlowGraph<FlowItem> graph, Node n, FlowItem dfIn)
            throws SemanticException {
    }
}
