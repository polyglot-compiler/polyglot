package jltools.util;

import java.io.*;

/**
 * A <code>ErrorQueue</code> handles outputing error messages.
 */
public class ErrorQueue
{
    private static final int ERROR_COUNT_LIMIT = 100;
    
    private PrintStream err;

    private boolean hasErrors;
    private int errorCount;
    private boolean flushed;
    
    public ErrorQueue(PrintStream err) {
	this.err = err;
	this.errorCount = 0;
	this.hasErrors = false;
	this.flushed = true;
    }

    public void enqueue( int type, String message) {
	enqueue( type, message, null);
    }

    public void enqueue( int type, String message, Position position) {
	enqueue( new ErrorInfo( type, message, position));
    }

    public void enqueue(ErrorInfo e) {
	if (e.getErrorKind() != ErrorInfo.WARNING) {
	    hasErrors = true;
	    errorCount++;
	}

	flushed = false;

	String message = e.getErrorKind() != ErrorInfo.WARNING
		       ? e.getMessage()
		       : e.getErrorString() + " -- " + e.getMessage();

	Position position = e.getPosition();

	String prefix = position != null ? (position.toString() + ": ") : "";

	err.println(prefix + message);

	if (errorCount >= ERROR_COUNT_LIMIT) {
	    prefix = position != null ? (position.file() + ": ") : "";
	    err.println(prefix + "Too many errors.  Aborting compilation.");
	    flush();
	    throw new ErrorLimitError();
	}
    }
    
    public void flush() {
	if (! flushed) {
	    err.println(errorCount + " error" + (errorCount > 1 ? "s." : "."));
	    flushed = true;
	}
    }

    public boolean hasErrors() {
      return hasErrors;
    }
}
