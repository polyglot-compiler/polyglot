package polyglot.filemanager;

import java.io.File;
import static java.io.File.separator;
import static java.io.File.separatorChar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * FileManager implementation - A class that provides input and output access to
 * the local file system. (NOTE: Extensions may extend this implementation and
 * are not forced to use local file system for i/o.)
 */
public class ExtFileManager implements FileManager {

	protected final ExtensionInfo extInfo;
	/** JavacFileManager used by java compiler */
	protected final StandardJavaFileManager javac_fm;
	/** Map of sources already loaded */
	protected final Map<String, FileSource> loadedSources;
	/** List of locations in which .class files are searched */
	protected final List<Location> locations;
	/** A cache for package look ups */
	protected final Map<String, Boolean> packageCache;
	/** A cache for the class files that don't exist */
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
	protected final Map<URI, JavaFileObject> absPathObjMap;
	/**
	 * Map for storing fully qualified package names and contained
	 * JavaFileObjects
	 */
	protected final Map<URI, Set<JavaFileObject>> pathObjectMap;
	/**
	 * Indicates if the file system is case-insensitive.
	 */
	private int caseInsensitive;
	/**
	 * Indicates if the file system case-sensitivity is set
	 */
	private boolean caseInsensitivityComputed;

	public static final String DEFAULT_PKG = "intermediate_output";

	public ExtFileManager(ExtensionInfo extInfo) {
		this.extInfo = extInfo;
		javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(
				null, null, null);
		loadedSources = new HashMap<String, FileSource>();
		locations = new ArrayList<Location>();
		packageCache = new HashMap<String, Boolean>();
		nocache = new HashSet<String>();
		absPathObjMap = new HashMap<URI, JavaFileObject>();
		pathObjectMap = new HashMap<URI, Set<JavaFileObject>>();
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
			String newName = packageName.equals("") ? ("" + relativeName)
					: (packageName.replace('.', separatorChar) + separator + relativeName);
			for (File f : javac_fm.getLocation(location)) {
				URI u = new File(f, newName).toURI();
				JavaFileObject jfo = absPathObjMap.get(u);
				if (jfo != null)
					return jfo;
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
		if (options.outputToFS)
			return javac_fm.getFileForOutput(location, packageName, relativeName, sibling);
		URI srcUri, srcParentUri;
		Kind k;
		if (relativeName.endsWith(".java"))
			k = Kind.SOURCE;
		else if (relativeName.endsWith(".class"))
			k = Kind.CLASS;
		else if (relativeName.endsWith(".html")
				|| relativeName.endsWith(".htm"))
			k = Kind.HTML;
		else
			k = Kind.OTHER;
		if (sibling == null) {
			File sourcedir = null;
			for (File f : javac_fm.getLocation(sourceOutputLoc)) {
				sourcedir = f;
				break;
			}
			if (sourcedir == null)
				throw new IOException("Source output directory is not set.");
			String pkg = packageName.equals("") ? "" : (packageName.replace('.', separatorChar) + separator);
			File sourcefile = new File(sourcedir, pkg + relativeName);
			srcUri = sourcefile.toURI();
			srcParentUri = sourcefile.getParentFile().toURI();
		} else {
			File sourcedir = new File(sibling.toUri()).getParentFile();
			File sourcefile = new File(sourcedir,
					relativeName.substring(relativeName
							.lastIndexOf(separatorChar) + 1));
			srcUri = sourcefile.toURI();
			srcParentUri = sourcefile.getParentFile().toURI();
		}
		JavaFileObject jfo = new SourceObject(srcUri, k);
		absPathObjMap.put(srcUri, jfo);
		if (pathObjectMap.containsKey(srcParentUri))
			pathObjectMap.get(srcParentUri).add(jfo);
		else {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			s.add(jfo);
			pathObjectMap.put(srcParentUri, s);
		}
		return jfo;
	}

	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		Options options = extInfo.getOptions();
		Location sourceOutputLoc = options.outputDirectory();
		if (sourceOutputLoc.equals(location)) {
			String clazz = className.replace('.', separatorChar)
					+ kind.extension;
			for (File f : javac_fm.getLocation(sourceOutputLoc)) {
				URI u = new File(f, clazz).toURI();
				JavaFileObject jfo = absPathObjMap.get(u);
				if (jfo != null)
					return jfo;
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
			if (options.outputToFS)
				return javac_fm.getJavaFileForOutput(location, className, kind, sibling);
			URI srcUri, srcParentUri;
			if (sibling == null) {
				File sourcedir = null;
				for (File f : javac_fm.getLocation(sourceOutputLoc)) {
					sourcedir = f;
					break;
				}
				if (sourcedir == null)
					throw new IOException("Source output directory is not set.");
				File sourcefile = new File(sourcedir, className.replace('.',
						separatorChar) + kind.extension);
				srcUri = sourcefile.toURI();
				srcParentUri = sourcefile.getParentFile().toURI();
			} else {
				File sourcedir = new File(sibling.toUri()).getParentFile();
				File sourcefile = new File(sourcedir,
						className.substring(className.lastIndexOf('.') + 1)
								+ kind.extension);
				srcUri = sourcefile.toURI();
				srcParentUri = sourcefile.getParentFile().toURI();
			}
			JavaFileObject jfo = new SourceObject(srcUri, kind);
			absPathObjMap.put(srcUri, jfo);
			if (pathObjectMap.containsKey(srcParentUri))
				pathObjectMap.get(srcParentUri).add(jfo);
			else {
				Set<JavaFileObject> s = new HashSet<JavaFileObject>();
				s.add(jfo);
				pathObjectMap.put(srcParentUri, s);
			}
			return jfo;
		} else if (kind.equals(Kind.CLASS)) {
			if (location == null || !classOutputLoc.equals(location)
					|| !javac_fm.hasLocation(classOutputLoc))
				return null;
			return javac_fm.getJavaFileForOutput(classOutputLoc, className, kind,
					sibling);
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
		if (file instanceof SourceObject) {
			String className = ((SourceObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		return javac_fm.inferBinaryName(location, file);
	}

	private void setFiller(URI parentUri, Set<Kind> kinds, Set<JavaFileObject> s) {
		for (JavaFileObject fo : pathObjectMap.get(parentUri)) {
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
		if (location == null)
			return new HashSet<JavaFileObject>();
		if (sourceOutputLoc.equals(location)) {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			String pkg = packageName.replace('.', separatorChar);
			for (File file : javac_fm.getLocation(sourceOutputLoc)) {
				URI parentUri = new File(file, pkg).toURI();
				if (pathObjectMap.containsKey(parentUri)) {
					setFiller(parentUri, kinds, s);
					if (recurse)
						for (URI u : pathObjectMap.keySet())
							if (u.getPath().startsWith(parentUri.getPath()))
								setFiller(u, kinds, s);
				}
			}
			return s;
		}
		else if (classOutputLoc.equals(location))
			return javac_fm.list(classOutputLoc, packageName, kinds, recurse);
		else
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
		String pkg = name.replace('.', File.separatorChar);
		Iterable<? extends File> files = getLocation(location);
		if (files == null)
			return false;
		for (File f : files) {
			File newFile = new File(f, pkg);
			if (newFile.exists() && newFile.isDirectory())
				return true;
		}
		return false;
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
		String key = fileKey(location, "", fileName);
		
		FileSource sourceFile = loadedSources.get(key);
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

		loadedSources.put(key, sourceFile);
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

			String key = fileKey(location, pkgName, fileName);
			FileSource source = loadedSources.get(key);
			// Skip it if already loaded
			if (source != null) {
				return source;
			}
			
			FileObject fo;
			try {
				fo = getFileForInput(location, pkgName, fileName);
			} catch (IOException e1) {
				return null;
			}
			if (fo == null)
				continue;

			try {
				source = extInfo.createFileSource(fo, false);
				if (Report.should_report(Report.loader, 2))
					Report.report(2, "Loading " + className + " from " + source);

				loadedSources.put(key, source);
				return source;
			} catch (IOException e) {
			}
		}
		return null;
	}

	protected String fileKey(Location location, String packageName, String fileName) {
		return location + "/" + packageName + "/" + fileName;
	}

	@Override
	public Map<URI, JavaFileObject> getAbsPathObjMap() {
		return Collections.unmodifiableMap(absPathObjMap);
	}

	@Override
	public boolean caseInsensitive() {
		if (!caseInsensitivityComputed) {
			setCaseInsensitive(System.getProperty("user.dir"));
			caseInsensitivityComputed = true;
		}
		if (caseInsensitive == 0) {
            throw new InternalCompilerError("unknown case sensitivity");
        }
        return caseInsensitive == 1;
	}
	
	private void setCaseInsensitive(String fileName) {
        if (caseInsensitive != 0) {
            return;
        }

        // File.equals doesn't work correctly on the Mac.
        // So, get the list of files in the same directory
        // as sourceFile.  Check if the sourceFile with two
        // different cases exists but only appears in the list once.
        File f1 = new File(fileName.toUpperCase());
        File f2 = new File(fileName.toLowerCase());

        if (f1.equals(f2)) {
            caseInsensitive = 1;
        }
        else if (f1.exists() && f2.exists()) {
            boolean f1Exists = false;
            boolean f2Exists = false;

            File dir;

            if (f1.getParent() != null) {
                dir = new File(f1.getParent());
            }
            else {
                dir = new File(fileName);
            }

            File[] ls = dir.listFiles();
            if (ls != null) {
                for (int i = 0; i < ls.length; i++) {
                    if (f1.equals(ls[i])) {
                        f1Exists = true;
                    }
                    if (f2.equals(ls[i])) {
                        f2Exists = true;
                    }
                }
            }
            else {
                // dir not found
            }

            if (! f1Exists || ! f2Exists) {
                caseInsensitive = 1;
            }
            else {
                // There are two files.
                caseInsensitive = -1;
            }
        }
        else {
            caseInsensitive = -1;
        }
    }
}
