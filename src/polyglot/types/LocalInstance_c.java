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

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof LocalInstance) {
            LocalInstance i = (LocalInstance) o;
            return super.equalsImpl(i);
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
