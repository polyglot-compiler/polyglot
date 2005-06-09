package polyglot.ext.jl.types;

import polyglot.frontend.*;
import polyglot.frontend.Scheduler;
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

    public ReferenceType container() {
        return container;
    }
    
    public boolean isConstant() {
        if (! constantValueSet) {
            Scheduler scheduler = typeSystem().extensionInfo().scheduler();
            try {
                Goal g = scheduler.FieldConstantsChecked(this);
                if (container instanceof ParsedTypeObject) {
                    Job job = ((ParsedTypeObject) container).job();
                    scheduler.addPrerequisiteDependency(g, scheduler.TypeChecked(job));
                }
                scheduler.addPrerequisiteDependency(scheduler.currentGoal(), g);
            }
            catch (CyclicDependencyException e) {
                setNotConstant();
            }
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
