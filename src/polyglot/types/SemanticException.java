package jltools.types;

import jltools.util.Position;

/**
 * Thrown during any number of phases of the compiler during which a semantic
 * error may be detected.
 */
public class SemanticException extends Exception
{
    protected Position position;
    
    public SemanticException() {
	super();
    }

    public SemanticException(Position position) {
	super();
	this.position = position;
    }

    public SemanticException(String m) {
	super(m);
    }

    public SemanticException(String m, Position position) {
	super(m);
	this.position = position;
    }

    public Position position() {
	return position;
    }
}
