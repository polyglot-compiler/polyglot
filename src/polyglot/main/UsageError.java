/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.main;

/** An exception used to indicate a command-line usage error. */
public class UsageError extends Exception {
    protected final int exitCode;
    public UsageError(String s) { this(s,1); }
    public UsageError(String s, int exitCode) { 
            super(s); 
            this.exitCode = exitCode; 
    }
    public int exitCode() { return exitCode; }
}
