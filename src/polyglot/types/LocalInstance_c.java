package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>LocalInstance</code> contains type information for a local variable.
 */
public class LocalInstance_c extends VarInstance_c implements LocalInstance
{
    /** Used for deserializing types. */
    protected LocalInstance_c() { }

    public LocalInstance_c(TypeSystem ts, Position pos,
	  		   Flags flags, Type type, String name) {
        super(ts, pos, flags, type, name);
    }

    public void setConstantValue(Object constantValue) {
	if (! (constantValue == null) &&
	    ! (constantValue instanceof Boolean) &&
	    ! (constantValue instanceof Number) &&
	    ! (constantValue instanceof Character) &&
	    ! (constantValue instanceof String)) {

	    throw new InternalCompilerError(
		"Can only set constant value to a primitive or String.");
	}

	this.constantValue = constantValue;
        this.isConstant = true;
    }

    public LocalInstance constantValue(Object constantValue) {
        LocalInstance_c n = (LocalInstance_c) copy();
        n.setConstantValue(constantValue);
        return n;
    }

    public LocalInstance flags(Flags flags) {
        LocalInstance_c n = (LocalInstance_c) copy();
	n.flags = flags;
	return n;
    }

    public LocalInstance name(String name) {
        LocalInstance_c n = (LocalInstance_c) copy();
	n.name = name;
	return n;
    }

    public LocalInstance type(Type type) {
        LocalInstance_c n = (LocalInstance_c) copy();
	n.type = type;
	return n;
    }
    
    public void setType(Type type) {
	this.type = type;
    }

    public boolean equals(Object o) {
        if (o instanceof LocalInstance) {
            LocalInstance i = (LocalInstance) o;
            return super.equals(i);
        }

        return false;
    }

    public String toString() {
        return "local " + flags.translate() + type + " " + name +
	    (constantValue != null ? (" = " + constantValue) : "");
    }

    public boolean isCanonical() {
	return type.isCanonical();
    }
}
