package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.InternalCompilerError;
import jltools.util.Position;
import jltools.types.Package;
import java.util.*;

/**
 * A <code>ClassType</code> represents a class -- either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 */
public abstract class ClassType_c extends ReferenceType_c implements ClassType
{
    protected Type superType;
    protected List interfaces;
    protected List methods;
    protected List fields;
    protected List constructors;
    protected List memberClasses;
    protected Package package_;
    protected Flags flags;

    /** Used for deserializing types. */
    protected ClassType_c() { }

    public ClassType_c(TypeSystem ts) {
	this(ts, null);
    }

    public ClassType_c(TypeSystem ts, Position pos) {
	super(ts, pos);
    }

    public boolean isCanonical() { return true; }
    public boolean isClass() { return true; }
    public ClassType toClass() { return this; }

    /** Get the class's constructors. */
    public List constructors() {
        return Collections.unmodifiableList(constructors);
    }

    /** Get the class's member classes. */
    public List memberClasses() {
        return Collections.unmodifiableList(memberClasses);
    }

    /** Get the class's methods. */
    public List methods() {
        return Collections.unmodifiableList(methods);
    }

    /** Get the class's fields. */
    public List fields() {
        return Collections.unmodifiableList(fields);
    }

    /** Get the class's interfaces. */
    public List interfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    /** Get the class's super type. */
    public Type superType() {
        return this.superType;
    }

    /** Get a field of the class by name. */
    public FieldInstance fieldNamed(String name) {
        for (Iterator i = fields().iterator(); i.hasNext(); ) {
	    FieldInstance fi = (FieldInstance) i.next();
	    if (fi.name().equals(name)) {
	        return fi;
	    }
	}

	return null;
    }

    /** Get a member class of the class by name. */
    public MemberClassType memberClassNamed(String name) {
        for (Iterator i = memberClasses().iterator(); i.hasNext(); ) {
	    MemberClassType t = (MemberClassType) i.next();
	    if (t.name().equals(name)) {
	        return t;
	    }
	}

	return null;
    }

    public boolean isTopLevel() { return false; }
    public boolean isInner() { return false; }
    public boolean isMember() { return false; }
    public boolean isLocal() { return false; }
    public boolean isAnonymous() { return false; }

    public TopLevelClassType toTopLevel() { return null; }
    public InnerClassType toInner() { return null; }
    public MemberClassType toMember() { return null; }
    public LocalClassType toLocal() { return null; }
    public AnonClassType toAnonymous() { return null; }

    /** Get the class's package. */
    public Package package_() {
        return package_;
    }

    /** Get the class's flags. */
    public Flags flags() {
        return flags;
    }
}
