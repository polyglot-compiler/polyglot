package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import java.util.*;

/**
 * A <code>MethodInstance</code> represents the type information for a Java
 * method.
 */
public class MethodInstance_c extends ProcedureInstance_c
                                implements MethodInstance
{
    protected String name;
    protected Type returnType;

    /** Used for deserializing types. */
    protected MethodInstance_c() { }

    public MethodInstance_c(TypeSystem ts, Position pos,
	 		    ReferenceType container,
	                    Flags flags, Type returnType, String name,
			    List argTypes, List excTypes) {
        super(ts, pos, container, flags, argTypes, excTypes);
	this.returnType = returnType;
	this.name = name;
    }

    public MethodInstance flags(Flags flags) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.flags = flags;
	return n;
    }

    public String name() {
        return name;
    }

    public MethodInstance name(String name) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.name = name;
	return n;
    }

    public Type returnType() {
        return returnType;
    }

    public MethodInstance returnType(Type returnType) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.returnType = returnType;
	return n;
    }

    public MethodInstance argumentTypes(List l) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.argTypes = new ArrayList(l);
	return n;
    }

    public MethodInstance exceptionTypes(List l) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.excTypes = new ArrayList(l);
	return n;
    }

    public MethodInstance container(ReferenceType container) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.container = container;
	return n;
    }

    public int hashCode() {
        return container.hashCode() + flags.hashCode() +
	       returnType.hashCode() + name.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof MethodInstance) {
	    MethodInstance i = (MethodInstance) o;
	    return returnType.isSame(i.returnType())
	        && name.equals(i.name())
		&& super.equals(i);
	}

	return false;
    }

    public String toString() {
	String s = "method " + flags.translate() + returnType +
	    " " + name + "(";

	for (Iterator i = argTypes.iterator(); i.hasNext(); ) {
	    Type t = (Type) i.next();
	    s += t.toString();

	    if (i.hasNext()) {
	        s += ", ";
	    }
	}

	s += ")";

	if (! excTypes.isEmpty()) {
	    s += " throws ";

	    for (Iterator i = excTypes.iterator(); i.hasNext(); ) {
		Type t = (Type) i.next();
		s += t.toString();

		if (i.hasNext()) {
		    s += ", ";
		}
	    }
	}

	return s;
    }

    public String signature() {
		String s = name + "(";

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
        return "method";
    }


    public TypeObject restore_() throws SemanticException {
	ReferenceType c = (ReferenceType) container.restore();
	Type t = (Type) returnType.restore();
	List a = restoreList(argTypes);
	List e = restoreList(excTypes);

	MethodInstance mi = this;

	if (c != container) mi = mi.container(c);
	if (t != returnType) mi = mi.returnType(t);
	if (a != argTypes) mi = mi.argumentTypes(a);
	if (e != excTypes) mi = mi.exceptionTypes(e);

	return mi;
    }

    public boolean isCanonical() {
	return container.isCanonical()
	    && returnType.isCanonical()
	    && listIsCanonical(argTypes)
	    && listIsCanonical(excTypes);
    }
}
