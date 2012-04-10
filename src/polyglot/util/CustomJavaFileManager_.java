package polyglot.util;

import java.io.File;
import static java.io.File.separator;
import static java.io.File.separatorChar;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import static polyglot.util.SharedResources.pathObjectMap;

public class CustomJavaFileManager_ implements StandardJavaFileManager {
	
	private final StandardJavaFileManager javac_fm; //JavacFileManager used by javac
	
	public CustomJavaFileManager_() {
		javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
	}
	
	public int isSupportedOption(String option) {
		return javac_fm.isSupportedOption(option);
	}

	public void close() throws IOException {
		javac_fm.close();
	}

	public void flush() throws IOException {
		javac_fm.flush();
	}

	public ClassLoader getClassLoader(Location location) {
		return javac_fm.getClassLoader(location);
	}

	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		throw new UnsupportedOperationException();
	}

	public FileObject getFileForOutput(Location location, String packageName,
			String relativeName, FileObject sibling) throws IOException {
		throw new UnsupportedOperationException();
	}

	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		throw new UnsupportedOperationException();
	}

	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (!kind.equals(Kind.CLASS))
			throw new UnsupportedOperationException();
		if(location == null || !location.equals(StandardLocation.CLASS_OUTPUT) || !javac_fm.hasLocation(location))
			return null;
		String path = "";
		if(sibling == null) {
			for(File f : javac_fm.getLocation(location)) {
				path = f.getAbsolutePath();
				break;
			}
		} else {
			String siblingPath = sibling.getName();
			path = siblingPath.substring(0, siblingPath.lastIndexOf(separatorChar));
		}
		int lastDot = className.lastIndexOf('.');
		if(lastDot > 0) {
			String pkg = className.substring(0, lastDot);
			File f = new File(path + separator + pkg.replace('.', separatorChar));
			if(!f.exists())
				f.mkdirs();
		}
		String absPath = path + separator + className.replace('.', separatorChar);
		return new CustomJavaClassObject(absPath);
	}

	public boolean handleOption(String current, Iterator<String> remaining) {
		return javac_fm.handleOption(current, remaining);
	}

	public boolean hasLocation(Location location) {
		return javac_fm.hasLocation(location);
	}

	// TODO: Implement inferBinaryName method correctly
	public String inferBinaryName(Location location, JavaFileObject file) {
		if(file instanceof CustomJavaFileObject) {
			String className = ((CustomJavaFileObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		return javac_fm.inferBinaryName(location, file);
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		if (a instanceof CustomFileObject || a instanceof CustomJavaFileObject || a instanceof CustomJavaSourceObject || a instanceof CustomJavaClassObject) {
			if (!(a.getClass().getName().equals(b.getClass().getName())))
				return false;
			return a.toUri().equals(b.toUri());
		}
		return javac_fm.isSameFile(a, b);
	}

	private void setFiller(String parentFilePath, Set<Kind> kinds, Set<JavaFileObject> s) {
		for(JavaFileObject fo : pathObjectMap.get(parentFilePath)) {
			if (kinds.contains(Kind.SOURCE) && fo.getKind().equals(Kind.SOURCE))
				s.add(fo);
			else if (kinds.contains(Kind.CLASS) && fo.getKind().equals(Kind.CLASS))
				s.add(fo);
			else if (kinds.contains(Kind.HTML) && fo.getKind().equals(Kind.HTML))
				s.add(fo);
			else if (kinds.contains(Kind.OTHER))
				s.add(fo);
		}
	}

	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		if (location.equals(StandardLocation.SOURCE_OUTPUT)) {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			for (File file : javac_fm.getLocation(location)) {
				String parentFilePath = file.getAbsolutePath() + separator + packageName.replace('.', separatorChar);
				if(pathObjectMap.containsKey(parentFilePath)) {
					setFiller(parentFilePath, kinds, s);
					if(recurse)
						for(String str : pathObjectMap.keySet())
							if(str.startsWith(parentFilePath))
								setFiller(str, kinds, s);
				}
			}
		}
		return javac_fm.list(location, packageName, kinds, recurse);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(File... arg0) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(String... arg0) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
			Iterable<? extends File> arg0) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
			Iterable<String> arg0) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends File> getLocation(Location location) {
		return javac_fm.getLocation(location);
	}

	public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
		javac_fm.setLocation(location, path);
	}

}
