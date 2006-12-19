/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>SilentErrorQueue</code> records but does not output error messages.
 */
public class SilentErrorQueue extends AbstractErrorQueue
{
    private List errors;

    public SilentErrorQueue(int limit, String name) {
        super(limit, name);
        this.errors = new ArrayList(limit);
    }

    public void displayError(ErrorInfo e) {
        errors.add(e);
    }
    
    public List getErrors() {
        return errors;
    }
}
