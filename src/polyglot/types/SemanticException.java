package polyglot.types;

import polyglot.util.Position;
import polyglot.main.Report;
import java.util.*;

/**
 * Thrown during any number of phases of the compiler during which a semantic
 * error may be detected.
 */
public class SemanticException extends Exception {
    protected Position position;
    
    public SemanticException() {
        super();
    }

    public SemanticException(Throwable cause) {
        super(cause);
    }

    public SemanticException(Position position) {
	super();
	this.position = position;
    }

    public SemanticException(String m) {
        super(m);
    }

    public SemanticException(String m, Throwable cause) {
        super(m, cause);
    }

    public SemanticException(String m, Position position) {
	super(m);
	this.position = position;
    }

    public Position position() {
	return position;
    }
    
    static boolean init = false;
    static boolean fill = true;
    
    public synchronized Throwable fillInStackTrace() {
        if (! fill) {
            // fast path: init==true, fill==false
            return this;
        }
        if (! init) {
            fill = Report.should_report("trace", 1);
            init = true;
        }
        return super.fillInStackTrace();
    }
}
