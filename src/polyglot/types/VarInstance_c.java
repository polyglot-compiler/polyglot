package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;

/**
 * A <code>VarInstance</code> contains type information for a variable.  It may
 * be either a local or a field.
 */
public abstract class VarInstance_c extends TypeObject_c implements VarInstance
{
    protected Flags flags;
    protected Type type;
    protected String name;
    protected Object constantValue;

    /** Used for deserializing types. */
    protected VarInstance_c() { }

    public VarInstance_c(TypeSystem ts, Position pos,
	                 Flags flags, Type type, String name) {
        super(ts, pos);
	this.flags = flags;
	this.type = type;
	this.name = name;
    }

    public boolean isConstant() {
        return constantValue != null;
    }

    public Object constantValue() {
        return constantValue;
    }

    public Flags flags() {
        return flags;
    }

    public Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public int hashCode() {
        return flags.hashCode() + name.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof VarInstance) {
	    VarInstance i = (VarInstance) o;
	    return flags.equals(i.flags())
	        && type.isSame(i.type())
		&& name.equals(i.name());
	}

	return false;
    }

    public boolean isCanonical() {
	return true;
    }
}
