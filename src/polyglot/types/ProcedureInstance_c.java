package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;
import java.util.*;

public abstract class ProcedureInstance_c extends TypeObject_c
                                       implements ProcedureInstance
{
    protected ReferenceType container;
    protected Flags flags;
    protected List argTypes;
    protected List excTypes;

    /** Used for deserializing types. */
    protected ProcedureInstance_c() { }

    public ProcedureInstance_c(TypeSystem ts, Position pos,
			       ReferenceType container,
			       Flags flags, List argTypes, List excTypes) {
        super(ts, pos);
	this.container = container;
	this.flags = flags;
	this.argTypes = new ArrayList(argTypes);
	this.excTypes = new ArrayList(excTypes);
    }

    public ReferenceType container() {
        return container;
    }

    public Flags flags() {
        return flags;
    }

    public List argumentTypes() {
        return Collections.unmodifiableList(argTypes);
    }

    public List exceptionTypes() {
        return Collections.unmodifiableList(excTypes);
    }

    public int hashCode() {
        return container.hashCode() + flags.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof ProcedureInstance) {
	    ProcedureInstance i = (ProcedureInstance) o;
	    // FIXME: Check excTypes too?
	    return flags.equals(i.flags())
	        && container.isSame(i.container())
	        && ts.hasSameArguments(this, i);
	}

	return false;
    }

    protected List restoreList(List l) throws SemanticException {
	List n = new ArrayList(l.size());
	boolean changed = false;

	for (Iterator i = l.iterator(); i.hasNext(); ) {
	    TypeObject o = (TypeObject) i.next();
	    TypeObject r = o.restore();
	    changed |= (o != r);
	    n.add(r);
	}

	if (changed) return n;
	return l;
    }

    protected boolean listIsCanonical(List l) {
	for (Iterator i = l.iterator(); i.hasNext(); ) {
	    TypeObject o = (TypeObject) i.next();
	    if (! o.isCanonical()) {
		return false;
	    }
	}

	return true;
    }
}
