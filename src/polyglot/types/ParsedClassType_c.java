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
    protected transient LazyClassInitializer init;

    protected ParsedClassType_c() {
	super();
    }

    public ParsedClassType_c(TypeSystem ts, LazyClassInitializer init) {
	super(ts);
        this.init = init;

        if (init == null) {
          throw new InternalCompilerError("Null lazy class initializer");
        }
    }

    /** Return true if we no longer need the initializer object. */
    protected boolean initialized() {
        return this.methods != null &&
               this.constructors != null &&
               this.fields != null &&
               this.memberClasses != null &&
               this.interfaces != null;
    }

    /** Free the initializer object if we no longer need it. */
    protected void freeInit() {
        if (initialized()) {
            init = null;
        }
        else if (init == null) {
          throw new InternalCompilerError("Null lazy class initializer");
        }
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
	interfaces().add(t);
    }

    public void addMethod(MethodInstance mi) {
	methods().add(mi);
    }

    public void addConstructor(ConstructorInstance ci) {
	constructors().add(ci);
    }

    public void addField(FieldInstance fi) {
	fields().add(fi);
    }

    public void addMemberClass(MemberClassType t) {
	memberClasses().add(t);
    }

    public void replaceField(FieldInstance old, FieldInstance fi) {
	fields().remove(old);
	fields().add(fi);
    }

    public void replaceMethod(MethodInstance old, MethodInstance mi) {
	methods().remove(old);
	methods().add(mi);
    }

    public void replaceConstructor(ConstructorInstance old, ConstructorInstance ci) {
	constructors().remove(old);
	constructors().add(ci);
    }

    public void replaceMemberClass(MemberClassType old, MemberClassType t) {
	memberClasses().remove(old);
	memberClasses().add(t);
    }

    /** Return a mutable list of constructors */
    public List constructors() {
        if (constructors == null) {
            constructors = new TypedList(new LinkedList(), ConstructorInstance.class, false);
            init.initConstructors(this);
            freeInit();
        }
        return constructors;
    }

    /** Return a mutable list of member classes */
    public List memberClasses() {
        if (memberClasses == null) {
            memberClasses = new TypedList(new LinkedList(), MemberClassType.class, false);
            init.initMemberClasses(this);
            freeInit();
        }
        return memberClasses;
    }

    /** Return a mutable list of methods */
    public List methods() {
        if (methods == null) {
            methods = new TypedList(new LinkedList(), MethodInstance.class, false);
            init.initMethods(this);
            freeInit();
        }
        return methods;
    }

    /** Return a mutable list of fields */
    public List fields() {
        if (fields == null) {
            fields = new TypedList(new LinkedList(), FieldInstance.class, false);
            init.initFields(this);
            freeInit();
        }
        return fields;
    }

    /** Return a mutable list of interfaces */
    public List interfaces() {
        if (interfaces == null) {
            interfaces = new TypedList(new LinkedList(), Type.class, false);
            init.initInterfaces(this);
            freeInit();
        }
        return interfaces;
    }

    public TypeObject restore_() throws SemanticException {
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
