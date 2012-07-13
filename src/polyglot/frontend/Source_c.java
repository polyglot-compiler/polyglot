package polyglot.frontend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Date;

import javax.tools.FileObject;
import javax.tools.ForwardingFileObject;
import javax.tools.JavaFileManager;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class Source_c extends ForwardingFileObject<FileObject> implements
		FileSource {
	protected boolean user_specified;

	protected Source_c(FileObject f, boolean userSpecified) {
		super(f);
		this.user_specified = userSpecified;
	}

	public void setUserSpecified(boolean userSpecified) {
		this.user_specified = userSpecified;
	}

	public boolean userSpecified() {
		return user_specified;
	}
	
	public String name() {
		return getName();
	}
	
	public String path() {
		return toUri().getPath();
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream());
	}

	@Override
	public String toString() {
		return toUri().getPath();
	}

	public boolean equals(Object o) {
		if (o instanceof FileObject) {

			FileObject fo = (FileObject) o;
			return toUri().equals(fo.toUri());
		} else {
			return false;
		}
	}

	public int hashCode() {
		return toUri().hashCode();
	}
}
