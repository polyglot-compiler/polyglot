package polyglot.types;

import polyglot.frontend.*;
import polyglot.frontend.goals.Goal;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A place holder type when serializing the Polylgot type information. 
 * When serializing the type information for some class <code>C</code>, 
 * Placeholders are used to prevent serializing the class type information
 * for classes that <code>C</code> depends on.  
 */
public class PlaceHolder_c implements NamedPlaceHolder
{
    /**
     * The name of the place holder.
     */
    protected String name;

    /** Used for deserializing types. */
    protected PlaceHolder_c() { }
    
    /** Creates a place holder type for the type. */
    public PlaceHolder_c(Named t) {
        this(t.fullName());
    }
    
    public PlaceHolder_c(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object o) {
        return o == this || (o instanceof PlaceHolder_c && name.equals(((PlaceHolder_c) o).name));
    }
    
    /**
     * Resolve the place holder into a TypeObject.  This method
     * should <strong>never</strong> throw a SchedulerException.
     * Instead, it should return null if the object cannot be resolved 
     * until after another pass runs.  The method is responsible for setting
     * up the appropriate dependencies to reattempt the current goal.
     */
    public TypeObject resolve(TypeSystem ts) throws CannotResolvePlaceHolderException {
        return resolveUnsafe(ts);
    }
    
    public TypeObject resolveUnsafe(TypeSystem ts) throws CannotResolvePlaceHolderException {
        Scheduler scheduler = ts.extensionInfo().scheduler();
        Goal g = scheduler.TypeExists(name);
        
        try {
            return ts.systemResolver().find(name);
        }
        catch (MissingDependencyException e) {
            // The type is in a source file that hasn't been parsed yet.
            g = e.goal();
            scheduler.currentGoal().setUnreachableThisRun();
            scheduler.addDependencyAndEnqueue(scheduler.currentGoal(), g, false);
            throw new CannotResolvePlaceHolderException(e);
        }
        catch (SchedulerException e) {
            // Some other scheduler error occurred.
            scheduler.currentGoal().setUnreachableThisRun();
            scheduler.addDependencyAndEnqueue(scheduler.currentGoal(), g, false);
            throw new CannotResolvePlaceHolderException(e);
        }
        catch (SemanticException e) {
            // The type could not be found.
            scheduler.currentGoal().setUnreachableThisRun();
            scheduler.addDependencyAndEnqueue(scheduler.currentGoal(), g, false);
            throw new CannotResolvePlaceHolderException(e);
        }
    }
    
    /** A potentially safer alternative implementation of resolve. */
    public TypeObject resolveSafe(TypeSystem ts) throws CannotResolvePlaceHolderException {
        Named n = ts.systemResolver().check(name);

        if (n != null) {
            return n;
        }

        // The class has not been loaded yet.  Set up a dependency
        // to load the class (coreq, in case this pass is the one to load it).
        Scheduler scheduler = ts.extensionInfo().scheduler();
        scheduler.currentGoal().setUnreachableThisRun();
        scheduler.addDependencyAndEnqueue(scheduler.currentGoal(),
                                          scheduler.TypeExists(name),
                                          false);

        throw new CannotResolvePlaceHolderException("Could not resolve " + name);
    }
    
    public String toString() {
	return "PlaceHolder(" + name + ")";
    }
}
