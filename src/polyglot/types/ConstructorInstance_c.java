package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import java.util.*;

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

    public ConstructorInstance argumentTypes(List l) {
        ConstructorInstance_c n = (ConstructorInstance_c) copy();
	n.argTypes = new ArrayList(l);
	return n;
    }

    public ConstructorInstance exceptionTypes(List l) {
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
	String s = "constructor " + flags.translate() + container + "(";

	for (Iterator i = argTypes.iterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();
	    s += t.toString();

	    if (i.hasNext()) {
	        s += ", ";
	    }
	}

	s += ")";

	return s;
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof ConstructorInstance) ) return false;
        return super.equals(o);
    }

    public TypeObject restore_() throws SemanticException {
	ClassType c = (ClassType) container.restore();
	List a = restoreList(argTypes);
	List e = restoreList(excTypes);

	ConstructorInstance ci = this;

	if (c != container) ci = ci.container(c);
	if (a != argTypes) ci = ci.argumentTypes(a);
	if (e != excTypes) ci = ci.exceptionTypes(e);

	return ci;
    }

    public boolean isCanonical() {
	return container.isCanonical()
	    && listIsCanonical(argTypes)
	    && listIsCanonical(excTypes);
    }
}
