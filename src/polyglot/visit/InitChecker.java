package polyglot.visit;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.frontend.*;
import java.util.*;

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
    
    /** The current CodeDecl being processed by the dataflow equations */
    private CodeDecl currentCodeDecl = null;
    /** 
     * A Map of all the final fields in the class currently being processed
     * to MinMaxInitCounts. This Map is used as the basis for the Maps returned
     * in createInitialItem(). 
     * */
    private Map currentClassFinalFieldInitCounts = null;
    /**
     * List of all the constructors. These will be checked once all the
     * initializer blocks have been processed.
     */
    private List allConstructors = null;
    
    /**
     * Map from ConstructorInstances to ConstructorInstances detailing
     * which constructors call which constructors.
     * This is used in checking the initialization of final fields.
     */
    private Map constructorCalls = null;
    
    /**
     * Map from ConstructorInstances to Sets of FieldInstances, detailing
     * which final non-static fields each constructor initializes. 
     * This is used in checking the initialization of final fields.
     */
    private Map fieldsConstructorInitializes = null;

    /**
     * Class representing the initialization counts of variables. The
     * different values of the counts that we are interested in are ZERO,
     * ONE and MANY.
     */
    private static class InitCount {
        static InitCount ZERO = new InitCount(0); 
        static InitCount ONE = new InitCount(1); 
        static InitCount MANY = new InitCount(2); 
        private int count;
        private InitCount(int i) {
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
    private static class MinMaxInitCount {
        private InitCount min, max;
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
        currentCodeDecl = code;
        return new FlowGraph(root, forward);
    }

    /**
     * Overridden superclass method.
     * 
     * Set up the state that must be tracked during a Class Declaration.
     */
    protected NodeVisitor enterCall(Node n) throws SemanticException {
      if (n instanceof ClassDecl) {
            // we are starting to process a class declaration, but have yet
            // to do any of the dataflow analysis.
            
          allConstructors = new ArrayList();
          constructorCalls = new HashMap();
          fieldsConstructorInitializes = new HashMap();

            // set up currentClassFinalFieldInitCounts to contain mappings
            // for all the final fields of the class.
            currentClassFinalFieldInitCounts = new HashMap();            
            
            
            Iterator classMembers = ((ClassDecl)n).body().members().iterator();            
            while (classMembers.hasNext()) {
                ClassMember cm = (ClassMember)classMembers.next();
                if (cm instanceof FieldDecl) {
                    FieldDecl fd = (FieldDecl)cm;
                    if (fd.flags().isFinal()) {
                        MinMaxInitCount initCount;
                        if (fd.init() != null) {
                            // the field has an initializer
                            initCount = new MinMaxInitCount(InitCount.ONE,InitCount.ONE);
                        }
                        else {
                            // the field does not have an initializer
                            initCount = new MinMaxInitCount(InitCount.ZERO,InitCount.ZERO);
                        }
                        currentClassFinalFieldInitCounts.put(fd.fieldInstance(),
                                                             initCount);
                    }
                }
            }             
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
    public Node leaveCall(Node n) throws SemanticException {
        if (n instanceof ConstructorDecl) {
            // postpone the checking of the constructors until all the 
            // initializer blocks have been processed.
            allConstructors.add(n);
            return n;
        }
        
        if (n instanceof ClassDecl) {
            // Now that we are at the end of the class declaration, and can
            // be sure that all of the initializer blocks have been processed,
            // we can now process the constructors.
            for (Iterator iter = allConstructors.iterator(); iter.hasNext(); ) {
                ConstructorDecl cd = (ConstructorDecl)iter.next();
                
                // rely on the fact that our dataflow does not change the AST,
                // so we can discard the result of this call.
                dataflow(cd);                
            }
            
            // check that all static fields have been initialized exactly once.             
            for (Iterator iter = currentClassFinalFieldInitCounts.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry e = (Map.Entry)iter.next();
                if (e.getKey() instanceof FieldInstance) {
                    FieldInstance fi = (FieldInstance)e.getKey();
                    if (fi.flags().isStatic() && fi.flags().isFinal()) {
                        MinMaxInitCount initCount = (MinMaxInitCount)e.getValue();
                        if (InitCount.ZERO.equals(initCount.getMin())) {
                            throw new SemanticException("field \"" + fi.name() +
                                                        "\" might not have been initialized",
                                                        n.position());                                
                        }
                    }
                }
            }   

            // for each non-static final field instance, check that all 
            // constructors intialize it exactly once, taking into account constructor calls.
            for (Iterator iter = currentClassFinalFieldInitCounts.keySet().iterator(); iter.hasNext(); ) {
                FieldInstance fi = (FieldInstance)iter.next();
                if (fi.flags().isFinal() && !fi.flags().isStatic()) {
                    // the field is final and not static
                    // it must be initialized exactly once.
                    // navigate up through all of the the constructors
                    // that this constructor calls.
                    
                    for (Iterator iter2 = allConstructors.iterator(); iter2.hasNext(); ) {
                        ConstructorDecl cd = (ConstructorDecl)iter2.next();
                        ConstructorInstance ci = cd.constructorInstance();
                        
                        boolean isInitialized = false;
                            
                        while (ci != null) {
                            Set s = (Set)fieldsConstructorInitializes.get(ci);
                            if (s != null && s.contains(fi)) {
                                if (isInitialized) {
                                    throw new SemanticException("field \"" + fi.name() +
                                            "\" might have already been initialized",
                                            cd.position());                                                                        
                                }
                                isInitialized = true;
                            }                                
                            ci = (ConstructorInstance)constructorCalls.get(ci);
                        }
                        if (!isInitialized) {
                            throw new SemanticException("field \"" + fi.name() +
                                    "\" might not have been initialized",
                                    cd.position());                                
                                
                        }                            
                    }
                }
            }
        }
            

        return super.leaveCall(n);
    }
    /**
     * The initial item to be given to the entry point of the dataflow contains
     * the init counts for the final fields.
     */
    public Item createInitialItem() {
        return new DataFlowItem(new HashMap(currentClassFinalFieldInitCounts));
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
     * Perform the appropriate flow operations for the Terms.
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
        
        if (n instanceof Formal) {
            // formal argument declaration.
            Formal f = (Formal) n;
            Map m = new HashMap(inDFItem.initStatus);
            // a formal argument is always defined.            
            m.put(f.localInstance(), new MinMaxInitCount(InitCount.ONE,InitCount.ONE));
            
            return itemToMap(new DataFlowItem(m), succEdgeKeys);
        }
        
        if (n instanceof LocalDecl) {
            // local variable declaration.
            LocalDecl l = (LocalDecl) n;
            Map m = new HashMap(inDFItem.initStatus);
            if (l.init() == null) {
                // declaration of local var with no initialization
                m.put(l.localInstance(), new MinMaxInitCount(InitCount.ZERO,InitCount.ZERO));
            }
            else {
                // declaration of local var with initialization.
                m.put(l.localInstance(), new MinMaxInitCount(InitCount.ONE,InitCount.ONE));
            }
            return itemToMap(new DataFlowItem(m), succEdgeKeys);
        }

        if (n instanceof Assign) {
            Assign a = (Assign) n;
            if (a.left() instanceof Local) {
                Local l = (Local) a.left();
                Map m = new HashMap(inDFItem.initStatus);
                MinMaxInitCount initCount = (MinMaxInitCount)m.get(l.localInstance());
                
                initCount = new MinMaxInitCount(initCount.getMin().increment(),
                                                initCount.getMax().increment());
                m.put(l.localInstance(), initCount);
                return itemToMap(new DataFlowItem(m), succEdgeKeys);  
            }            
            if (a.left() instanceof Field) {
                Field f = (Field)a.left();
                FieldInstance fi = f.fieldInstance();
                if (fi.flags().isFinal() && isFieldsTargetAppropriate(f)) {
                    // this field is final and the target for this field is 
                    // appropriate for what we are interested in.
                    Map m = new HashMap(inDFItem.initStatus);
                    MinMaxInitCount initCount = (MinMaxInitCount)m.get(fi);
                    initCount = new MinMaxInitCount(initCount.getMin().increment(),
                            initCount.getMax().increment());
                    m.put(fi, initCount);
                    return itemToMap(new DataFlowItem(m), succEdgeKeys);
                }                
            }            
        }
        
        if (n instanceof ConstructorCall) {
            ConstructorCall cc = (ConstructorCall)n;
            if (ConstructorCall.THIS.equals(cc.kind())) {
                // currentCodeDecl must be a ConstructorDecl, as that
                // is the only place constructor calls are allowed
                // record the fact that the current constructor calls the other
                // constructor
                constructorCalls.put(((ConstructorDecl)currentCodeDecl).constructorInstance(), 
                                     cc.constructorInstance());
            }
        }

        return itemToMap(inItem, succEdgeKeys);
    }

    /**
     * Determine if we are interested in this field on the basis of the
     * target of the field. To wit, if the field
     * is static, then the target of the field must be the current class; if
     * the field is not static then the target must be "this".
     */
    private boolean isFieldsTargetAppropriate(Field f) {
        if (f.fieldInstance().flags().isStatic()) {
            ClassType containingClass = (ClassType)currentCodeDecl.codeInstance().container();
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
     * MinMaxInitCounts to the map currentClassFinalFieldInitCounts.
     */
    public void check(FlowGraph graph, Term n, Item inItem, Map outItems) throws SemanticException {
        DataFlowItem dfIn = (DataFlowItem)inItem;        
        DataFlowItem dfOut = null;
        if (outItems != null && !outItems.isEmpty()) {
            // due to the flow equations, all DataFlowItems in the outItems map
            // are the same, so just take the first one.
            dfOut = (DataFlowItem)outItems.values().iterator().next(); 
        }
        if (n instanceof Local) {
            Local l = (Local) n;
            MinMaxInitCount initCount = (MinMaxInitCount) 
                      dfIn.initStatus.get(l.localInstance());
            if (initCount != null) { // ###@@@ I don't like this line; it's symptomatic of other problems.
                if (InitCount.ZERO.equals(initCount.getMin())) {
                    throw new SemanticException("Local variable \"" + l.name() +
                            "\" may not have been initialized",
                            l.position());
                }
            }
        }
        
        if (n instanceof Assign) {
            Assign a = (Assign)n;
            if (a.left() instanceof Local) {
                LocalInstance li = ((Local)a.left()).localInstance();
                MinMaxInitCount initCount = (MinMaxInitCount) 
                                       dfOut.initStatus.get(li);                                
                if (li.flags().isFinal() && InitCount.MANY.equals(initCount.getMax())) {
                    throw new SemanticException("variable \"" + li.name() +
                                                "\" might already have been assigned to",
                                                a.position());
                }
            }
            
            if (a.left() instanceof Field) {
                Field f = (Field)a.left();
                FieldInstance fi = f.fieldInstance();
                if (fi.flags().isFinal()) {
                    if ((currentCodeDecl instanceof ConstructorDecl ||
                        currentCodeDecl instanceof Initializer) &&
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
                        if (!fi.flags().isStatic() && currentCodeDecl instanceof ConstructorDecl) {
                            ConstructorInstance ci = ((ConstructorDecl)currentCodeDecl).constructorInstance();
                            Set s = (Set)fieldsConstructorInitializes.get(ci);
                            if (s == null) {
                                s = new HashSet();
                                fieldsConstructorInitializes.put(ci, s);
                            }
                            s.add(fi);
                        }
                    
                    }
                    else {
                        // not in a constructor or intializer, or the target is
                        // not appropriate. So we cannot assign 
                        // to a final field at all.
                        throw new SemanticException("cannot assign a value " +
                                   "to final variable \"" + fi.name() + "\"",
                                   a.position());
                    }
                }                        
            }
        }
        
        if (n == graph.finishNode()) {            
            if (currentCodeDecl instanceof Initializer) {
                // We are finishing the checking of an intializer.
                // We need to copy back the init counts of any fields back into
                // currentClassFinalFieldInitCounts, so that the counts are 
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
                            currentClassFinalFieldInitCounts.put(fi, 
                                    e.getValue());
                        }
                    }
                }                
            }
        }        
    }
}
