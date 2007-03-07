/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.frontend;

import java.io.*;
import java.util.*;

import polyglot.util.InternalCompilerError;

/** A <code>Source</code> represents a source file. */
public class FileSource extends Source
{
    protected final File file;
    protected Reader reader;

    public FileSource(File file) throws IOException {
        this(file, false);
    }
    
    public FileSource(String name) {
    	super(name, name, null, false);
    	file = new File(name); // This file doesn't really need to exist
    }

    
    public FileSource(File file, boolean userSpecified) throws IOException {
        super(file.getName(), userSpecified);
        this.file = file;
    
        if (! file.exists()) {
            throw new FileNotFoundException(file.getName());
        }

        path = file.getPath();
        lastModified = new Date(file.lastModified());
    }

    public boolean equals(Object o) {
	if (o instanceof FileSource) {
	    FileSource s = (FileSource) o;
	    return file.equals(s.file);
	}

	return false;
    }

    public int hashCode() {
	return file.getPath().hashCode();
    }

    /** Open the source file. */
    public Reader open() throws IOException {
	if (reader == null) {
	    FileInputStream str = new FileInputStream(file);
	    reader = createReader(str);
	}

	return reader;
    }

    /** This method defines the character encoding used by
        a file source. By default, it is ASCII with Unicode escapes,
	but it may be overridden. */
    protected Reader createReader(InputStream str) {
      try {
	return new polyglot.lex.EscapedUnicodeReader(
	             new InputStreamReader(str, "US-ASCII"));
      } catch (UnsupportedEncodingException e) { return null; }
    }

    /** Close the source file. */
    public void close() throws IOException {
	if (reader != null) {
	    reader.close();
	    reader = null;
	}
    }

    public String toString() {
	return file.getPath();
    }
}
