package jltools.frontend;

import java.io.*;
import java.util.*;

/** A <code>SourceLoader</code> is responsible for loading source files. */
public class SourceLoader
{
    String sourceExtension;
    Collection sourcePath;

    public SourceLoader(Collection sourcePath, String sourceExtension) {
	this.sourcePath = sourcePath;
	this.sourceExtension = sourceExtension;
    }

    public Source fileSource(String fileName) throws IOException {
	File sourceFile = new File(fileName);

	if (! sourceFile.exists()) {
	    throw new FileNotFoundException(fileName);
	}

	jltools.frontend.Compiler.report(1, "Loading class from " + sourceFile);

	return new Source(fileName, sourceExtension);
    }

    public Source classSource(String className) throws IOException {
	/* Search the source path. */
	String fileName = className.replace('.', File.separatorChar) 
			   + "." + sourceExtension;

	for (Iterator i = sourcePath.iterator(); i.hasNext(); ) {
	    File directory = (File) i.next();

	    File sourceFile = new File(directory, fileName);
	    
	    if (sourceFile.exists()) {
		jltools.frontend.Compiler.report(1,
		    "Loading " + className + " from " + sourceFile);

		return new Source(sourceFile.getPath(), sourceExtension);
	    }
	}

	throw new FileNotFoundException(fileName);
    }
}
