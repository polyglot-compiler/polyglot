/*
 * UnavailableTypeException.java
 * 
 * Author: nystrom
 * Creation date: Dec 27, 2004
 */
package polyglot.types;

import polyglot.frontend.Job;
import polyglot.frontend.SchedulerException;
import polyglot.util.Position;

/**
 * An <code>UnavailableTypeException</code> is an exception thrown when a type
 * object is not in a required state to continue a pass.
 * 
 * @author nystrom
 */
public class UnavailableTypeException extends SchedulerException {
    Job job;
    Position position;
    
    /**
     * @param job
     * @param fullName
     */
    public UnavailableTypeException(Job job, String fullName) {
        this(job, fullName, null);
    }

    /**
     * @param job
     * @param fullName
     * @param position
     */
    public UnavailableTypeException(Job job, String fullName, Position position) {
        super(fullName);
        this.job = job;
        this.position = position;
    }
    
    public UnavailableTypeException(ParsedTypeObject ct) {
        this(ct.job(), ct.fullName(), ct.position());
    }
    
    public Job job() {
        return job;
    }
    
    public Position position() {
        return position;
    }
}
