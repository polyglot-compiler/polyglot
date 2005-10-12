package polyglot.ext.jl.types;

import java.util.ArrayList;
import java.util.List;

import polyglot.frontend.*;
import polyglot.frontend.goals.FieldConstantsChecked;
import polyglot.frontend.goals.Goal;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>FieldInstance</code> contains type information for a field.
 */
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
    
    public FieldInstance orig() {
        return (FieldInstance) declaration();
    }
    
    public FieldInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            FieldInstance n = (FieldInstance) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    public FieldInstance name(String name) {
        if ((name != null && !name.equals(this.name)) ||
            (name == null && this.name != null)) {
            FieldInstance n = (FieldInstance) copy();
            n.setName(name);
            return n;
        }
        return this;
    }

    public FieldInstance type(Type type) {
        if (this.type != type) {
            FieldInstance n = (FieldInstance) copy();
            n.setType(type);
            return n;
        }
        return this;
    }

    public FieldInstance container(ReferenceType container) {
        if (this.container != container) {
            FieldInstance_c n = (FieldInstance_c) copy();
            n.setContainer(container);
            return n;
        }
        return this;
    }
    
    public FieldInstance constantValue(Object constantValue) {
        if (!constantValueSet
                || (constantValue != null && !constantValue.equals(this.constantValue))
                || (constantValue == null && this.constantValue != null)) {
            FieldInstance copy = (FieldInstance) this.copy();
            copy.setConstantValue(constantValue);
            return copy;
        }
        return this;
    }
    
    public FieldInstance notConstant() {
        if (! this.constantValueSet || this.isConstant) {
            FieldInstance copy = (FieldInstance) this.copy();
            copy.setNotConstant();
            return copy;
        }
        return this;
    }


    public ReferenceType container() {
        return container;
    }
    
    public boolean isConstant() {
        if (this != orig()) {
            return orig().isConstant();
        }
    
        if (! constantValueSet) {
            if (! flags.isFinal()) {
                setNotConstant();
                return isConstant;
            }

            Scheduler scheduler = typeSystem().extensionInfo().scheduler();
            Goal g = scheduler.FieldConstantsChecked(this);
            throw new MissingDependencyException(g);
        }
            
        return isConstant;
    }

    /**
     * @param container The container to set.
     */
    public void setContainer(ReferenceType container) {
        this.container = container;
    }
     
    public boolean equalsImpl(TypeObject o) {
        if (o instanceof FieldInstance) {
	    FieldInstance i = (FieldInstance) o;
	    return super.equalsImpl(i) && ts.equals(container, i.container());
	}

	return false;
    }

    public String toString() {
        Object v = constantValue;
        if (v instanceof String) {
          String s = (String) v;

          if (s.length() > 8) {
            s = s.substring(0, 8) + "...";
          }

          v = "\"" + s + "\"";
        }

        return "field " + flags.translate() + type + " " + name +
	    (v != null ? (" = " + v) : "");
    }

    public boolean isCanonical() {
	return container.isCanonical() && type.isCanonical();
    }
}
