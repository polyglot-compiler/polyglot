/*
 * UnavailableTypeException.java
 * 
 * Author: nystrom
 * Creation date: Dec 27, 2004
 */
package polyglot.types;

import polyglot.frontend.Job;
import polyglot.util.Position;

/**
 * Comment for <code>UnavailableTypeException</code>
 *
 * @author nystrom
 */
public class UnavailableTypeException extends RuntimeException {
    Job job;
    Position position;
    
    /**
     * @param job
     * @param className
     */
    public UnavailableTypeException(Job job, String className) {
        this(job, className, null);
    }

    /**
     * @param job
     * @param className
     * @param position
     */
    public UnavailableTypeException(Job job, String className, Position position) {
        super(className);
        this.job = job;
        this.position = position;
    }
    
    public UnavailableTypeException(ParsedClassType ct) {
        this(ct.job(), ct.fullName(), ct.position());
    }
    
    public Job job() {
        return job;
    }
    
    public Position position() {
        return position;
    }
}
