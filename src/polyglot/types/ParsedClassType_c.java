package jltools.ext.jl.types;

import jltools.types.*;
import jltools.types.Package;
import jltools.util.*;
import jltools.frontend.Job;
import java.io.*;
import java.util.*;


/**
 * ParsedClassType
 *
 * Overview: 
 * A ParsedClassType represents a information that has been parsed (but not
 * necessarily type checked) from a .java file.
 **/
public abstract class ParsedClassType_c extends ClassType_c
				     implements ParsedClassType
{
    protected transient boolean clean = false;
    protected transient Job job;
    
    protected ParsedClassType_c() {
	super();
    }

    public ParsedClassType_c(TypeSystem ts, Job job) {
	super(ts);
	this.job = job;

	this.interfaces = new TypedList(new LinkedList(), Type.class, false);
	this.fields = new TypedList(new LinkedList(), FieldInstance.class, false);
	this.methods = new TypedList(new LinkedList(), MethodInstance.class, false);
	this.constructors = new TypedList(new LinkedList(), ConstructorInstance.class, false);
	this.memberClasses = new TypedList(new LinkedList(), MemberClassType.class, false);

	// Special field in every class.
	this.fields.add(ts.fieldInstance(position(), this,
	    Flags.PUBLIC.set(Flags.STATIC).set(Flags.FINAL),
	    ts.Class(), "class"));
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public boolean isClean() {
        return clean;
    }

    public Job job() {
	return job;
    }

    public void flags(Flags flags) {
	this.flags = flags;
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

    public void addMemberClass(MemberClassType t) {
	memberClasses.add(t);
    }

    public void replaceField(FieldInstance old, FieldInstance fi) {
	fields.remove(old);
	fields.add(fi);
    }

    public void replaceMethod(MethodInstance old, MethodInstance mi) {
	methods.remove(old);
	methods.add(mi);
    }

    public void replaceConstructor(ConstructorInstance old, ConstructorInstance ci) {
	constructors.remove(old);
	constructors.add(ci);
    }

    public void replaceMemberClass(MemberClassType old, MemberClassType t) {
	memberClasses.remove(old);
	memberClasses.add(t);
    }

    /** Return a mutable list of constructors */
    public List constructors() {
        return constructors;
    }

    /** Return a mutable list of member classes */
    public List memberClasses() {
        return memberClasses;
    }

    /** Return a mutable list of methods */
    public List methods() {
        return methods;
    }

    /** Return a mutable list of fields */
    public List fields() {
        return fields;
    }

    /** Return a mutable list of interfaces */
    public List interfaces() {
        return interfaces;
    }

    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException {

	if (in instanceof TypeInputStream) {
	    TypeSystem ts = ((TypeInputStream) in).getTypeSystem();
	    job = null;
	    clean = false;
	}
    }

    public TypeObject restore() throws SemanticException {
	if (package_ != null) {
	    package_ = (Package) package_.restore();
	}

	if (superType != null) {
	    superType = (Type) superType.restore();
	}

	for (ListIterator i = interfaces.listIterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();
	    i.set(t.restore());
	}

	for (ListIterator i = fields.listIterator(); i.hasNext(); ) {
	    FieldInstance t = (FieldInstance) i.next();
	    i.set(t.restore());
	}

	for (ListIterator i = methods.listIterator(); i.hasNext(); ) {
	    MethodInstance t = (MethodInstance) i.next();
	    i.set(t.restore());
	}

	for (ListIterator i = constructors.listIterator(); i.hasNext(); ) {
	    ConstructorInstance t = (ConstructorInstance) i.next();
	    i.set(t.restore());
	}

	for (ListIterator i = memberClasses.listIterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();
	    i.set(t.restore());
	}

	return this;
    }
}
