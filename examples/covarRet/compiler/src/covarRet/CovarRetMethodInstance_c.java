package polyglot.ext.covarRet;

import polyglot.ext.jl.types.*;
import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>MethodInstance</code> represents the type information for a Java
 * method.
 */
public class CovarRetMethodInstance_c extends MethodInstance_c
{
    /** Used for deserializing types. */
    protected CovarRetMethodInstance_c() { }

    public CovarRetMethodInstance_c(TypeSystem ts, Position pos,
	 		    ReferenceType container,
	                    Flags flags, Type returnType, String name,
			    List argTypes, List excTypes) {
        super(ts, pos, container, flags, returnType, name, argTypes, excTypes);
    }

    public boolean canOverride(MethodInstance mj) {
        MethodInstance mi = this;

        // This is the only rule that has changed.
        if (! ts.isSubtype(mi.returnType(), mj.returnType())) {
            return false;
        } 

        // Force the return types to be the same and then let the super
        // class perform the remainder of the tests.
        return super.canOverride(mj.returnType(mi.returnType()));
    }
}
