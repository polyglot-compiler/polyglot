package jltools.frontend;

import java.io.*;
import java.util.*;
import jltools.frontend.Compiler;

/** A <code>SourceLoader</code> is responsible for loading source files. */
public class SourceLoader
{
    String sourceExtension;
    Collection sourcePath;

    public SourceLoader(Collection sourcePath, String sourceExtension) {
	this.sourcePath = sourcePath;
	this.sourceExtension = sourceExtension;
    }

    /** Load a source from a specific file. */
    public Source fileSource(String fileName) throws IOException {
	File sourceFile = new File(fileName);

	if (! sourceFile.exists()) {
	    throw new FileNotFoundException(fileName);
	}

	Compiler.report(2, "Loading class from " + sourceFile);

	return new Source(fileName, sourceExtension);
    }

    /** Load the source file for the given class name using the source path. */
    public Source classSource(String className) throws IOException {
	/* Search the source path. */
	String fileName = className.replace('.', File.separatorChar) 
			   + "." + sourceExtension;

	File current_dir = new File(System.getProperty("user.dir"));

	for (Iterator i = sourcePath.iterator(); i.hasNext(); ) {
	    File directory = (File) i.next();

	    File sourceFile;

	    if (directory != null && directory.equals(current_dir)) {
		sourceFile = new File(fileName);
	    }
	    else {
	        sourceFile = new File(directory, fileName);
	    }
	    
	    if (sourceFile.exists()) {
		Compiler.report(2,
		    "Loading " + className + " from " + sourceFile);

		return new Source(sourceFile.getPath(), sourceExtension);
	    }
	}

	throw new FileNotFoundException(fileName);
    }
}
