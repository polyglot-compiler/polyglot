package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.types.Package;
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

    public boolean isSameImpl(Type t) {
        return t == this;
    }

    public boolean descendsFromImpl(Type ancestor) {
        if (! ancestor.isCanonical()) {
            return false;
        }

        if (ancestor.isNull()) {
            return false;
        }

        if (ts.isSame(this, ancestor)) {
            return false;
        }

        if (! ancestor.isReference()) {
            return false;
        }

        if (ts.isSame(ancestor, ts.Object())) {
            return true;
        }

        // Check subtype relation for classes.
        if (! flags().isInterface()) {
            if (ts.isSame(this, ts.Object())) {
                return false;
            }

            if (superType() == null) {
                return false;
            }

            if (ts.isSubtype(superType(), ancestor)) {
                return true;
            }
        }

        // Next check interfaces.
        for (Iterator i = interfaces().iterator(); i.hasNext(); ) {
            Type parentType = (Type) i.next();

            if (ts.isSubtype(parentType, ancestor)) {
                return true;
            }
        }

        return false;
    }

    public boolean isThrowable() {
        return ts.isSubtype(this, ts.Throwable());
    }

    public boolean isUncheckedException() {
        if (isThrowable()) {
            Collection c = ts.uncheckedExceptions();
                                  
            for (Iterator i = c.iterator(); i.hasNext(); ) {
                Type t = (Type) i.next();

                if (ts.isSubtype(this, t)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isImplicitCastValidImpl(Type toType) {
        if (! toType.isClass()) return false;
        return ts.isSubtype(this, toType);
    }

    /**
     * Requires: all type arguments are canonical.  ToType is not a NullType.
     *
     * Returns true iff a cast from this to toType is valid; in other
     * words, some non-null members of this are also members of toType.
     **/
    public boolean isCastValidImpl(Type toType) {
	if (! toType.isCanonical()) return false;
	if (! toType.isReference()) return false;

	if (toType.isArray()) {
	    // Ancestor is not an array, but child is.  Check if the array
	    // is a subtype of the ancestor.  This happens when ancestor
	    // is java.lang.Object.
	    return ts.isSubtype(toType, this);
	}

	// Both types should be classes now.
	if (! toType.isClass()) return false;

	// From and to are neither primitive nor an array. They are distinct.
	boolean fromInterface = flags().isInterface();
	boolean toInterface   = toType.toClass().flags().isInterface();
	boolean fromFinal     = flags().isFinal();
	boolean toFinal       = toType.toClass().flags().isFinal();

	// This is taken from Section 5.5 of the JLS.
	if (! fromInterface) {
	    // From is not an interface.
	    if (! toInterface) {
		// Nether from nor to is an interface.
		return ts.isSubtype(this, toType) || ts.isSubtype(toType, this);
	    }

	    if (fromFinal) {
		// From is a final class, and to is an interface
		return ts.isSubtype(this, toType);
	    }

	    // From is a non-final class, and to is an interface.
	    return true;
	}
	else {
	    // From is an interface
	    if (! toInterface && ! toFinal) {
		// To is a non-final class.
		return true;
	    }

	    if (toFinal) {
		// To is a final class.
		return ts.isSubtype(toType, this);
	    }

	    // To and From are both interfaces.
	    return true;
	}
    }

    public final boolean isEnclosed(ClassType maybe_outer) {
        return ts.isEnclosed(this, maybe_outer);
    }

    public boolean isEnclosedImpl(ClassType maybe_outer) {
        return false;
    }
}
