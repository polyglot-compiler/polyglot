package polyglot.frontend;

import java.io.*;
import java.util.*;

import polyglot.util.InternalCompilerError;

/** A <code>Source</code> represents a source file. */
public class FileSource extends Source
{
    protected final File file;
    protected FileReader reader;

    public FileSource(String name) throws IOException {
        this(name, false);
    }
    
    public FileSource(String name, boolean userSpecified) throws IOException {
        super(name, userSpecified);
        this.file = new File(name);
        if (! file.exists()) {
            throw new FileNotFoundException(name);
        }

        path = file.getPath();
        lastModified = new Date(file.lastModified());
    }

    public FileSource(File file) {
        this(file, false);
    }
    
    public FileSource(File file, boolean userSpecified) {
        super(file.getPath(), userSpecified);
        this.file = file;
    
        if (! file.exists()) {
            throw new InternalCompilerError("FileSource given a " + 
                        "non-existent file");
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
	return file.getAbsolutePath().hashCode();
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

    public String toString() {
	return file.getName();
    }
}
