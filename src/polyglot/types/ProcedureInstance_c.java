package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>ProcedureInstance</code> contains the type information for a Java
 * procedure (either a method or a constructor).
 */
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
	        && ts.isSame(container, i.container())
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

    /** Returns true iff <this> has the same arguments as <p> */
    public boolean hasSameArguments(ProcedureInstance p) {
        return hasArguments(p.argumentTypes());
    }

    public boolean hasArguments(List argTypes) {
        List l1 = this.argumentTypes();
        List l2 = argTypes;

        Iterator i1 = l1.iterator();
        Iterator i2 = l2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            Type t1 = (Type) i1.next();
            Type t2 = (Type) i2.next();

            if (! ts.isSame(t1, t2)) {
                return false;
            }
        }

        return ! (i1.hasNext() || i2.hasNext());
    }

    /** Returns true iff <this> throws fewer exceptions than <p>. */
    public boolean throwsSubset(ProcedureInstance p) {
        SubtypeSet s1 = new SubtypeSet(ts);
        SubtypeSet s2 = new SubtypeSet(ts);

        s1.addAll(this.exceptionTypes());
        s2.addAll(p.exceptionTypes());

        for (Iterator i = s1.iterator(); i.hasNext(); ) {
            Type t = (Type) i.next();
            if (! ts.isUncheckedException(t) && ! s2.contains(t)) {
                return false;
            }
        }

        return true;
    }

    public boolean callValid(ProcedureInstance call) {
        return ts.callValid(this, call.argumentTypes());
    }

    public boolean callValid(List argTypes) {
        List l1 = this.argumentTypes();
        List l2 = argTypes;

        Iterator i1 = l1.iterator();
        Iterator i2 = l2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            Type t1 = (Type) i1.next();
            Type t2 = (Type) i2.next();

            if (! ts.isImplicitCastValid(t2, t1)) {
                return false;
            }
        }

        return ! (i1.hasNext() || i2.hasNext());
    }
}
