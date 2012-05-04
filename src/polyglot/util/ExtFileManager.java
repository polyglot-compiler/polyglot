package polyglot.util;

import java.io.File;
import static java.io.File.separator;
import static java.io.File.separatorChar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.FileSource;
import polyglot.frontend.SourceLoader;
import polyglot.main.Options;
import polyglot.main.Report;
import polyglot.types.reflect.ClassFile;
import polyglot.types.reflect.ClassFileLoader;

public class ExtFileManager implements FileManager {

	protected final ExtensionInfo extInfo;
	/** JavacFileManager used by java compiler */
	protected final StandardJavaFileManager javac_fm;
	/** Map of sources already loaded */
	protected final Map<Object, FileSource> loadedSources;
	/** List of locations in which .class files are searched */
	protected final List<Location> locations;
	/** A cache for package look ups */
	protected final Map<String, Boolean> packageCache;

	protected final Set<String> nocache;

	protected static final int BUF_SIZE = 1024 * 8;

	protected final static Collection<String> report_topics = CollectionUtil
			.list(Report.types, Report.resolver, Report.loader);

	protected static final Set<Kind> ALL_KINDS = new HashSet<Kind>();
	static {
		ALL_KINDS.add(Kind.CLASS);
		ALL_KINDS.add(Kind.SOURCE);
		ALL_KINDS.add(Kind.HTML);
		ALL_KINDS.add(Kind.OTHER);
	}
	/**
	 * Map for storing in-memory FileObjects and associated fully qualified
	 * names
	 */
	protected final Map<String, JavaFileObject> absPathObjMap;
	/**
	 * Map for storing fully qualified package names and contained
	 * JavaFileObjects
	 */
	protected final Map<String, Set<JavaFileObject>> pathObjectMap;

	public static final String DEFAULT_PKG = "intermediate_output";

	public ExtFileManager(ExtensionInfo extInfo) {
		this.extInfo = extInfo;
		javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(
				null, null, null);
		loadedSources = new HashMap<Object, FileSource>();
		locations = new ArrayList<Location>();
		packageCache = new HashMap<String, Boolean>();
		nocache = new HashSet<String>();
		absPathObjMap = new HashMap<String, JavaFileObject>();
		pathObjectMap = new HashMap<String, Set<JavaFileObject>>();
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
		Options options = extInfo.getOptions();
		Location sourceOutputLoc = options.outputDirectory();
		if (sourceOutputLoc.equals(location)) {
			String newName = separator
					+ (packageName.equals("") ? ("" + relativeName)
							: (packageName.replace('.', separatorChar)
									+ separator + relativeName));
			for (File f : javac_fm.getLocation(location)) {
				String absPath = f.getAbsolutePath() + newName;
				JavaFileObject fo = absPathObjMap.get(absPath);
				if (fo != null)
					return fo;
			}
			return null;
		}
		return javac_fm.getFileForInput(location, packageName, relativeName);
	}

	public FileObject getFileForOutput(Location location, String packageName,
			String relativeName, FileObject sibling) throws IOException {
		Options options = extInfo.getOptions();
		Location sourceOutputLoc = options.outputDirectory();
		if (location == null || !sourceOutputLoc.equals(location)
				|| !javac_fm.hasLocation(sourceOutputLoc))
			return null;
		if (packageName == null || packageName.equals(""))
			packageName = DEFAULT_PKG;
		String path = "";
		if (sibling == null) {
			for (File f : javac_fm.getLocation(sourceOutputLoc)) {
				path = f.getAbsolutePath();
				break;
			}
		} else {
			for (File f : javac_fm.getLocation(sourceOutputLoc)) {
				String[] files = f.list();
				for (String s : files)
					if (s.equals(sibling.getName()))
						path = f.getAbsolutePath();
			}
		}
		String parentFilePath = path + separator
				+ packageName.replace('.', separatorChar);
		String absPath = parentFilePath + separator + relativeName;
		Kind k;
		if (absPath.endsWith(".java"))
			k = Kind.SOURCE;
		else if (absPath.endsWith(".class"))
			k = Kind.CLASS;
		else if (absPath.endsWith(".htm") || absPath.endsWith(".html"))
			k = Kind.HTML;
		else
			k = Kind.OTHER;
		JavaFileObject fo = new JavaFileObject_c(absPath, k, true);
		absPathObjMap.put(absPath, fo);
		if (pathObjectMap.containsKey(parentFilePath))
			pathObjectMap.get(parentFilePath).add(fo);
		else {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			s.add(fo);
			pathObjectMap.put(parentFilePath, s);
		}
		return fo;
	}

	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		Options options = extInfo.getOptions();
		Location sourceOutputLoc = options.outputDirectory();
		if (sourceOutputLoc.equals(location)) {
			String clazz = separator + className.replace('.', separatorChar)
					+ kind.extension;
			for (File f : javac_fm.getLocation(sourceOutputLoc)) {
				String absPath = f.getAbsolutePath() + clazz;
				JavaFileObject fo = absPathObjMap.get(absPath);
				if (fo != null)
					return fo;
			}
			return null;
		}
		return javac_fm.getJavaFileForInput(location, className, kind);
	}

	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		Options options = extInfo.getOptions();
		Location sourceOutputLoc = options.outputDirectory();
		Location classOutputLoc = options.classOutputDirectory();
		if (kind.equals(Kind.SOURCE)) {
			if (location == null || !sourceOutputLoc.equals(location)
					|| !javac_fm.hasLocation(sourceOutputLoc))
				return null;
			String path = "";
			if (sibling == null) {
				for (File f : javac_fm.getLocation(sourceOutputLoc)) {
					path = f.getAbsolutePath();
					break;
				}
			} else {
				String siblingPath = sibling.getName();
				path = siblingPath.substring(0,
						siblingPath.lastIndexOf(separatorChar));
			}
			String classNamePath = className.replace('.', separatorChar);
			String absPath;
			if (classNamePath.startsWith(path))
				absPath = classNamePath;
			else
				absPath = path
						+ separator
						+ classNamePath.substring(classNamePath
								.lastIndexOf(separatorChar) + 1);
			JavaFileObject fo = new JavaSourceObject(absPath);
			if (pathObjectMap.containsKey(path))
				pathObjectMap.get(path).add(fo);
			else {
				Set<JavaFileObject> s = new HashSet<JavaFileObject>();
				s.add(fo);
				pathObjectMap.put(path, s);
			}
			return fo;
		} else if (kind.equals(Kind.CLASS)) {
			if (location == null || !classOutputLoc.equals(location)
					|| !javac_fm.hasLocation(classOutputLoc))
				return null;
			String path = "";
			if (sibling == null) {
				for (File f : javac_fm.getLocation(classOutputLoc)) {
					path = f.getAbsolutePath();
					break;
				}
			} else {
				String siblingPath = sibling.getName();
				path = siblingPath.substring(0,
						siblingPath.lastIndexOf(separatorChar));
			}
			int lastDot = className.lastIndexOf('.');
			if (lastDot > 0) {
				String pkg = className.substring(0, lastDot);
				File f = new File(path + separator
						+ pkg.replace('.', separatorChar));
				if (!f.exists())
					f.mkdirs();
			}
			String absPath = path + separator
					+ className.replace('.', separatorChar);
			return new JavaClassObject(absPath);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public boolean handleOption(String current, Iterator<String> remaining) {
		return javac_fm.handleOption(current, remaining);
	}

	public boolean hasLocation(Location location) {
		return javac_fm.hasLocation(location);
	}

	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof FileObject_c) {
			String className = ((FileObject_c) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		if (file instanceof JavaSourceObject) {
			String className = ((JavaSourceObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		if (file instanceof JavaClassObject) {
			String className = ((JavaClassObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		return javac_fm.inferBinaryName(location, file);
	}

	private void setFiller(String parentFilePath, Set<Kind> kinds,
			Set<JavaFileObject> s) {
		for (JavaFileObject fo : pathObjectMap.get(parentFilePath)) {
			if (kinds.contains(Kind.SOURCE) && fo.getKind().equals(Kind.SOURCE))
				s.add(fo);
			else if (kinds.contains(Kind.CLASS)
					&& fo.getKind().equals(Kind.CLASS))
				s.add(fo);
			else if (kinds.contains(Kind.HTML)
					&& fo.getKind().equals(Kind.HTML))
				s.add(fo);
			else if (kinds.contains(Kind.OTHER))
				s.add(fo);
		}
	}

	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<Kind> kinds, boolean recurse) throws IOException {
		Options options = extInfo.getOptions();
		Location sourceOutputLoc = options.outputDirectory();
		Location classOutputLoc = options.classOutputDirectory();
		if (location == null
				|| (!sourceOutputLoc.equals(location)
						&& !classOutputLoc.equals(location) && !javac_fm
							.hasLocation(location)))
			return new HashSet<JavaFileObject>();
		if (sourceOutputLoc.equals(location)) {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			String pkg = separator + packageName.replace('.', separatorChar);
			for (File file : javac_fm.getLocation(sourceOutputLoc)) {
				String parentFilePath = file.getAbsolutePath() + pkg;
				if (pathObjectMap.containsKey(parentFilePath)) {
					setFiller(parentFilePath, kinds, s);
					if (recurse)
						for (String str : pathObjectMap.keySet())
							if (str.startsWith(parentFilePath))
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

	public void setLocation(Location location, Iterable<? extends File> path)
			throws IOException {
		javac_fm.setLocation(location, path);
	}

	@Override
	public boolean packageExists(String name) {
		Boolean exists = packageCache.get(name);
		if (exists != null)
			return exists;
		exists = false;
		for (int i = locations.size() - 1; i >= 0; i--) {
			exists = packageExists(locations.get(i), name);
			if (exists)
				break;
		}
		packageCache.put(name, exists);
		return exists;
	}

	@Override
	public boolean packageExists(Location location, String name) {
		Iterable<JavaFileObject> contents;
		try {
			contents = list(location, name, ALL_KINDS, false);
		} catch (IOException e) {
			throw new InternalCompilerError("Error while checking for package "
					+ name, e);
		}
		return contents.iterator().hasNext();
	}

	@Override
	public ClassFile loadFile(String name) {
		if (nocache.contains(name))
			return null;
		ClassFile clazz = null;
		for (int i = locations.size() - 1; i >= 0; i--) {
			clazz = loadFile(locations.get(i), name);
			if (clazz != null)
				break;
		}
		if (clazz == null)
			nocache.add(name);
		return clazz;
	}

	@Override
	public ClassFile loadFile(Location location, String name) {
		try {
			JavaFileObject jfo = null;
			try {
				jfo = getJavaFileForInput(location, name, Kind.CLASS);
			} catch (IOException e) {
				throw new InternalCompilerError(
						"Error while checking for class file " + name, e);
			}
			if (jfo != null) {
				if (Report.should_report(report_topics, 4)) {
					Report.report(4, "Class " + name + " found in " + location
							+ " at " + jfo.toUri());
				}
			} else {
				if (Report.should_report(report_topics, 4)) {
					Report.report(4, "Class " + name + " not found in "
							+ location);
				}
			}

			if (jfo != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				InputStream is = jfo.openInputStream();
				byte buf[] = new byte[BUF_SIZE];
				int len;
				while ((len = is.read(buf, 0, BUF_SIZE)) != -1)
					bos.write(buf, 0, len);

				ClassFile clazz = extInfo.createClassFile(jfo,
						bos.toByteArray());

				return clazz;
			}
		} catch (ClassFormatError e) {
			if (Report.should_report(report_topics, 4))
				Report.report(4, "Class " + name + " format error");
		} catch (IOException e) {
			if (Report.should_report(report_topics, 4))
				Report.report(4, "Error loading class " + name);
		}
		return null;
	}

	@Override
	public void addLocation(Location loc) {
		locations.add(loc);
	}

	@Override
	public FileSource fileSource(String fileName) throws IOException {
		return fileSource(extInfo.getOptions().source_path, fileName, false);
	}

	@Override
	public FileSource fileSource(String fileName, boolean userSpecified)
			throws IOException {
		return fileSource(extInfo.getOptions().source_path, fileName,
				userSpecified);
	}

	@Override
	public FileSource fileSource(Location location, String fileName)
			throws IOException {
		return fileSource(location, fileName, false);
	}

	@Override
	public FileSource fileSource(Location location, String fileName,
			boolean userSpecified) throws IOException {
		FileSource sourceFile = loadedSources.get(fileName);
		if (sourceFile != null)
			return sourceFile;

		FileObject fo = getFileForInput(location, "", fileName);
		if (fo == null)
			throw new FileNotFoundException("File: " + fileName + " not found.");
		sourceFile = extInfo.createFileSource(fo, userSpecified);

		String[] exts = extInfo.fileExtensions();
		boolean ok = false;

		for (int i = 0; i < exts.length; i++) {
			String ext = exts[i];

			if (fileName.endsWith("." + ext)) {
				ok = true;
				break;
			}
		}

		if (!ok) {
			String extString = "";

			for (int i = 0; i < exts.length; i++) {
				if (exts.length == 2 && i == exts.length - 1) {
					extString += " or ";
				} else if (exts.length != 1 && i == exts.length - 1) {
					extString += ", or ";
				} else if (i != 0) {
					extString += ", ";
				}
				extString = extString + "\"." + exts[i] + "\"";
			}

			if (exts.length == 1) {
				throw new IOException("Source \"" + fileName
						+ "\" does not have the extension " + extString + ".");
			} else {
				throw new IOException("Source \"" + fileName
						+ "\" does not have any of the extensions " + extString
						+ ".");
			}
		}

		if (Report.should_report(Report.loader, 2))
			Report.report(2, "Loading class from " + sourceFile);

		if (sourceFile != null) {
			if (!sourceFile.userSpecified() && userSpecified) {
				sourceFile.setUserSpecified(true);
			}
			return sourceFile;
		}

		loadedSources.put(fileKey(fo), sourceFile);
		return sourceFile;
	}

	@Override
	public FileSource classSource(String className) {
		return classSource(extInfo.getOptions().source_path, className);
	}

	@Override
	public FileSource classSource(Location location, String className) {
		String name = className;
		boolean done = false;
		while (!done) {
			FileSource source = checkForSource(location, name);
			if (source != null)
				return source;
			int dot = name.lastIndexOf('.');
			if (dot == -1)
				done = true;
			else
				name = name.substring(0, dot);
		}
		return null;
	}

	/** Load the source file for the given class name using the source path. */
	protected FileSource checkForSource(Location location, String className) {
		/* Search the source path. */
		String[] exts = extInfo.fileExtensions();
		for (int k = 0; k < exts.length; k++) {
			String pkgName = StringUtil.getPackageComponent(className);
			String shortName = StringUtil.getShortNameComponent(className);
			String fileName = shortName + "." + exts[k];

			FileObject fo;
			try {
				fo = getFileForInput(location, pkgName, fileName);
			} catch (IOException e1) {
				return null;
			}
			if (fo == null)
				continue;

			FileSource source = (FileSource) loadedSources.get(fileKey(fo));
			// Skip it if already loaded
			if (source != null) {
				return source;
			}

			try {
				source = extInfo.createFileSource(fo, false);
				if (Report.should_report(Report.loader, 2))
					Report.report(2, "Loading " + className + " from " + source);

				loadedSources.put(fileKey(fo), source);
				return source;
			} catch (IOException e) {
			}
		}
		return null;
	}

	protected Object fileKey(FileObject fo) {
		return fo.getName();
	}

}
