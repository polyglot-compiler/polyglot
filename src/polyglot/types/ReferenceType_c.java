package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>ReferenceType</code> represents a reference type --
 * a type on which contains methods and fields and is a subtype of
 * Object.
 */
public abstract class ReferenceType_c extends Type_c implements ReferenceType
{
    protected ReferenceType_c() {
	super();
    }

    public ReferenceType_c(TypeSystem ts) {
	this(ts, null);
    }

    public ReferenceType_c(TypeSystem ts, Position pos) {
	super(ts, pos);
    }

    public boolean isReference() { return true; }
    public ReferenceType toReference() { return this; }

    /**
     * Returns a list of MethodInstances for all the methods declared in this.
     * It does not return methods declared in supertypes.
     */
    public abstract List methods();

    /**
     * Returns a list of FieldInstances for all the fields declared in this.
     * It does not return fields declared in supertypes.
     */
    public abstract List fields();

    /** 
     * Returns the supertype of this class.  For every class except Object,
     * this is non-null.
     */
    public abstract Type superType();

    /**
     * Returns a list of the types of this class's interfaces.
     */
    public abstract List interfaces();
}
