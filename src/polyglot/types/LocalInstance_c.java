package polyglot.types;

import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>LocalInstance</code> contains type information for a local variable.
 */
public class LocalInstance_c extends VarInstance_c implements LocalInstance
{
    /** Used for deserializing types. */
    protected LocalInstance_c() { }

    public LocalInstance_c(TypeSystem ts, Position pos,
	  		   Flags flags, Type type, String name) {
        super(ts, pos, flags, type, name);
    }
    
    public LocalInstance orig() {
        return (LocalInstance) declaration();
    }

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof LocalInstance) {
            LocalInstance i = (LocalInstance) o;
            return super.equalsImpl(i);
        }

        return false;
    }

    public String toString() {
        return "local " + flags.translate() + type + " " + name +
	    (constantValue != null ? (" = " + constantValue) : "");
    }

    public boolean isCanonical() {
	return type.isCanonical();
    }
    
    public LocalInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            LocalInstance n = (LocalInstance) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    public LocalInstance name(String name) {
        if ((name != null && !name.equals(this.name)) ||
            (name == null && this.name != null)) {
            LocalInstance n = (LocalInstance) copy();
            n.setName(name);
            return n;
        }
        return this;
    }

    public LocalInstance type(Type type) {
        if (this.type != type) {
            LocalInstance n = (LocalInstance) copy();
            n.setType(type);
            return n;
        }
        return this;
    }

    public LocalInstance constantValue(Object constantValue) {
        if ( ! constantValueSet ||
                (constantValue != null && !constantValue.equals(this.constantValue))
                || (constantValue == null && this.constantValue != null)) {
            LocalInstance copy = (LocalInstance) this.copy();
            copy.setConstantValue(constantValue);
            return copy;
        }
        return this;
    }
    
    public LocalInstance notConstant() {
        if (! this.constantValueSet || this.isConstant) {
            LocalInstance copy = (LocalInstance) this.copy();
            copy.setNotConstant();
            return copy;
        }
        return this;
    }

}
