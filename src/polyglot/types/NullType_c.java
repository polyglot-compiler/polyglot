package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>NullType</code> represents the type of the Java keyword
 * <code>null</code>.
 */
public class NullType_c extends Type_c implements NullType
{
    /** Used for deserializing types. */
    protected NullType_c() { }

    public NullType_c(TypeSystem ts) {
	super(ts);
    }
    
    public String translate(Resolver c) {
	throw new InternalCompilerError("Cannot translate a null type.");
    }

    public String toString() {
	return "type(null)";
    }
    
    public boolean equals(Object o) {
	return o instanceof NullType;
    }

    public int hashCode() {
	return 6060842;
    }

    public boolean isCanonical() { return true; }
    public boolean isNull() { return true; }

    public NullType toNull() { return this; }
}
