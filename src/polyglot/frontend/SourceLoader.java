package polyglot.frontend;

import java.io.IOException;

public interface SourceLoader {

	/** Load a source from a specific file. This method is deprecated in favor of  */
	FileSource fileSource(String fileName) throws IOException;
	
	FileSource fileSource(String fileName, boolean userSpecified)
			throws IOException;

	/** Load the source file for the given (possibly nested) class name
	    using the source path. */
	FileSource classSource(String className);

}