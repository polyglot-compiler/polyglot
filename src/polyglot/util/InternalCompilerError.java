
package jltools.util;

public class InternalCompilerError extends RuntimeException
{
    Position pos;

    public InternalCompilerError(String msg) {
        this(msg, null);
    }

    public InternalCompilerError(Position position, String msg) {
	super(msg); 
	pos = position;
    }

    public InternalCompilerError(String msg, Position position) {
	super(msg); 
	pos = position;
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
