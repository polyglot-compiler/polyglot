package polyglot.visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.CodeDecl;
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
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.Special;
import polyglot.ast.Term;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.VarInstance;

/**
 * Visitor which checks that all local variables must be defined before use, 
 * and that final variables and fields are initialized correctly.
 * 
 * The checking of the rules is implemented in the methods leaveCall(Node)
 * and check(FlowGraph, Term, Item, Item).
 * 
 */
public class InitChecker extends DataFlow
{
    public InitChecker(Job job, TypeSystem ts, NodeFactory nf) {
	super(job, ts, nf, true /* forward analysis */);
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
         * The info for the outer ClassBody. The <code>ClassBodyInfo</code>s
         * form a stack. 
         */
        ClassBodyInfo outer = null;
        
        /** The current CodeDecl being processed by the dataflow equations */
        CodeDecl currCodeDecl = null;
        /** 
         * A Map of all the final fields in the class currently being processed
         * to MinMaxInitCounts. This Map is used as the basis for the Maps returned
         * in createInitialItem(). 
         * */
        Map currClassFinalFieldInitCounts = new HashMap();
        /**
         * List of all the constructors. These will be checked once all the
         * initializer blocks have been processed.
         */
        List allConstructors = new ArrayList();

        /**
         * Map from ConstructorInstances to ConstructorInstances detailing
         * which constructors call which constructors.
         * This is used in checking the initialization of final fields.
         */
        Map constructorCalls = new HashMap();
        
        /**
         * Map from ConstructorInstances to Sets of FieldInstances, detailing
         * which final non-static fields each constructor initializes. 
         * This is used in checking the initialization of final fields.
         */
        Map fieldsConstructorInitializes = new HashMap();
        
        /**
         * Set of LocalInstances from the outer class body that were used
         * during the declaration of this class. We need to track this
         * in order to correctly populate <code>localsUsedInClassBodies</code>
         */
        Set outerLocalsUsed = new HashSet();
        
        /**
         * Map from <code>ClassBody</code>s to <code>Set</code>s of 
         * <code>LocalInstance</code>s. If localsUsedInClassBodies(C) = S, then
         * the class body C is an inner class declared in the current code 
         * declaration, and S is the set of LocalInstances that are defined
         * in the current code declaration, but are used in the declaration
         * of the class C. We need this information in order to ensure that
         * these local variables are definitely assigned before the class
         * declaration of C. 
         */
        Map localsUsedInClassBodies = new HashMap();
    }


    /**
     * Class representing the initialization counts of variables. The
     * different values of the counts that we are interested in are ZERO,
     * ONE and MANY.
     */
    protected static class InitCount {
        static InitCount ZERO = new InitCount(0); 
        static InitCount ONE = new InitCount(1); 
        static InitCount MANY = new InitCount(2); 
        protected int count;
        protected InitCount(int i) {
            count = i;
        }
        
        public boolean equals(Object o) {
            if (o instanceof InitCount) {
                return this.count == ((InitCount)o).count;
            }
            return false;
        }
        
        public String toString() {
            if (count == 0) {
                return "0";
            }
            else if (count == 1) {
                return "1";
            }
            else if (count == 2) {
                return "many";
            }
            throw new RuntimeException("Unexpected value for count");            
        }
        
        public InitCount increment() {
            if (count == 0) {
                return ONE;
            }
            return MANY;
        }
        public static InitCount min(InitCount a, InitCount b) {
            if (ZERO.equals(a) || ZERO.equals(b)) {
                return ZERO;
            }
            if (ONE.equals(a) || ONE.equals(b)) {
                return ONE;
            }
            return MANY;
        }
        public static InitCount max(InitCount a, InitCount b) {
            if (MANY.equals(a) || MANY.equals(b)) {
                return MANY;
            }
            if (ONE.equals(a) || ONE.equals(b)) {
                return ONE;
            }
            return ZERO;
        }
        
    }
    
    /**
     * Class to record counts of the minimum and maximum number of times
     * a variable or field has been initialized or assigned to.
     */
    protected static class MinMaxInitCount {
        protected InitCount min, max;
        MinMaxInitCount(InitCount min, InitCount max) {
            MinMaxInitCount.this.min = min;
            MinMaxInitCount.this.max = max;
        }
        InitCount getMin() { return min; }
        InitCount getMax() { return max; }
        public String toString() {
            return "[ min: " + min + "; max: " + max + " ]";
        }
        public boolean equals(Object o) {
            if (o instanceof MinMaxInitCount) {
                return this.min.equals(((MinMaxInitCount)o).min) &&
                       this.max.equals(((MinMaxInitCount)o).max);
            }
            return false;
        }
        static MinMaxInitCount join(MinMaxInitCount initCount1, MinMaxInitCount initCount2) {
            if (initCount1 == null) {
                return initCount2;
            }
            if (initCount2 == null) {
                return initCount1;
            }
            MinMaxInitCount t = new MinMaxInitCount(
                                  InitCount.min(initCount1.getMin(), initCount2.getMin()),
                                  InitCount.max(initCount1.getMax(), initCount2.getMax()));
            return t;

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
    static class DataFlowItem extends Item {
        Map initStatus; // map of VarInstances to MinMaxInitCount

        DataFlowItem(Map m) {
            this.initStatus = Collections.unmodifiableMap(m);
        }

        public String toString() {
            return initStatus.toString();
        }

        public boolean equals(Object o) {
            if (o instanceof DataFlowItem) {
                return this.initStatus.equals(((DataFlowItem)o).initStatus);
            }
            return false;
        }
        
        public int hashCode() {
            return (initStatus.hashCode());
        }

    }

    /**
     * Initialise the FlowGraph to be used in the dataflow analysis.
     * @return null if no dataflow analysis should be performed for this
     *         code declaration; otherwise, an apropriately initialized
     *         FlowGraph.
     */
    protected FlowGraph initGraph(CodeDecl code, Term root) {
        currCBI.currCodeDecl = code;
        return new FlowGraph(root, forward);
    }

    /**
     * Overridden superclass method.
     * 
     * Set up the state that must be tracked during a Class Declaration.
     */
    protected NodeVisitor enterCall(Node n) throws SemanticException {
      if (n instanceof ClassBody) {            
            // we are starting to process a class declaration, but have yet
            // to do any of the dataflow analysis.
            
            // set up the new ClassBodyInfo, and make sure that it forms
            // a stack.
            setupClassBody((ClassBody)n);
        }
        
        // Postpone the checking of constructors until leaving the class 
        // declaration, to ensure that all initializers are 
        // processed first.
        if (n instanceof ConstructorDecl) {
            // postpone the checking of the constructors until all the 
            // initializer blocks have been processed.
            currCBI.allConstructors.add(n);
            return this;
        }
        
        // otherwise, let the superclass process the node appropriately...
        return super.enterCall(n);
    }

    protected Node leaveCall(Node n) throws SemanticException {
        // At the end of the class declaration, check that all static final
        // fields have been initialized at least once, and that for each constructor
        // all non-static final fields must have been initialized at least once,
        // taking into account the constructor calls.

        if (n instanceof ClassBody) {
            // Now that we are at the end of the class declaration, and can
            // be sure that all of the initializer blocks have been processed,
            // we can now process the constructors.
            
            for (Iterator iter = currCBI.allConstructors.iterator(); 
                    iter.hasNext(); ) {
                ConstructorDecl cd = (ConstructorDecl)iter.next();
                
                // perform the dataflow on the constructor declaration
                dataflow(cd);                
            }
            
            // check that all static fields have been initialized exactly once 
            checkStaticFinalFieldsInit((ClassBody)n);
            
            // check that at the end of each constructor!@!
            checkNonStaticFinalFieldsInit((ClassBody)n);
            
            // copy the locals used to the outer scope
            if (currCBI.outer != null) {
                currCBI.outer.localsUsedInClassBodies.put(n, 
                                                    currCBI.outerLocalsUsed);
            }
            
            // pop the stack
            currCBI = currCBI.outer;
        }
        
        return super.leaveCall(n);     
    }
    
    protected void setupClassBody(ClassBody n) throws SemanticException {
        ClassBodyInfo newCDI = new ClassBodyInfo();
        newCDI.outer = currCBI;  
        currCBI = newCDI;
            

        // set up currClassFinalFieldInitCounts to contain mappings
        // for all the final fields of the class.            
        Iterator classMembers = n.members().iterator();            
        while (classMembers.hasNext()) {
            ClassMember cm = (ClassMember)classMembers.next();
            if (cm instanceof FieldDecl) {
                FieldDecl fd = (FieldDecl)cm;
                if (fd.flags().isFinal()) {
                    MinMaxInitCount initCount;
                    if (fd.init() != null) {
                        // the field has an initializer
                        initCount = new MinMaxInitCount(InitCount.ONE,InitCount.ONE);
                            
                        // do dataflow over the initialization expression
                        // to pick up any uses of outer local variables.
                        if (currCBI.outer != null)
                            dataflow(fd.init());                        
                    }
                    else {
                        // the field does not have an initializer
                        initCount = new MinMaxInitCount(InitCount.ZERO,InitCount.ZERO);
                    }
                    newCDI.currClassFinalFieldInitCounts.put(fd.fieldInstance(),
                                                         initCount);
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
    protected void checkStaticFinalFieldsInit(ClassBody cb) throws SemanticException {
        // check that all static fields have been initialized exactly once.             
        for (Iterator iter = currCBI.currClassFinalFieldInitCounts.entrySet().iterator(); 
                iter.hasNext(); ) {
            Map.Entry e = (Map.Entry)iter.next();
            if (e.getKey() instanceof FieldInstance) {
                FieldInstance fi = (FieldInstance)e.getKey();
                if (fi.flags().isStatic() && fi.flags().isFinal()) {
                    MinMaxInitCount initCount = (MinMaxInitCount)e.getValue();
                    if (InitCount.ZERO.equals(initCount.getMin())) {
                        throw new SemanticException("field \"" + fi.name() +
                            "\" might not have been initialized",
                            cb.position());                                
                    }
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
    protected void checkNonStaticFinalFieldsInit(ClassBody cb) throws SemanticException {
        // for each non-static final field instance, check that all 
        // constructors intialize it exactly once, taking into account constructor calls.
        for (Iterator iter = currCBI.currClassFinalFieldInitCounts.keySet().iterator(); 
                iter.hasNext(); ) {
            FieldInstance fi = (FieldInstance)iter.next();
            if (fi.flags().isFinal() && !fi.flags().isStatic()) {
                // the field is final and not static
                // it must be initialized exactly once.
                // navigate up through all of the the constructors
                // that this constructor calls.
                    
                boolean fieldInitializedBeforeConstructors = false;
                MinMaxInitCount ic = (MinMaxInitCount)
                    currCBI.currClassFinalFieldInitCounts.get(fi);
                if (ic != null && !InitCount.ZERO.equals(ic.getMin())) {
                    fieldInitializedBeforeConstructors = true;
                }
                            
                for (Iterator iter2 = currCBI.allConstructors.iterator(); 
                        iter2.hasNext(); ) {
                    ConstructorDecl cd = (ConstructorDecl)iter2.next();
                    ConstructorInstance ciStart = cd.constructorInstance();
                    ConstructorInstance ci = ciStart;
                        
                    boolean isInitialized = fieldInitializedBeforeConstructors;
                        
                    while (ci != null) {
                        Set s = (Set)currCBI.fieldsConstructorInitializes.get(ci);
                        if (s != null && s.contains(fi)) {
                            if (isInitialized) {
                                throw new SemanticException("field \"" + fi.name() +
                                        "\" might have already been initialized",
                                        cd.position());                                                                        
                            }
                            isInitialized = true;
                        }                                
                        ci = (ConstructorInstance)currCBI.constructorCalls.get(ci);
                    }
                    if (!isInitialized) {
                        throw new SemanticException("field \"" + fi.name() +
                                "\" might not have been initialized",
                                ciStart.position());                                
                                
                    }                            
                }
            }
        }        
    }
    
    /**
     * Construct a flow graph for the <code>Expr</code> provided, and call 
     * <code>dataflow(FlowGraph)</code>. Is also responsible for calling 
     * <code>post(FlowGraph, Term)</code> after
     * <code>dataflow(FlowGraph)</code> has been called.
     * There is no need to push a CFG onto the stack, as the expression
     * cannot contain CodeDecls. 
     */
    protected void dataflow(Expr root) throws SemanticException {
        // Build the control flow graph.
        FlowGraph g = new FlowGraph(root, forward);
        CFGBuilder v = new CFGBuilder(ts, g, this);
        v.visitGraph();
        dataflow(g);
        post(g, root);        
    }
        
    /**
     * The initial item to be given to the entry point of the dataflow contains
     * the init counts for the final fields.
     */
    public Item createInitialItem(FlowGraph graph) {
        return new DataFlowItem(new HashMap(currCBI.currClassFinalFieldInitCounts));
    }
    
    /**
     * The confluence operator is essentially the union of all of the
     * inItems. However, if two or more of the initCount maps from
     * the inItems each have a MinMaxInitCounts entry for the same
     * VarInstance, the conflict must be resolved, by using the
     * minimum of all mins and the maximum of all maxs. 
     */
    public Item confluence(List inItems, Term node) {        
        // Resolve any conflicts pairwise.
        Iterator iter = inItems.iterator();
        Map m = new HashMap(((DataFlowItem)iter.next()).initStatus);
        while (iter.hasNext()) {
            Map n = ((DataFlowItem)iter.next()).initStatus;
            for (Iterator iter2 = n.entrySet().iterator(); iter2.hasNext(); ) {
                Map.Entry entry = (Map.Entry)iter2.next();
                VarInstance v = (VarInstance)entry.getKey();
                MinMaxInitCount initCount1 = (MinMaxInitCount)m.get(v);
                MinMaxInitCount initCount2 = (MinMaxInitCount)entry.getValue();
                m.put(v, MinMaxInitCount.join(initCount1, initCount2));                                        
            }
        }
        
        return new DataFlowItem(m);
    }
    

    /**
     * Perform the appropriate flow operations for the Terms. This method
     * delegates to other appropriate methods in this class, for modularity.
     * 
     * To summarize:
     * - Formals: declaration of a Formal param, just insert a new 
     *              MinMaxInitCount for the LocalInstance.
     * - LocalDecl: a declaration of a local variable, just insert a new 
     *              MinMaxInitCount for the LocalInstance as appropriate
     *              based on whether the declaration has an initializer or not.
     * - Assign: if the LHS of the assign is a local var or a field that we
     *              are interested in, then increment the min and max counts
     *              for that local var or field.   
     */
    public Map flow(Item inItem, FlowGraph graph, Term n, Set succEdgeKeys) {
        DataFlowItem inDFItem = ((DataFlowItem)inItem);

        Map ret = null;        
        if (n instanceof Formal) {            
            // formal argument declaration.
            ret = flowFormal(inDFItem, graph, (Formal)n, succEdgeKeys);
        }
        else if (n instanceof LocalDecl) {
            // local variable declaration.
            ret = flowLocalDecl(inDFItem, graph, (LocalDecl)n, succEdgeKeys);
        }
        else if (n instanceof LocalAssign) {
            // assignment to a local variable
            ret = flowLocalAssign(inDFItem, graph, (LocalAssign)n, succEdgeKeys);
        }
        else if (n instanceof FieldAssign) {
            // assignment to a field
            ret = flowFieldAssign(inDFItem, graph, (FieldAssign)n, succEdgeKeys);
        }
        else if (n instanceof ConstructorCall) {
            // call to another constructor.
            ret = flowConstructorCall(inDFItem, graph, (ConstructorCall)n, succEdgeKeys);
        }
        
        if (ret != null) {
            return ret;
        }
        return itemToMap(inItem, succEdgeKeys);
    }

    /**
     * Perform the appropriate flow operations for declaration of a formal 
     * parameter
     */
    protected Map flowFormal(DataFlowItem inItem, FlowGraph graph, Formal f, Set succEdgeKeys) {
        Map m = new HashMap(inItem.initStatus);
        // a formal argument is always defined.            
        m.put(f.localInstance(), new MinMaxInitCount(InitCount.ONE,InitCount.ONE));
            
        return itemToMap(new DataFlowItem(m), succEdgeKeys);
    }

    /**
     * Perform the appropriate flow operations for declaration of a local 
     * variable
     */
    protected Map flowLocalDecl(DataFlowItem inItem, 
                                FlowGraph graph, 
                                LocalDecl ld, 
                                Set succEdgeKeys) {
        Map m = new HashMap(inItem.initStatus);
        if (ld.init() == null) {
            // declaration of local var with no initialization
            m.put(ld.localInstance(), new MinMaxInitCount(InitCount.ZERO,InitCount.ZERO));
        }
        else {
            // declaration of local var with initialization.
            m.put(ld.localInstance(), new MinMaxInitCount(InitCount.ONE,InitCount.ONE));
        }
        return itemToMap(new DataFlowItem(m), succEdgeKeys);
    }
    
    /**
     * Perform the appropriate flow operations for assignment to a local 
     * variable
     */
    protected Map flowLocalAssign(DataFlowItem inItem, 
                                  FlowGraph graph, 
                                  LocalAssign a, 
                                  Set succEdgeKeys) {
          Local l = (Local) a.left();
          Map m = new HashMap(inItem.initStatus);
          MinMaxInitCount initCount = (MinMaxInitCount)m.get(l.localInstance());

          // initcount could be null if the local is defined in the outer
          // class.
          if (initCount != null ) {
              initCount = new MinMaxInitCount(initCount.getMin().increment(),
                                              initCount.getMax().increment());
              m.put(l.localInstance(), initCount);
              return itemToMap(new DataFlowItem(m), succEdgeKeys);  
          }
          return null;
    }

    /**
     * Perform the appropriate flow operations for assignment to a field
     */
    protected Map flowFieldAssign(DataFlowItem inItem, 
                                  FlowGraph graph, 
                                  FieldAssign a, 
                                  Set succEdgeKeys) {
        Field f = (Field)a.left();
        FieldInstance fi = f.fieldInstance();
        
        if (fi.flags().isFinal() && isFieldsTargetAppropriate(f)) {
            // this field is final and the target for this field is 
            // appropriate for what we are interested in.
            Map m = new HashMap(inItem.initStatus);
            MinMaxInitCount initCount = (MinMaxInitCount)m.get(fi);
            // initCount may be null if the field is defined in an
            // outer class.
            if (initCount != null) {
                initCount = new MinMaxInitCount(initCount.getMin().increment(),
                          initCount.getMax().increment());
                m.put(fi, initCount);
                return itemToMap(new DataFlowItem(m), succEdgeKeys);
            }
        }
        return null;
    }
                                  
    /**
     * Perform the appropriate flow operations for a constructor call
     */
    protected Map flowConstructorCall(DataFlowItem inItem, 
                                      FlowGraph graph, 
                                      ConstructorCall cc, 
                                      Set succEdgeKeys) {
        if (ConstructorCall.THIS.equals(cc.kind())) {
            // currCodeDecl must be a ConstructorDecl, as that
            // is the only place constructor calls are allowed
            // record the fact that the current constructor calls the other
            // constructor
            currCBI.constructorCalls.put(((ConstructorDecl)currCBI.currCodeDecl).constructorInstance(), 
                                 cc.constructorInstance());
        }
        return null;
    }
    
    /**
     * Determine if we are interested in this field on the basis of the
     * target of the field. To wit, if the field
     * is static, then the target of the field must be the current class; if
     * the field is not static then the target must be "this".
     */
    protected boolean isFieldsTargetAppropriate(Field f) {
        if (f.fieldInstance().flags().isStatic()) {
            ClassType containingClass = (ClassType)currCBI.currCodeDecl.codeInstance().container();
            return containingClass.equals(f.fieldInstance().container());
        }
        else {
            return (f.target() instanceof Special && 
                    Special.THIS.equals(((Special)f.target()).kind()));
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
     * MinMaxInitCounts to the map currClassFinalFieldInitCounts.
     */
    public void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException {
        DataFlowItem dfIn = (DataFlowItem)inItem;        
        if (dfIn == null) {
            // There is no input data flow item. This can happen if we are 
            // checking an unreachable term, and so no Items have flowed 
            // through the term. For example, in the code fragment:
            //     a: do { break a; } while (++i < 10);
            // the expression "++i < 10" is unreachable, but the as there is
            // no unreachable statement, the Java Language Spec permits it.
            
            // Set inItem to a default Item
            dfIn = (DataFlowItem)createInitialItem(graph);             
        }
        
        DataFlowItem dfOut = null;
        if (outItems != null && !outItems.isEmpty()) {
            // due to the flow equations, all DataFlowItems in the outItems map
            // are the same, so just take the first one.
            dfOut = (DataFlowItem)outItems.values().iterator().next(); 
        }
        
        if (n instanceof Local) {
            checkLocal(graph, (Local)n, dfIn, dfOut);
        }
        else if (n instanceof LocalAssign) {
            checkLocalAssign(graph, (LocalAssign)n, dfIn, dfOut);
        }
        else if (n instanceof FieldAssign) {
            checkFieldAssign(graph, (FieldAssign)n, dfIn, dfOut);
        }
        else if (n instanceof ClassBody) {
            // we need to check that the locals used inside this class body
            // have all been defined at this point.
            Set localsUsed = (Set)currCBI.localsUsedInClassBodies.get(n);
            
            if (localsUsed != null) {
                checkLocalsUsedByInnerClass(graph, 
                                            (ClassBody)n, 
                                            localsUsed,
                                            dfIn, 
                                            dfOut);
            }            
        }
        
        if (n == graph.finishNode()) {            
            if (currCBI.currCodeDecl instanceof Initializer) {
                // We are finishing the checking of an intializer.
                // We need to copy back the init counts of any fields back into
                // currClassFinalFieldInitCounts, so that the counts are 
                // correct for the next initializer or constructor.
                Iterator iter = dfOut.initStatus.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry e = (Map.Entry)iter.next();
                    if (e.getKey() instanceof FieldInstance) {
                        FieldInstance fi = (FieldInstance)e.getKey();
                        if (fi.flags().isFinal()) {
                            // we don't need to join the init counts, as all
                            // dataflows will go through all of the 
                            // initializers
                            currCBI.currClassFinalFieldInitCounts.put(fi, 
                                    e.getValue());
                        }
                    }
                }                
            }
        }        
    }

    /**
     * Check that the local variable <code>l</code> is used correctly.
     */
    protected void checkLocal(FlowGraph graph, 
                              Local l, 
                              DataFlowItem dfIn, 
                              DataFlowItem dfOut) 
        throws SemanticException {
        MinMaxInitCount initCount = (MinMaxInitCount) 
                  dfIn.initStatus.get(l.localInstance());
        if (initCount == null) {
            // it's a local variable that has not been declared within
            // this scope. The only way this can arise is from an
            // inner class that is not a member of a class (typically
            // a local class, or an anonymous class declared in a method,
            // constructor or initializer).
            // We need to check that it is a final local, and also
            // keep track of it, to ensure that it has been definitely
            // assigned at this point.
            currCBI.outerLocalsUsed.add(l.localInstance());                
        }
        else { 
            if (InitCount.ZERO.equals(initCount.getMin())) {
                throw new SemanticException("Local variable \"" + l.name() +
                        "\" may not have been initialized",
                        l.position());
            }
        }
    }
        
    /**
     * Check that the assignment to a local variable is correct.
     */
    protected void checkLocalAssign(FlowGraph graph, 
                                    LocalAssign a, 
                                    DataFlowItem dfIn, 
                                    DataFlowItem dfOut) 
        throws SemanticException {
        LocalInstance li = ((Local)a.left()).localInstance();
        MinMaxInitCount initCount = (MinMaxInitCount) 
                               dfOut.initStatus.get(li);                                
        if (initCount == null) {
            throw new SemanticException("Final local variable \"" + li.name() +
                    "\" cannot be assigned to in an inner class.",
                    a.position());                     
        }

        if (li.flags().isFinal() && InitCount.MANY.equals(initCount.getMax())) {
            throw new SemanticException("variable \"" + li.name() +
                                        "\" might already have been assigned to",
                                        a.position());
        }
    }

    /**
     * Check that the assignment to a field is correct.
     */
    protected void checkFieldAssign(FlowGraph graph, 
                                    FieldAssign a, 
                                    DataFlowItem dfIn, 
                                    DataFlowItem dfOut) 
        throws SemanticException {
        Field f = (Field)a.left();
        FieldInstance fi = f.fieldInstance();
        if (fi.flags().isFinal()) {
            if ((currCBI.currCodeDecl instanceof ConstructorDecl ||
                    currCBI.currCodeDecl instanceof Initializer) &&
                    isFieldsTargetAppropriate(f)) {
                // we are in a constructor or initializer block and 
                // if the field is static then the target is the class
                // at hand, and if it is not static then the
                // target of the field is this. 
                // So a final field in this situation can be 
                // assigned to at most once.                    
                MinMaxInitCount initCount = (MinMaxInitCount) 
                                       dfOut.initStatus.get(fi);                                
                if (InitCount.MANY.equals(initCount.getMax())) {
                    throw new SemanticException("field \"" + fi.name() +
                            "\" might already have been assigned to",
                            a.position());
                }
                    
                // if the field is non-static and final, and we are in
                // a constructor, record the fact that this constructor 
                // initializes the field 
                if (!fi.flags().isStatic() && currCBI.currCodeDecl instanceof ConstructorDecl) {
                    ConstructorInstance ci = ((ConstructorDecl)currCBI.currCodeDecl).constructorInstance();
                    Set s = (Set)currCBI.fieldsConstructorInitializes.get(ci);
                    if (s == null) {
                        s = new HashSet();
                        currCBI.fieldsConstructorInitializes.put(ci, s);
                    }
                    s.add(fi);
                }
                
            }
            else {
                // not in a constructor or intializer, or the target is
                // not appropriate. So we cannot assign 
                // to a final field at all.
                throw new SemanticException("Cannot assign a value " +
                           "to final field \"" + fi.name() + "\"",
                           a.position());
            }
        }                        
    }
    
    /**
     * Check that the set of <code>LocalInstance</code>s 
     * <code>localsUsed</code>, which is the set of locals used in the inner 
     * class declared by <code>cb</code>
     * are initialized before the class declaration.
     */
    protected void checkLocalsUsedByInnerClass(FlowGraph graph, 
                                               ClassBody cb,
                                               Set localsUsed,
                                               DataFlowItem dfIn,
                                               DataFlowItem dfOut) 
    throws SemanticException {
        for (Iterator iter = localsUsed.iterator(); iter.hasNext(); ) {
            LocalInstance li = (LocalInstance)iter.next();
            MinMaxInitCount initCount = (MinMaxInitCount)
                                            dfOut.initStatus.get(li);                                
    
            if (initCount == null) {
                // the local wasn't defined in this scope.
                currCBI.outerLocalsUsed.add(li);
            }
            else if (InitCount.ZERO.equals(initCount.getMin())) {
                throw new SemanticException("Local variable \"" + li.name() +
                        "\" must be initialized before the class " + 
                        "declaration.",
                        cb.position());
            }            
        }
    }

    
}
