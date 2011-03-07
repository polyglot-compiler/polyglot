/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

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
        
    public FileSource(File file, boolean userSpecified) throws IOException {
        super(file.getName(), userSpecified);
        this.file = file;
    
        if (! file.exists()) {
            throw new FileNotFoundException(file.getName());
        }

        path = file.getPath();
        lastModified = new Date(file.lastModified());
    }
    
    public FileSource(String path, String name, Date lastModified, boolean userSpecified) throws IOException {
        super(name, userSpecified);
        this.file = null;
        this.path = path;
        this.lastModified = lastModified;
    }

    public boolean equals(Object o) {
	if (o instanceof FileSource) {
	    FileSource s = (FileSource) o;
	    return (file != null && file.equals(s.file))
	    	|| (file == null && s.file == null && super.equals(s));
	}

	return false;
    }

    public int hashCode() {
    	return path.hashCode();
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
        a file source. By default, it is US-ASCII
	but it may be overridden. */
    protected Reader createReader(InputStream str) {
      try {
          return new InputStreamReader(str, "US-ASCII");
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
	return path;
    }
}
