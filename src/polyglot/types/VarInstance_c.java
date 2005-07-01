package polyglot.ext.jl.types;

import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.FieldConstantsChecked;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>VarInstance</code> contains type information for a variable.  It may
 * be either a local or a field.
 */
public abstract class VarInstance_c extends TypeObject_c implements VarInstance
{
    protected Flags flags;
    protected Type type;
    protected String name;
    protected Object constantValue;
    protected boolean isConstant;
    protected boolean constantValueSet;

    /** Used for deserializing types. */
    protected VarInstance_c() { }

    public VarInstance_c(TypeSystem ts, Position pos,
	                 Flags flags, Type type, String name) {
        super(ts, pos);
	this.flags = flags;
	this.type = type;
	this.name = name;
    }

    public boolean constantValueSet() {
        return constantValueSet;
    }
    
    public boolean isConstant() {
        if (! constantValueSet) {
            if (! flags.isFinal()) {
                setNotConstant();
                return isConstant;
            }
//            Scheduler scheduler = typeSystem().extensionInfo().scheduler();
//            scheduler.addConcurrentDependency(scheduler.currentGoal(), new ConstantsChecked(this));
        }
        return isConstant;
    }

    public Object constantValue() {
        if (isConstant()) {
            return constantValue;
        }
        return null;
    }

    public Flags flags() {
        return flags;
    }
    
    public Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public int hashCode() {
        return flags.hashCode() + name.hashCode();
    }

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof VarInstance) {
	    VarInstance i = (VarInstance) o;
	    return flags.equals(i.flags())
	        && ts.equals(type, i.type())
		&& name.equals(i.name());
	}

	return false;
    }

    public boolean isCanonical() {
	return true;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }
    
    /** Destructive update of constant value. */
    public void setConstantValue(Object constantValue) {
        if (! (constantValue == null) &&
                ! (constantValue instanceof Boolean) &&
                ! (constantValue instanceof Number) &&
                ! (constantValue instanceof Character) &&
                ! (constantValue instanceof String)) {
            
            throw new InternalCompilerError(
            "Can only set constant value to a primitive or String.");
        }

        this.constantValue = constantValue;
        this.isConstant = true;
        this.constantValueSet = true;
    }
    
    public void setNotConstant() {
        this.constantValue = null;
        this.isConstant = false;
        this.constantValueSet = true;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}
