package polyglot.frontend;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;

public interface Source extends FileObject {
	
    void setUserSpecified(boolean userSpecified);  
    boolean userSpecified();

	/**
	 * Open a reader for this file. This method is primarily for backward
	 * compatibility. For most uses, JavaFileObject.openReader(boolean) or
	 * JavaFileObject.openInputStream() is more appropriate.
	 * @throws IOException 
	 */
    @Deprecated
    Reader open() throws IOException;
	/**
	 * The path of the source file. This method is primarily for backward
	 * compatibility. For most uses, JavaFileObject.toUri() is more appropriate.
	 */
    @Deprecated
    String path();
	/**
	 * The name of the source file. This method is primarily for backward
	 * compatibility. For most uses, JavaFileObject.getName() is more appropriate.
	 */
    @Deprecated
    String name();

	/**
	 * The last modification date. This method is for backward compatibility,
	 * and should be identical to JavaFileObject.getLastModified().
	 */
    @Deprecated
	Date lastModified();
}
