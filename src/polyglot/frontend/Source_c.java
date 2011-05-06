package polyglot.frontend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Date;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class Source_c extends ForwardingJavaFileObject<JavaFileObject>
	implements FileSource {
	@Deprecated
	protected Date last_modified = null;
	protected boolean user_specified;
	protected JavaFileObject file;

	protected Source_c(JavaFileObject f, boolean userSpecified) {
		super(f);
		this.user_specified = userSpecified;
	}
	
	@Deprecated
	public Date lastModified() {
		if(this.last_modified == null) {
			this.last_modified = new Date(getLastModified());
		}
		return last_modified;
	}

	@Deprecated
	public String name() {
		return getName();
	}

	@Deprecated
	public Reader open() throws IOException {
		return openReader(false);
	}

	@Deprecated
	public String path() {
		return toUri().getPath();
	}

	public void setUserSpecified(boolean userSpecified) {
		this.user_specified = userSpecified;
	}

	public boolean userSpecified() {
		return user_specified;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream());
	}
		
	@Override
	public String toString() {
		return getName();
	}
	
	public boolean equals(Object o) {
		if(o instanceof FileObject) {

			FileObject fo = (FileObject) o;
			return toUri().equals(fo.toUri());
		}
		else {
			return false;
		}
	}
	
	public int hashCode() {
		return toUri().hashCode();
	}
}
