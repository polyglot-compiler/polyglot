package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.main.Report;
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

    public MethodInstance formalTypes(List l) {
        MethodInstance_c n = (MethodInstance_c) copy();
	n.argTypes = new ArrayList(l);
	return n;
    }

    public MethodInstance throwTypes(List l) {
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
        //return container.hashCode() + flags.hashCode() +
	//       returnType.hashCode() + name.hashCode();
	return flags.hashCode() + name.hashCode();
    }

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof MethodInstance) {
	    MethodInstance i = (MethodInstance) o;
	    return ts.equals(returnType, i.returnType())
	        && name.equals(i.name())
		&& super.equalsImpl(i);
	}

	return false;
    }

    public String toString() {
	String s = designator() + " " + flags.translate() + returnType + " " +
                   signature();

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

    /** Returns true iff <this> is the same method as <m> */
    public final boolean isSameMethod(MethodInstance m) {
        return ts.isSameMethod(this, m);
    }

    /** Returns true iff <this> is the same method as <m> */
    public boolean isSameMethodImpl(MethodInstance m) {
        return this.name().equals(m.name()) && hasFormals(m.formalTypes());
    }

    public boolean isCanonical() {
	return container.isCanonical()
	    && returnType.isCanonical()
	    && listIsCanonical(argTypes)
	    && listIsCanonical(excTypes);
    }

    public final boolean methodCallValid(String name, List argTypes) {
        return ts.methodCallValid(this, name, argTypes);
    }

    public boolean methodCallValidImpl(String name, List argTypes) {
        return name().equals(name) && ts.callValid(this, argTypes);
    }

    public List overrides() {
        return ts.overrides(this);
    }

    public List overridesImpl() {
        List l = new LinkedList();

        Type t = container().superType();

        while (t instanceof ReferenceType) {
            ReferenceType rt = (ReferenceType) t;
            t = rt.superType();

            for (Iterator i = rt.methods(name, argTypes).iterator(); i.hasNext(); ) {
                MethodInstance mi = (MethodInstance) i.next();
                l.add(mi);
            }
        }

        return l;
    }

    public final boolean canOverride(MethodInstance mj) {
        return ts.canOverride(this, mj);
    }

    public boolean canOverrideImpl(MethodInstance mj) {
        MethodInstance mi = this;

        if (! ts.equals(mi.returnType(), mj.returnType())) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, "return type " + mi.returnType() +
                              " != " + mj.returnType());
            return false;
        } 

        if (! ts.throwsSubset(mi, mj)) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, mi.throwTypes() + " not subset of " +
                              mj.throwTypes());
            return false;
        }   

        if (mi.flags().moreRestrictiveThan(mj.flags())) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, mi.flags() + " more restrictive than " +
                              mj.flags());
            return false;
        }

        if (! mi.flags().isStatic() && mj.flags().isStatic()) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, mi.flags() + " not static but " +
                              mj.flags() + " static");
            return false;
        }

        if (mj.flags().isFinal()) {
            if (Report.should_report(Report.types, 3))
                Report.report(3, mj.flags() + " final");
            return false;
        }

        return true;
    }
    
    public List implemented() {
	return ts.implemented(this);
    }

    public List implementedImpl(ReferenceType rt) {
	if (rt == null) {
	    return Collections.EMPTY_LIST;
	}

        List l = new LinkedList();
        l.addAll(rt.methods(name, argTypes));

	Type superType = rt.superType();
	if (superType != null) {
	    l.addAll(implementedImpl(superType.toReference())); 
	}
	
	List ints = rt.interfaces();
	for (Iterator i = ints.iterator(); i.hasNext(); ) {
	    ReferenceType rt2 = (ReferenceType) i.next();
	    l.addAll(implementedImpl(rt2));
	}
	
        return l;
    }

}
