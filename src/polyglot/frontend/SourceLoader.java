package polyglot.frontend;

import java.io.*;
import java.util.*;
import polyglot.main.Report;

/** A <code>SourceLoader</code> is responsible for loading source files. */
public class SourceLoader
{
    protected ExtensionInfo sourceExt;
    protected Collection sourcePath;
    
    /**
     * This is a map from Files (of directories) to Set[String]s, which
     * records the first level of contents of the directory. This cache
     * is used to avoid a number of File.exists() calls.
     */
    protected Map directoryContentsCache;

    public SourceLoader(ExtensionInfo sourceExt, Collection sourcePath) {
	this.sourcePath = sourcePath;
	this.sourceExt = sourceExt;
        this.directoryContentsCache = new HashMap();
    }

    /** Load a source from a specific file. */
    public FileSource fileSource(String fileName) throws IOException {
	File sourceFile = new File(fileName);

	if (! sourceFile.exists()) {
	    throw new FileNotFoundException(fileName);
	}

        if (! fileName.endsWith("." + sourceExt.fileExtension())) {
            throw new IOException("Source \"" + fileName +
                                  "\" does not have the extension \"." +
                                  sourceExt.fileExtension() + "\".");
        }

	if (Report.should_report(Report.frontend, 2))
	    Report.report(2, "Loading class from " + sourceFile);

	return new FileSource(fileName);
    }

    /**
     * The current user directory. We make it static so we don't need to
     * keep on making copies of it. 
     */
    protected static File current_dir = null;
    
    /** Load the source file for the given class name using the source path. */
    public FileSource classSource(String className) {
	/* Search the source path. */
        String fileName = className.replace('.', File.separatorChar) +
                                        "." + sourceExt.fileExtension();

	if (current_dir == null) {
            current_dir = new File(System.getProperty("user.dir"));
        }

	for (Iterator i = sourcePath.iterator(); i.hasNext(); ) {
	    File directory = (File) i.next();
            Set dirContents = (Set)directoryContentsCache.get(directory);
            if (dirContents == null) {
                dirContents = new HashSet();
                directoryContentsCache.put(directory, dirContents);
                if (directory.exists()) {
                    String[] contents = directory.list();
                    for (int j = 0; j < contents.length; j++) {
                        dirContents.add(contents[j]);
                    }
                }                
            }

            // check if the source file exists in the directory
            int index = fileName.indexOf(File.separatorChar);
            if (index < 0) index = fileName.length(); 
            String firstPart = fileName.substring(0, index);

            if (dirContents.contains(firstPart)) {
                // the directory contains at least the first part of the
                // file path. We will check if this file exists.
                File sourceFile;
                
                if (directory != null && directory.equals(current_dir)) {
                    sourceFile = new File(fileName);
                }
                else {
                    sourceFile = new File(directory, fileName);
                }
                
                if (sourceFile.exists()) {
                    if (Report.should_report(Report.frontend, 2))
                        Report.report(2, "Loading " + className + " from " + sourceFile);
                
                    return new FileSource(sourceFile);
                }
            }
	}

	return null;
    }
}
