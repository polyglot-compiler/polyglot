package jltools.util;

/**
 * This class represents a posiiton within a file.
 **/
public class Position {
    String file;
    int line;
    int column;

    public static final int UNKNOWN = -1;

    public Position(String file) {
	this(file, UNKNOWN, UNKNOWN);
    }

    public Position(String file, int line) {
	this(file, line, UNKNOWN);
    }

    public Position(String file, int line, int column) {
	this.file = file;
	this.line = line;
	this.column = column;
    }

    public int line() {
	return line;
    }

    public int column() {
	return column;
    }

    public String file() {
	return file;
    }

    public String toString() {
	String s = file;

	if (line != UNKNOWN) {
	    s += ":" + line;

	    if (column != UNKNOWN) {
		s += "," + column;
	    }
	}

	return s;
    }
}
