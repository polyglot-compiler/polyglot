package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;

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

    public LocalInstance constantValue(Object constantValue) {
	if (! (constantValue instanceof Boolean) &&
	    ! (constantValue instanceof Number) &&
	    ! (constantValue instanceof Character) &&
	    ! (constantValue instanceof String)) {

	    throw new InternalCompilerError(
		"Can only set constant value to a primitive or String.");
	}

        LocalInstance_c n = (LocalInstance_c) copy();
	n.constantValue = constantValue;
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

    public String toString() {
        return "local " + flags.translate() + type + " " + name +
	    (constantValue != null ? (" = " + constantValue) : "");
    }

    public TypeObject restore_() throws SemanticException {
	Type t = (Type) type.restore();

	LocalInstance li = this;

	if (t != li.type()) {
	    li = type(t);
	}

	return li;
    }

    public boolean isCanonical() {
	return type.isCanonical();
    }
}
