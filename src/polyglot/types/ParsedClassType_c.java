package polyglot.ext.jl.types;

import java.util.*;

import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.types.Package;
import polyglot.util.*;

/**
 * ParsedClassType
 *
 * Overview: 
 * A ParsedClassType represents a information that has been parsed (but not
 * necessarily type checked) from a .java file.
 **/
public class ParsedClassType_c extends ClassType_c implements ParsedClassType
{
    protected transient LazyClassInitializer init;
    protected transient Source fromSource;
    protected transient Job job;
    protected Type superType;
    protected List interfaces;
    protected List methods;
    protected List fields;
    protected List constructors;
    protected List memberClasses;
    protected Package package_;
    protected Flags flags;
    protected Kind kind;
    protected String name;
    protected ClassType outer;
    
    /** Was the class declared in a static context? */
    protected boolean inStaticContext = false;

    protected ParsedClassType_c() {
	super();
    }

    public ParsedClassType_c(TypeSystem ts, LazyClassInitializer init, 
                             Source fromSource) {
        super(ts);
        this.init = init;
        this.fromSource = fromSource;

        this.interfaces = new TypedList(new LinkedList(), Type.class, false);
        this.methods = new TypedList(new LinkedList(), MethodInstance.class, false);
        this.fields = new TypedList(new LinkedList(), FieldInstance.class, false);
        this.constructors = new TypedList(new LinkedList(), ConstructorInstance.class, false);
        this.memberClasses = new TypedList(new LinkedList(), Type.class, false);
        
        if (init == null) {
            throw new InternalCompilerError("Null lazy class initializer");
        }
    }
     
    public Source fromSource() {
        return fromSource;
    }
    
    public Job job() {
        return job;
    }
    
    public void setJob(Job job) {
        this.job = job;
    }
    
    public Kind kind() {
        return kind;
    }

    public void inStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    public boolean inStaticContext() {
        return inStaticContext;
    }
    
    public ClassType outer() {
        if (isTopLevel())
            return null;
        if (outer == null)
            throw new InternalCompilerError("Nested classes must have outer classes.");
            
        return outer;
    }

    public String name() {
        if (isAnonymous())
            throw new InternalCompilerError("Anonymous classes cannot have names.");

        if (name == null)
            throw new InternalCompilerError("Non-anonymous classes must have names.");
        return name;
    }

    /** Get the class's super type. */
    public Type superType() {
        if (init != null && ! init.superclassInitialized()) {
            init.initSuperclass(this);
            freeInit();
        }
        requireSupertypesResolved();
        return this.superType;
    }

    /** Get the class's package. */
    public Package package_() {
        return package_;
    }

    /** Get the class's flags. */
    public Flags flags() {
        if (isAnonymous())
            return Flags.NONE;
        return flags;
    }
    
    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    /** Free the initializer object if we no longer need it. */
    protected void freeInit() {
        if (init.initialized()) {
            init = null;
        }
        else if (init == null) {
            throw new InternalCompilerError("Null lazy class initializer");
        }
    }

    public void flags(Flags flags) {
	this.flags = flags;
    }

    public void kind(Kind kind) {
        this.kind = kind;
    }

    public void outer(ClassType outer) {
        if (isTopLevel())
            throw new InternalCompilerError("Top-level classes cannot have outer classes.");
        this.outer = outer;
    }
    
    public void setContainer(ReferenceType container) {
        if (container instanceof ClassType && isMember()) {
            outer((ClassType) container);
        }
        else {
            throw new InternalCompilerError("Only member classes can have containers.");
        }
    }

    public void name(String name) {
        if (isAnonymous())
            throw new InternalCompilerError("Anonymous classes cannot have names.");
        this.name = name;
    }

    public void position(Position pos) {
	this.position = pos;
    }

    public void package_(Package p) {
	this.package_ = p;
    }

    public void superType(Type t) {
	this.superType = t;
    }

    public void addInterface(Type t) {
	interfaces.add(t);
    }

    public void addMethod(MethodInstance mi) {
	methods.add(mi);
    }

    public void addConstructor(ConstructorInstance ci) {
	constructors.add(ci);
    }

    public void addField(FieldInstance fi) {
	fields.add(fi);
    }

    public void addMemberClass(ClassType t) {
	memberClasses.add(t);
    }
    
    public void requireSupertypesResolved() {
        if (supertypesResolved) {
            return;
        }
        
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (job() == scheduler.currentJob()) {
            // throw new UnavailableTypeException(this);
            return;
        }
        
        Goal g = new SupertypesResolved(this);
        try {
            boolean result = scheduler.attemptGoal(g);
        }
        catch (CyclicDependencyException e) {
            scheduler.addConcurrentDependency(scheduler.currentGoal(), g);
            throw new UnavailableTypeException(this);
        }
    }

    public void requireMembersAdded() {
        if (membersAdded) {
            return;
        }
        
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (job() == scheduler.currentJob()) {
            // throw new UnavailableTypeException(this);
            return;
        }
        
        Goal g = new MembersAdded(this);
        try {
            boolean result = scheduler.attemptGoal(g);
        }
        catch (CyclicDependencyException e) {
            scheduler.addConcurrentDependency(scheduler.currentGoal(), g);
            throw new UnavailableTypeException(this);
        }
    }
       
    public void requireSignaturesResolved() {
        if (signaturesResolved) {
            return;
        }
        
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (job() == scheduler.currentJob()) {
            // throw new UnavailableTypeException(this);
            return;
        }
        
        Goal g = new SignaturesResolved(this);
        try {
            boolean result = scheduler.attemptGoal(g);
        }
        catch (CyclicDependencyException e) {
            scheduler.addConcurrentDependency(scheduler.currentGoal(), g);
            throw new UnavailableTypeException(this);
        }
    }
    
    /** Return a mutable list of constructors */
    public List constructors() {
        if (init != null && ! init.constructorsInitialized()) {
            init.initConstructors(this);
            freeInit();
        }
        requireSignaturesResolved();
        return constructors;
    }

    /** Return a mutable list of member classes */
    public List memberClasses() {
        if (init != null && ! init.memberClassesInitialized()) {
            init.initMemberClasses(this);
            freeInit();
        }
        requireMembersAdded();
        return memberClasses;
    }

    /** Return an immutable list of methods. */
    public List methods() {
        if (init != null && ! init.methodsInitialized()) {
            init.initMethods(this);
            freeInit();
        }
        requireSignaturesResolved();
        return methods;
    }

    /** Return a mutable list of fields */
    public List fields() {
        if (init != null && ! init.fieldsInitialized()) {
            init.initFields(this);
            freeInit();
        }
        requireSignaturesResolved();
        return fields;
    }

    /** Return a mutable list of interfaces */
    public List interfaces() {
        if (init != null && ! init.interfacesInitialized()) {
            init.initInterfaces(this);
            freeInit();
        }
        requireSupertypesResolved();
        return interfaces;
    }
    
    boolean membersAdded;
    boolean supertypesResolved;
    boolean allMembersAdded;
    boolean signaturesResolved;

    /**
     * @param allMembersAdded The allMembersAdded to set.
     */
    public void setAllMembersAdded(boolean allMembersAdded) {
        this.allMembersAdded = allMembersAdded;
    }
    /**
     * @return Returns the membersAdded.
     */
    public boolean membersAdded() {
        return membersAdded;
    }
    
    /**
     * @param membersAdded The membersAdded to set.
     */
    public void setMembersAdded(boolean membersAdded) {
        this.membersAdded = membersAdded;
    }
    /**
     * @param signaturesDisambiguated The signaturesDisambiguated to set.
     */
    public void setSignaturesResolved(boolean signaturesDisambiguated) {
        this.signaturesResolved = signaturesDisambiguated;
    }
    /**
     * @return Returns the supertypesResolved.
     */
    public boolean supertypesResolved() {
        return supertypesResolved;
    }
    /**
     * @param supertypesResolved The supertypesResolved to set.
     */
    public void setSupertypesResolved(boolean supertypesResolved) {
        this.supertypesResolved = supertypesResolved;
    }

    public boolean allMembersAdded() {
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (allMembersAdded) {
            return true;
        }
        
        if (! membersAdded()) {
            try {
                scheduler.addPrerequisiteDependency(new AllMembersAdded(this), new MembersAdded(this));
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e.getMessage());
            }
            return false;
        }
        
        if (! supertypesResolved()) {
            try {
                scheduler.addPrerequisiteDependency(new AllMembersAdded(this), new SupertypesResolved(this));
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e.getMessage());
            }
            return false;
        }
        
        // Don't use superType() or interfaces() since
        // they ensure that supertypes are resolved and this method
        // is just suppossed to check if they are resolved.

        if (superType instanceof ParsedClassType) {
            ParsedClassType superCT = (ParsedClassType) superType;
            if (! superCT.allMembersAdded()) {
                scheduler.addConcurrentDependency(new AllMembersAdded(this), new AllMembersAdded(superCT));
                return false;
            }
        }
        
        for (Iterator i = interfaces.iterator(); i.hasNext(); ) {
            Type t = (Type) i.next();
            if (t instanceof ParsedClassType) {
                ParsedClassType superCT = (ParsedClassType) t;
                if (! superCT.allMembersAdded()) {
                    scheduler.addConcurrentDependency(new AllMembersAdded(this), new AllMembersAdded(superCT));
                    return false;
                }
            }
        }
     
        allMembersAdded = true;
        return true;
    }

    public boolean signaturesResolved() {
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (signaturesResolved) {
            return true;
        }
        
        if (! membersAdded()) {
            try {
                scheduler.addPrerequisiteDependency(new SignaturesResolved(this), new MembersAdded(this));
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e.getMessage());
            }
            return false;
        }

        // Create a new list of members.  Don't use members() since
        // it ensures that signatures be resolved and this method
        // is just suppossed to check if they are resolved.
        List l = new ArrayList();
        l.addAll(methods);
        l.addAll(fields);
        l.addAll(constructors);
        l.addAll(memberClasses);
        
        for (Iterator i = l.iterator(); i.hasNext(); ) {
            MemberInstance mi = (MemberInstance) i.next();
            if (! mi.isCanonical()) {
                return false;
            }
        }
        
        signaturesResolved = true;
        return true;
    }
}
