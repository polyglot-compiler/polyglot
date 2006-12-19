/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

/*
 * SchedulerException.java
 * 
 * Author: nystrom
 * Creation date: Aug 6, 2004
 */
package polyglot.frontend;

/**
 * A <code>SchedulerException</code> is a runtime exception that may abort
 * a running pass and return the scheduler to the scheduling loop.
 *
 * @author nystrom
 */
public class SchedulerException extends RuntimeException {
    public SchedulerException() {
    }
    
    /**
     * @param message An error message
     * @param cause The cause of this scheduler exception
     */
    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message An error message
     */
    public SchedulerException(String message) {
        super(message);
    }

    /**
     * @param cause The cause of this scheduler exception
     */
    public SchedulerException(Throwable cause) {
        super(cause);
    }
}
