package jltools.frontend;

import java.io.*;
import java.util.*;
import jltools.util.*;

/** A <code>Source</code> represents a source file. */
public class Source
{
    String name;
    File file;
    FileReader reader;
    Date lastModified;

    public Source(String name) throws IOException {
	this.name = name;
	this.file = new File(name);

	if (! file.exists()) {
	    throw new FileNotFoundException(name);
	}

	lastModified = new Date(file.lastModified());
    }

    public boolean equals(Object o) {
	if (o instanceof Source) {
	    Source s = (Source) o;
	    return file.equals(s.file);
	}

	return false;
    }

    public int hashCode() {
	return file.getAbsolutePath().hashCode();
    }

    /** The name of the source file. */
    public String name() {
	return name;
    }

    /** Gets the path of the source file. */
    public String path() {
	return file.getPath();
    }

    /** Open the source file. */
    public Reader open() throws IOException {
	if (reader == null) {
	    reader = new FileReader(file);
	}

	return reader;
    }

    /** Close the source file. */
    public void close() throws IOException {
	if (reader != null) {
	    reader.close();
	    reader = null;
	}
    }

    /** Return the date the source file was last modified. */
    public Date lastModified() {
	return lastModified;
    }

    public String toString() {
	return file.getName();
    }
}
