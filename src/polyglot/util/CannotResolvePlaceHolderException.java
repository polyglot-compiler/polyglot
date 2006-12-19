/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.util;

/**
 * This exception is thrown when a PlaceHolder cannot be resolved
 * during deserialization.  The thrower should set up a goal
 * to resolve the place holder in another pass.
 * When caught, deserialization should fail.
 */
public class CannotResolvePlaceHolderException extends Exception {
    public CannotResolvePlaceHolderException(Throwable cause) {
        super(cause);
    }

    public CannotResolvePlaceHolderException(String m) {
        super(m);
    }

    public CannotResolvePlaceHolderException(String m, Throwable cause) {
        super(m, cause);
    }
}
