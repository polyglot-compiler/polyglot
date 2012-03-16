package polyglot.util;

import java.io.File;
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

public class CustomExtFileManager implements StandardJavaFileManager {

	private final String separator = File.separator;
	private final char separatorChar = File.separatorChar;
	
	/** Map for storing locations and associated paths */
	private final Map<Location, Iterable<? extends File>> locationPathMap;
	/** Map for storing paths in memory and associated JavaFileObjects */
	private final Map<String, Set<JavaFileObject>> pathObjectMap;
	/** Map for storing absolute names and FileObjects */
	private final Map<String, FileObject> absPathObjMap;

	public static final String DEFAULT_PKG = "intermediate_output";

	public CustomExtFileManager() {
		locationPathMap = new HashMap<Location, Iterable<? extends File>>();
		pathObjectMap = new HashMap<String, Set<JavaFileObject>>();
		absPathObjMap = new HashMap<String, FileObject>();
	}

	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void flush() throws IOException {
		throw new UnsupportedOperationException();
	}

	public ClassLoader getClassLoader(Location location) {
		throw new UnsupportedOperationException();
	}

	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		if(relativeName.startsWith("/") || relativeName.startsWith("./") || relativeName.startsWith("../"))
			return new CustomFileObject(relativeName, false);
		if(location == null || packageName == null || !locationPathMap.containsKey(location))
			return null;
		String pkg = packageName.equals("") ? "" : (packageName.replace('.', separatorChar) + separator);
		for (File file : locationPathMap.get(location)) {
			String absPath = file.getAbsolutePath() + separator + pkg + relativeName;
			File f = new File(absPath);
			if (f.exists())
				return new CustomFileObject(absPath, false);
			if(absPathObjMap.containsKey(absPath))
				return absPathObjMap.get(absPath);
		}
		return null;
	}

	/**
	 * NOTE: location must be StandardLocation.SOURCE_OUTPUT. Otherwise returns null.
	 */
	public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
		if(location == null || !location.equals(StandardLocation.SOURCE_OUTPUT) || !locationPathMap.containsKey(location))
			return null;
		if (packageName == null || packageName.equals(""))
			packageName = DEFAULT_PKG;
		String path = "";
		if(sibling == null) {
			for(File f : locationPathMap.get(location)) {
				path = f.getAbsolutePath();
				break;
			}
		} else {
			for(File f : locationPathMap.get(location)) {
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
		if(pathObjectMap.containsKey(parentFilePath)) {
			pathObjectMap.get(parentFilePath).add(fo);
		}
		else {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			s.add(fo);
			pathObjectMap.put(parentFilePath, s);
		}
		return fo;
	}

	/**
	 * NOTE: kind must be Kind.CLASS. Otherwise throws UnsupportedOperationException().
	 */
	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		if (!kind.equals(Kind.CLASS))
			throw new UnsupportedOperationException();
		if (!locationPathMap.containsKey(location))
			return null;
		for (File file : locationPathMap.get(location)) {
			String filePath = file.getAbsolutePath();
			String absPath = filePath + separator
					+ className.replace('.', separatorChar) + ".class";
			if (filePath.endsWith(".jar")) {
				JarFile jarf = new JarFile(filePath);
				JarEntry entry = jarf.getJarEntry(className.replace('.', separatorChar) + ".class");
				if (entry != null)
					return new CustomJavaFileObject(absPath, Kind.CLASS,
							jarf.getInputStream(entry));
			}
			File f = new File(absPath);
			if (f.exists())
				return new CustomJavaFileObject(absPath, Kind.CLASS, false);
		}
		return null;
	}

	/**
	 * NOTE: This method must be called when translated java source code needs to 
	 * be written in an object. (For intermediate code other than java, getFileForOutput
	 * method must be called.)
	 */
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (!kind.equals(Kind.SOURCE))
			throw new UnsupportedOperationException();
		if(location == null || !location.equals(StandardLocation.SOURCE_OUTPUT) || !locationPathMap.containsKey(location))
			return null;
		String path = "";
		if(sibling == null) {
			for(File f : locationPathMap.get(location)) {
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
		throw new UnsupportedOperationException();
	}

	public boolean hasLocation(Location location) {
		return locationPathMap.containsKey(location);
	}

	public String inferBinaryName(Location location, JavaFileObject file) {
		throw new UnsupportedOperationException();
	}

	private void setFiller(File f, Set<Kind> kinds, boolean recurse, Set<JavaFileObject> s) throws IOException {
		for (File file : f.listFiles()) {
			if (file.isDirectory() && recurse) {
				setFiller(file, kinds, recurse, s);
			} else if (file.isFile()) {
				String filename = file.getAbsolutePath();
				if (kinds.contains(Kind.SOURCE) && filename.endsWith(".java"))
					s.add(new CustomJavaFileObject(filename, Kind.SOURCE, false));
				else if (kinds.contains(Kind.CLASS) && filename.endsWith(".class"))
					s.add(new CustomJavaFileObject(filename, Kind.CLASS, false));
				else if (kinds.contains(Kind.HTML) && (filename.endsWith(".html") || filename.endsWith(".htm")))
					s.add(new CustomJavaFileObject(filename, Kind.HTML, false));
				else if (kinds.contains(Kind.OTHER))
					s.add(new CustomJavaFileObject(filename, Kind.OTHER, false));
			}
		}
	}

	private void setFiller(String parentFilePath, Set<Kind> kinds, Set<JavaFileObject> s) {
		for(JavaFileObject fo : pathObjectMap.get(parentFilePath)) {
			if (kinds.contains(Kind.SOURCE) && fo.getKind().equals(Kind.SOURCE))
				s.add(fo);
			else if (kinds.contains(Kind.CLASS) && fo.getKind().equals(Kind.CLASS))
				s.add(fo);
			else if (kinds.contains(Kind.HTML) && fo.getKind().equals(Kind.HTML))
				s.add(fo);
			else if (kinds.contains(Kind.OTHER) && fo.getKind().equals(Kind.OTHER))
				s.add(fo);
		}
	}

	private void setFiller(String filePath, String packageName, boolean recurse, Set<JavaFileObject> s) throws IOException {
		JarFile jarf = new JarFile(filePath);
		if(recurse)
			for (Enumeration<JarEntry> e = jarf.entries(); e.hasMoreElements();) {
				JarEntry entry = e.nextElement();
				String entryName = entry.getName();
				String pkg = entryName.substring(0, entryName.lastIndexOf(separatorChar));
				if(pkg.startsWith(packageName))
					s.add(new CustomJavaFileObject(filePath + separator + entryName, Kind.CLASS, jarf.getInputStream(entry)));
			}
		else
			for (Enumeration<JarEntry> e = jarf.entries(); e.hasMoreElements();) {
				JarEntry entry =  e.nextElement();
				String entryName = entry.getName();
				String pkg = entryName.substring(0, entryName.lastIndexOf(separatorChar));
				if(pkg.equals(packageName))
					s.add(new CustomJavaFileObject(filePath + separator + entryName, Kind.CLASS, jarf.getInputStream(entry)));
			}
	}
	
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		Set<JavaFileObject> s = new HashSet<JavaFileObject>();
		if(location == null || !locationPathMap.containsKey(location))
			return s;
		if(location.equals(StandardLocation.SOURCE_OUTPUT)) {
			for (File file : locationPathMap.get(location)) {
				String parentFilePath = file.getAbsolutePath() + separator + packageName.replace('.', separatorChar);
				if(pathObjectMap.containsKey(parentFilePath)) {
					setFiller(parentFilePath, kinds, s);
					if(recurse)
						for(String str : pathObjectMap.keySet())
							if(str.startsWith(parentFilePath))
								setFiller(str, kinds, s);
				}
			}
		} else {
			for (File file : locationPathMap.get(location)) {
				String filePath = file.getAbsolutePath();
				String parentFilePath = filePath + separator
						+ packageName.replace('.', separatorChar);
				//if(kinds.contains(Kind.CLASS) && filePath.endsWith(".jar")) {
					//setFiller(filePath, packageName, recurse, s);
				//	StandardJavaFileManager javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
				//	javac_fm.setLocation(location, locationPathMap.get(location));
				//	for(JavaFileObject fo : javac_fm.list(location, packageName, kinds, recurse))
				//		s.add(fo);
				//} else {
					File f = new File(parentFilePath);
					if (!f.exists() || !f.isDirectory())
						return s;
					setFiller(f, kinds, recurse, s);
				//}
			}
		} 
		return s;
	}

	public int isSupportedOption(String option) {
		throw new UnsupportedOperationException();
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
		return locationPathMap.get(location);
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		return a.toUri().equals(b.toUri());
	}

	public void setLocation(Location location, Iterable<? extends File> path)
			throws IOException {
		locationPathMap.put(location, path);
	}

}
