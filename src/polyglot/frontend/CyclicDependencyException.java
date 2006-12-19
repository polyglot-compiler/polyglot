/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;


/**
 * Thrown during when the compiler tries to run a pass that is
 * already running.
 */
public class CyclicDependencyException extends Exception {
    public CyclicDependencyException() {
        super();
    }

    public CyclicDependencyException(String m) {
        super(m);
    }
}
