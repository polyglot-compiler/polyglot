package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.types.Package;
import java.io.*;

/**
 * A place holder type used to serialize types that cannot be serialized.  
 */
public class PlaceHolder_c implements PlaceHolder
{
    String name;
    Type outer;

    /** Used for deserializing types. */
    protected PlaceHolder_c() { }
    
    /** Creates a place holder type for the type. */
    public PlaceHolder_c(Type t) {
	if (t.isClass()) {
	    name = t.toClass().fullName();

            if (t.toClass().isMember()) {
                outer = t.toClass().container();
            }
        }
	else {
	    throw new InternalCompilerError("Cannot serialize " + t + ".");
	}
    }

    /** Restore the placeholder into a proper type. */ 
    public TypeObject resolve(TypeSystem ts) {
        try {
            if (outer == null) {
                return ts.systemResolver().findType(name);
            }
            else {
                ClassType o = (ClassType) outer.toClass();
                ClassType m = o.memberClassNamed(name);

                if (m == null) {
                    throw new SemanticException("Member class \"" + name +
                        "\" not found in class " + o + ".");
                }

                return m;
            }
	} catch (SemanticException se) {
	    throw new InternalCompilerError(se.getMessage());
	}
    }

    public String translate(Resolver c) {
	throw new InternalCompilerError("Cannot translate place holder type.");
    }

    public String toString() {
	return "PlaceHolder(" + (outer == null ? "" : outer.toString() + ", ") 
	    + name + ")";
    }
}
