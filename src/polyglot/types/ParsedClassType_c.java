package polyglot.ext.jl.types;

import java.io.IOException;
import java.io.ObjectInputStream;
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
    
    /** Wether we need to serialize this class. */
    protected boolean needSerialization = true;

    protected ParsedClassType_c() {
	super();
    }

    public ParsedClassType_c(TypeSystem ts, LazyClassInitializer init, 
                             Source fromSource) {
        super(ts);
        this.fromSource = fromSource;

        this.init = init;
        init.setClass(this);

        this.interfaces = new TypedList(new LinkedList(), Type.class, false);
        this.methods = new TypedList(new LinkedList(), MethodInstance.class, false);
        this.fields = new TypedList(new LinkedList(), FieldInstance.class, false);
        this.constructors = new TypedList(new LinkedList(), ConstructorInstance.class, false);
        this.memberClasses = new TypedList(new LinkedList(), Type.class, false);
    }
     
    public void initDependencies() {
        Scheduler scheduler = ts.extensionInfo().scheduler();
        
        try {
            if (this.job() != null) {
                scheduler.addConcurrentDependency(scheduler.MembersAdded(this),
                                                  scheduler.Parsed(this.job()));
                scheduler.addConcurrentDependency(scheduler.SupertypesResolved(this),
                                                  scheduler.TypesInitialized(this.job()));
                scheduler.addConcurrentDependency(scheduler.SignaturesResolved(this),
                                                  scheduler.TypesInitialized(this.job()));
                /*
                 scheduler.addConcurrentDependency(scheduler.MembersAdded(ct),
                 scheduler.TypesInitialized(ct.job()));
                 scheduler.addConcurrentDependency(scheduler.SupertypesResolved(ct),
                 scheduler.TypeChecked(ct.job()));
                 scheduler.addConcurrentDependency(scheduler.AllMembersAdded(ct),
                 scheduler.TypeChecked(ct.job()));
                 scheduler.addConcurrentDependency(scheduler.SignaturesResolved(ct),
                 scheduler.TypeChecked(ct.job()));
                 */
            }
            
            scheduler.addPrerequisiteDependency(scheduler.SignaturesResolved(this),
                                                scheduler.MembersAdded(this));
            scheduler.addPrerequisiteDependency(scheduler.SignaturesResolved(this),
                                                scheduler.SupertypesResolved(this));
        }
        catch (CyclicDependencyException e) {
            throw new InternalCompilerError(e.getMessage());
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
        initDependencies();
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
        init.initSuperclass();
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
    
    public boolean defaultConstructorNeeded() {
        if (flags().isInterface()) {
            return false;
        }
        return constructors().isEmpty();
    }
    
    /** Return an immutable list of constructors */
    public List constructors() {
        init.initConstructors();
        return Collections.unmodifiableList(constructors);
    }

    /** Return an immutable list of member classes */
    public List memberClasses() {
        init.initMemberClasses();
        return Collections.unmodifiableList(memberClasses);
    }

    /** Return an immutable list of methods. */
    public List methods() {
        init.initMethods();
        return Collections.unmodifiableList(methods);
    }

    /** Return an immutable list of fields */
    public List fields() {
        init.initFields();
        return Collections.unmodifiableList(fields);
    }

    /** Return an immutable list of interfaces */
    public List interfaces() {
        init.initInterfaces();
        return Collections.unmodifiableList(interfaces);
    }
    
    boolean membersAdded;
    boolean supertypesResolved;
    boolean signaturesResolved;

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

    public int numSignaturesUnresolved() {
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (signaturesResolved) {
            return 0;
        }
        
        if (! membersAdded()) {
            try {
                scheduler.addPrerequisiteDependency(scheduler.SignaturesResolved(this), scheduler.MembersAdded(this));
            }
            catch (CyclicDependencyException e) {
                throw new InternalCompilerError(e.getMessage());
            }
            return Integer.MAX_VALUE;
        }

        // Create a new list of members.  Don't use members() since
        // it ensures that signatures be resolved and this method
        // is just suppossed to check if they are resolved.
        List l = new ArrayList();
        l.addAll(methods);
        l.addAll(fields);
        l.addAll(constructors);
        l.addAll(memberClasses);
        
        int count = 0;
        
        for (Iterator i = l.iterator(); i.hasNext(); ) {
            MemberInstance mi = (MemberInstance) i.next();
            if (! mi.isCanonical()) {
                count++;
            }
        }
        
        if (count == 0) {
            signaturesResolved = true;
        }
        
        return count;
    }

    public boolean signaturesResolved() {
        Scheduler scheduler = typeSystem().extensionInfo().scheduler();
        
        if (signaturesResolved) {
            return true;
        }
        
        if (! membersAdded()) {
            try {
                scheduler.addPrerequisiteDependency(scheduler.SignaturesResolved(this), scheduler.MembersAdded(this));
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
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (in instanceof TypeInputStream) {
            fromSource = null;
            job = null;
           
            init = ((TypeInputStream) in).getTypeSystem().deserializedClassInitializer();
            membersAdded = true;
            supertypesResolved = true;
            signaturesResolved = true;
        }

        in.defaultReadObject();
    }

    public void clearConstructors() {
    	constructors.clear();
    }
    
    public void needSerialization(boolean b) {
        needSerialization = b;
    }
    
    public boolean needSerialization() {
        return needSerialization;
    }
    
    public void clearMethods() {
    	methods.clear();
    }
}
