package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>ConstructorInstance</code> contains type information for a
 * constructor.
 */
public class ConstructorInstance_c extends ProcedureInstance_c
                                implements ConstructorInstance
{
    /** Used for deserializing types. */
    protected ConstructorInstance_c() { }

    public ConstructorInstance_c(TypeSystem ts, Position pos,
	                         ClassType container,
				 Flags flags, List argTypes, List excTypes) {
        super(ts, pos, container, flags, argTypes, excTypes);
    }

    public ConstructorInstance flags(Flags flags) {
        ConstructorInstance_c n = (ConstructorInstance_c) copy();
	n.flags = flags;
	return n;
    }

    public ConstructorInstance formalTypes(List l) {
        ConstructorInstance_c n = (ConstructorInstance_c) copy();
	n.argTypes = new ArrayList(l);
	return n;
    }

    public ConstructorInstance throwTypes(List l) {
        ConstructorInstance_c n = (ConstructorInstance_c) copy();
	n.excTypes = new ArrayList(l);
	return n;
    }

    public ConstructorInstance container(ClassType container) {
        ConstructorInstance_c n = (ConstructorInstance_c) copy();
	n.container = container;
	return n;
    }

    public String toString() {
	return designator() + " " + flags.translate() + signature();
    }
    
    public String signature() {
        String s = container + "(";

        for (Iterator i = argTypes.iterator(); i.hasNext(); ) {
            Type t = (Type) i.next();
            s += t.toString();

            if (i.hasNext()) {
                s += ",";
            }
        }

        s += ")";

        return s;
    }

    public String designator() {
        return "constructor";
    }

    protected boolean equalsImpl(Object o) {
        if (! (o instanceof ConstructorInstance) ) return false;
        return super.equals(o);
    }

    public boolean isCanonical() {
	return container.isCanonical()
	    && listIsCanonical(argTypes)
	    && listIsCanonical(excTypes);
    }
}
