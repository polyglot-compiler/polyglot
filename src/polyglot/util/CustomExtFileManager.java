package polyglot.util;

import java.io.File;
import static java.io.File.separator;
import static java.io.File.separatorChar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import static polyglot.util.SharedResources.pathObjectMap;;

public class CustomExtFileManager implements StandardJavaFileManager {

	private static final StandardJavaFileManager instance = new CustomExtFileManager();
	
	private final StandardJavaFileManager javac_fm; // JavacFileManager used by javac
	
	/** Map for storing in-memory FileObjects and associated fully qualified names */
	private final Map<String, FileObject> absPathObjMap;

	public static final String DEFAULT_PKG = "intermediate_output";

	private CustomExtFileManager() {
		javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
		absPathObjMap = new HashMap<String, FileObject>();
	}
	
	public static StandardJavaFileManager getInstance() {
		return instance;
	}
	
	public static StandardJavaFileManager getNewInstance() {
		return new CustomExtFileManager();
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
	
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		if(location.equals(StandardLocation.SOURCE_OUTPUT)) {
			String pkg = packageName.equals("") ? "" : (packageName.replace('.', separatorChar) + separator);
			for (File f : javac_fm.getLocation(location)) {
				String absPath = f.getAbsolutePath() + separator + pkg + relativeName;
				FileObject fo = absPathObjMap.get(absPath);
				if (fo != null)
					return fo;
			}
			return null;
		}
		return javac_fm.getFileForInput(location, packageName, relativeName);
	}

	public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
		if (location == null || !location.equals(StandardLocation.SOURCE_OUTPUT) || !javac_fm.hasLocation(location))
			return null;
		if (packageName == null || packageName.equals(""))
			packageName = DEFAULT_PKG;
		String path = "";
		if(sibling == null) {
			for(File f : javac_fm.getLocation(location)) {
				path = f.getAbsolutePath();
				break;
			}
		} else {
			for(File f : javac_fm.getLocation(location)) {
				String[] files = f.list();
				for(String s : files)
					if(s.equals(sibling.getName()))
						path = f.getAbsolutePath();
			}
		}
		String parentFilePath = path + separator + packageName.replace('.', separatorChar);
		String absPath = parentFilePath + separator + relativeName;
		Kind k;
		if(absPath.endsWith(".java"))
			k = Kind.SOURCE;
		else if(absPath.endsWith(".class"))
			k = Kind.CLASS;
		else if(absPath.endsWith(".htm") || absPath.endsWith(".html"))
			k = Kind.HTML;
		else
			k = Kind.OTHER;
		JavaFileObject fo = new CustomJavaFileObject(absPath, k, true);
		absPathObjMap.put(absPath, fo);
		if(pathObjectMap.containsKey(parentFilePath))
			pathObjectMap.get(parentFilePath).add(fo);
		else {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			s.add(fo);
			pathObjectMap.put(parentFilePath, s);
		}
		return fo;
	}

	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		if (!kind.equals(Kind.CLASS))
			throw new UnsupportedOperationException();
		return javac_fm.getJavaFileForInput(location, className, kind);
	}
	
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (!kind.equals(Kind.SOURCE))
			throw new UnsupportedOperationException();
		if(location == null || !location.equals(StandardLocation.SOURCE_OUTPUT) || !javac_fm.hasLocation(location))
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
		String classNamePath = className.replace('.', separatorChar);
		String absPath;
		if(classNamePath.startsWith(path))
			absPath = classNamePath;
		else
			absPath = path + separator + classNamePath.substring(classNamePath.lastIndexOf(separatorChar) + 1);
		JavaFileObject fo = new CustomJavaSourceObject(absPath);
		if(pathObjectMap.containsKey(path))
			pathObjectMap.get(path).add(fo);
		else {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			s.add(fo);
			pathObjectMap.put(path, s);
		}
		return fo;
	}

	public boolean handleOption(String current, Iterator<String> remaining) {
		return javac_fm.handleOption(current, remaining);
	}

	public boolean hasLocation(Location location) {
		return javac_fm.hasLocation(location);
	}

	public String inferBinaryName(Location location, JavaFileObject file) {
		return javac_fm.inferBinaryName(location, file);
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
		Set<JavaFileObject> s = new HashSet<JavaFileObject>();
		if(location == null || !javac_fm.hasLocation(location))
			return s;
		if(location.equals(StandardLocation.SOURCE_OUTPUT)) {
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
			return s;
		}
		return javac_fm.list(location, packageName, kinds, recurse);
	}

	public int isSupportedOption(String option) {
		return javac_fm.isSupportedOption(option);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(
			String... names) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
			Iterable<? extends File> files) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
			Iterable<String> names) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends File> getLocation(Location location) {
		return javac_fm.getLocation(location);
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		return a.toUri().equals(b.toUri());
	}

	public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
		javac_fm.setLocation(location, path);
	}

}
