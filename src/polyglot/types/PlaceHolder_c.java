package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import jltools.types.Package;
import java.io.*;

/**
 * A place holder type used to serialize types that cannot be serialized.  
 */
public class PlaceHolder_c extends Type_c
{
    boolean primitive;
    String name;
    Type outer;

    /** Used for deserializing types. */
    protected PlaceHolder_c() { }
    
    /** Creates a place holder type for the type. */
    public PlaceHolder_c(Type t) {
        super(t.typeSystem(), t.position());

	if (t.isPrimitive()) {
	    name = t.toPrimitive().kind().toString();
	    primitive = true;
	}
	else if (t.isClass() && t.toClass().isTopLevel()) {
	    primitive = false;
	    name = t.toClass().toTopLevel().fullName();
	}
	else if (t.isClass() && t.toClass().isMember()) {
	    primitive = false;
	    name = t.toClass().toMember().name();
	    outer = (Type) ts.placeHolder(t.toClass().toMember().container());
	}
	else {
	    throw new InternalCompilerError("Cannot serialize " + t + ".");
	}
    }

    /** Restore the placeholder into a proper type. */ 
    public TypeObject restore_() throws SemanticException {
	if (primitive) {
	    return ts.primitiveForName(name);
	}
	else if (outer == null) {
	    return ts.systemResolver().findType(name);
	}
	else {
	    ClassType o = (ClassType) outer.restore();
	    ClassType m = o.memberClassNamed(name);

	    if (m == null) {
		throw new SemanticException("Member class \"" + name +
		    "\" not found in class " + o + ".");
	    }

	    return m;
	}
    }

    public String translate(Resolver c) {
	throw new InternalCompilerError("Cannot translate place holder type.");
    }

    public String toString() {
	return "PlaceHolder(" + name + ")";
    }
}
