package polyglot.util;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.Iterator;
import java.util.Set;

public class CustomExtFileManager implements JavaFileManager {
	
	private CustomJavaSourceObject sourceObject;

	public void close() throws IOException {
		sourceObject.openWriter().close();
	}

	public void flush() throws IOException {
		sourceObject.openWriter().flush();
	}

	public ClassLoader getClassLoader(Location location) {
		throw new UnsupportedOperationException();
	}

	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		throw new UnsupportedOperationException();
	}

	public FileObject getFileForOutput(Location location, String packageName,
			String relativeName, FileObject sibling) throws IOException {
		throw new UnsupportedOperationException();
	}

	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		throw new UnsupportedOperationException();
	}

	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		sourceObject = new CustomJavaSourceObject(className);
		return sourceObject;
	}

	public boolean handleOption(String current, Iterator<String> remaining) {
		return false;
	}

	public boolean hasLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public String inferBinaryName(Location location, JavaFileObject file) {
		throw new UnsupportedOperationException();
	}

	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<Kind> kinds, boolean recurse) throws IOException {
		throw new UnsupportedOperationException();
	}

	public int isSupportedOption(String option) {
		throw new UnsupportedOperationException();
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		throw new UnsupportedOperationException();
	}
	
}
