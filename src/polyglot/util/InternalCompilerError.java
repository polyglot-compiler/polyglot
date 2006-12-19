/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.util;

/** Exception thrown when the compiler is confused. */
public class InternalCompilerError extends RuntimeException
{
    protected Position pos;

    public InternalCompilerError(String msg) {
        this(msg, (Position)null);
    }

    public InternalCompilerError(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public InternalCompilerError(String msg, Throwable cause) {
        this(msg, null, cause);
    }

    public InternalCompilerError(Position position, String msg) {
	this(msg, position); 
    }

    public InternalCompilerError(String msg, Position position) {
        super(msg); 
        pos = position;
    }
    public InternalCompilerError(String msg, Position position, Throwable cause) {
        super(msg, cause); 
        pos = position;
    }

    public void setPosition(Position pos) {
	this.pos = pos;
    }

    public Position position() {
	return pos;
    }

    public String message() {
	return super.getMessage();
    }

    public String getMessage() {
	return pos == null ? message() : pos + ": " + message();
    }
}
