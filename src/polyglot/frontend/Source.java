package polyglot.frontend;

import java.io.*;
import java.util.*;
import polyglot.util.*;

/** A <code>Source</code> represents a source file. */
public class Source
{
    String name;
    String path;
    Date lastModified;

    protected Source(String name) {
	this.name = name;
    }

    public Source(String name, String path, Date lastModified) {
	this.name = name;
        this.path = path;
	this.lastModified = lastModified;
    }

    public boolean equals(Object o) {
	if (o instanceof Source) {
	    Source s = (Source) o;
	    return name.equals(s.name) && path.equals(s.path);
	}

	return false;
    }

    public int hashCode() {
	return path.hashCode() + name.hashCode();
    }

    /** The name of the source file. */
    public String name() {
	return name;
    }

    /** The name of the source file. */
    public String path() {
	return path;
    }

    /** Return the date the source file was last modified. */
    public Date lastModified() {
	return lastModified;
    }

    public String toString() {
	return name;
    }
}
