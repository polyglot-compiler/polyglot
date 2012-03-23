package polyglot.util;

import java.io.File;
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

public class CustomJavaFileManager_ implements StandardJavaFileManager {

	private static final StandardJavaFileManager instance = new CustomJavaFileManager_();
	private final String separator = File.separator;
	private final char separatorChar = File.separatorChar;
	
	//private final Map<Location, Iterable<? extends File>> locationPathMap = new HashMap<Location, Iterable<? extends File>>();
	//private final Map<String, Set<JavaFileObject>> pathObjectMap = new HashMap<String, Set<JavaFileObject>>();
	//private final Map<String, JavaFileObject> fileObjMap = new HashMap<String, JavaFileObject>();
	private final StandardJavaFileManager javac_fm;
	
	private CustomJavaFileManager_() {
		javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
	}

	public static StandardJavaFileManager getInstance() {
		return instance;
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
	
	/*public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
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
				JarEntry entry = jarf.getJarEntry(className.replace('.', separatorChar)
						+ ".class");
				if (entry != null)
					return new CustomJavaFileObject(absPath, Kind.CLASS,
							jarf.getInputStream(entry));
			} else if (fileObjMap.containsKey(absPath))
				return fileObjMap.get(absPath);
			else {
				File f = new File(absPath);
				if (f.exists())
					return new CustomJavaFileObject(absPath, Kind.CLASS, false);
			}
		}
		return null;
	}*/

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
	/*public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		if (!kind.equals(Kind.CLASS))
			throw new UnsupportedOperationException();
		if(location == null || !location.equals(StandardLocation.CLASS_OUTPUT) || !locationPathMap.containsKey(location))
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
		int lastDot = className.lastIndexOf('.');
		if(lastDot > 0) {
			String pkg = className.substring(0, lastDot);
			File f = new File(path + separator + pkg.replace('.', separatorChar));
			if(!f.exists())
				f.mkdirs();
		}
		String absPath = path + separator + className.replace('.', separatorChar);
		JavaFileObject fo = new CustomJavaClassObject(absPath);
		//fileObjMap.put(absPath + ".class", fo);
		//if(pathObjectMap.containsKey(path))
		//	pathObjectMap.get(path).add(fo);
		//else {
		//	Set<JavaFileObject> s = new HashSet<JavaFileObject>();
		//	s.add(fo);
		//	pathObjectMap.put(path, s);
		//}
		return fo;
	}*/

	public boolean handleOption(String current, Iterator<String> remaining) {
		return javac_fm.handleOption(current, remaining);
	}

	public boolean hasLocation(Location location) {
		return javac_fm.hasLocation(location);
	}
	/*public boolean hasLocation(Location location) {
		return locationPathMap.containsKey(location);
	}*/

	// TODO: Implement inferBinaryName method correctly
	public String inferBinaryName(Location location, JavaFileObject file) {
		if(file instanceof CustomJavaFileObject) {
			String className = ((CustomJavaFileObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		return javac_fm.inferBinaryName(location, file);
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		return javac_fm.isSameFile(a, b);
	}

	private void setFiller(File f, Set<Kind> kinds, boolean recurse, Set<JavaFileObject> s) {
		for (File file : f.listFiles()) {
			if (recurse && file.isDirectory()) {
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
	
	/*private void setFiller(String parentFilePath, Set<Kind> kinds, Set<JavaFileObject> s) {
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
	}*/
	
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		return javac_fm.list(location, packageName, kinds, recurse);
	}
	/*public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		if(location == null || !locationPathMap.containsKey(location))
			return new HashSet<JavaFileObject>();
		if(location.equals(StandardLocation.PLATFORM_CLASS_PATH))
			return javac_fm.list(location, packageName, kinds, recurse);
		else if (location.equals(StandardLocation.CLASS_PATH)) {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			String pkg = packageName.equals("") ? "" : (separator + packageName.replace('.', separatorChar));
			for(File f : locationPathMap.get(location)) {
				String parentFilePath = f.getAbsolutePath() + pkg;
			//	if(pathObjectMap.containsKey(parentFilePath)) {
			//		setFiller(parentFilePath, kinds, s);
			//		if(recurse)
			//			for(String str : pathObjectMap.keySet())
			//				if(str.startsWith(parentFilePath))
			//					setFiller(str, kinds, s);
			//	} else {
					File file = new File(parentFilePath);
					if(!file.exists() || !file.isDirectory())
						return s;
					setFiller(file, kinds, recurse, s);
			//	}
			}
		}
		return new HashSet<JavaFileObject>();
	}*/

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
	/*public Iterable<? extends File> getLocation(Location location) {
		return locationPathMap.get(location);
	}*/

	public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
		javac_fm.setLocation(location, path);
	}
	/*public void setLocation(Location location, Iterable<? extends File> path)
			throws IOException {
		locationPathMap.put(location, path);
	}*/

}
