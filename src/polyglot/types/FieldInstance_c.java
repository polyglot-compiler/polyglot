package jltools.ext.jl.types;

import jltools.types.*;
import jltools.util.*;

public class FieldInstance_c extends VarInstance_c implements FieldInstance
{
    protected ReferenceType container;

    /** Used for deserializing types. */
    protected FieldInstance_c() { }

    public FieldInstance_c(TypeSystem ts, Position pos,
			   ReferenceType container,
	                   Flags flags, Type type, String name) {
        super(ts, pos, flags, type, name);
	this.container = container;
    }

    public ReferenceType container() {
        return container;
    }

    public FieldInstance constantValue(Object constantValue) {
	if (! (constantValue instanceof Boolean) &&
	    ! (constantValue instanceof Number) &&
	    ! (constantValue instanceof Character) &&
	    ! (constantValue instanceof String)) {

	    throw new InternalCompilerError(
		"Can only set constant value to a primitive or String.");
	}

        FieldInstance_c n = (FieldInstance_c) copy();
	n.constantValue = constantValue;
        return n;
    }

    public FieldInstance container(ReferenceType container) {
        FieldInstance_c n = (FieldInstance_c) copy();
	n.container = container;
        return n;
    }

    public FieldInstance flags(Flags flags) {
        FieldInstance_c n = (FieldInstance_c) copy();
	n.flags = flags;
	return n;
    }

    public FieldInstance name(String name) {
        FieldInstance_c n = (FieldInstance_c) copy();
	n.name = name;
	return n;
    }

    public FieldInstance type(Type type) {
        FieldInstance_c n = (FieldInstance_c) copy();
	n.type = type;
	return n;
    }

    public boolean equals(Object o) {
        if (o instanceof FieldInstance) {
	    FieldInstance i = (FieldInstance) o;
	    return super.equals(i) && container.isSame(i.container());
	}

	return false;
    }

    public String toString() {
        return "field " + flags.translate() + type + " " + name +
	    (constantValue != null ? (" = " + constantValue) : "");
    }

    public TypeObject restore_() throws SemanticException {
	ReferenceType c = (ReferenceType) container.restore();
	Type t = (Type) type.restore();

	FieldInstance fi = this;

	if (c != fi.container()) {
	    fi = container(c);
	}

	if (t != fi.type()) {
	    fi = type(t);
	}

	return fi;
    }

    public boolean isCanonical() {
	return container.isCanonical()
	    && type.isCanonical();
    }
}
