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

    public Source(String name, String sourceExtension) throws IOException {
	this.name = name;
	this.file = new File(name);

	if (! file.exists()) {
	    throw new FileNotFoundException(name);
	}

	if (! name.endsWith("." + sourceExtension)) {
	    throw new IOException(
		"Source \"" + name + "\" does not have the extension \"." +
		sourceExtension + "\".");
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

    public String name() {
	return name;
    }

    public Reader open() throws IOException {
	if (reader == null) {
	    reader = new FileReader(file);
	}

	return reader;
    }

    public void close() throws IOException {
	if (reader != null) {
	    reader.close();
	    reader = null;
	}
    }

    public Date lastModified() {
	return lastModified;
    }

    public String toString() {
	return file.getName();
    }
}
