package polyglot.ext.jl.types;

import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>InitializerInstance</code> contains the type information for a
 * static or anonymous initializer.
 */
public class InitializerInstance_c extends TypeObject_c
                                implements InitializerInstance
{
    protected ClassType container;
    protected Flags flags;

    /** Used for deserializing types. */
    protected InitializerInstance_c() { }

    public InitializerInstance_c(TypeSystem ts, Position pos,
				 ClassType container, Flags flags) {
        super(ts, pos);
	this.container = container;
	this.flags = flags;
    }

    public ReferenceType container() {
        return container;
    }

    public InitializerInstance container(ClassType container) {
        InitializerInstance_c n = (InitializerInstance_c) copy();
	n.container = container;
        return n;
    }

    public Flags flags() {
        return flags;
    }

    public InitializerInstance flags(Flags flags) {
        InitializerInstance_c n = (InitializerInstance_c) copy();
	n.flags = flags;
        return n;
    }

    public int hashCode() {
        return container.hashCode() + flags.hashCode();
    }

    protected boolean equalsImpl(Object o) {
        if (o instanceof InitializerInstance) {
	    InitializerInstance i = (InitializerInstance) o;
	    return flags.equals(i.flags()) && ts.equals(container, i.container());
	}

	return false;
    }

    public String toString() {
        return flags.translate() + "initializer";
    }

    public boolean isCanonical() {
	return true;
    }
}
