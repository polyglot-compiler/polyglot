/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package polyglot.filemanager;

import static java.io.File.separatorChar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.FileSource;
import polyglot.frontend.Source;
import polyglot.main.Main;
import polyglot.main.Report;
import polyglot.types.reflect.ClassFile;
import polyglot.util.CollectionUtil;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/**
 * FileManager implementation - A class that provides input and output access to
 * the local file system. (NOTE: Extensions may extend this implementation and
 * are not forced to use local file system for i/o.)
 */
public class ExtFileManager extends
        ForwardingJavaFileManager<StandardJavaFileManager> implements
        FileManager {
    protected static final JavaCompiler javaCompiler = Main.javaCompiler();

    protected final ExtensionInfo extInfo;
    /** Map of sources already loaded */
    protected final Map<String, FileSource> loadedSources;
    /** A cache for package look ups */
    protected static final Map<String, Boolean> packageCache = new HashMap<>();
    /** A cache for the class files that don't exist */
    protected static final Set<String> nocache = new HashSet<>();

    protected static final Map<File, Object> zipCache = new HashMap<>();

    protected static final Object not_found = new Object();

    protected static final int BUF_SIZE = 1024 * 8;

    protected static final Collection<String> report_topics =
            CollectionUtil.list(Report.types, Report.resolver, Report.loader);

    protected static final Set<Kind> ALL_KINDS = new HashSet<>();
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
    protected final Map<Location, Map<String, JavaFileObject>> objectMap;

    /**
     * Indicates if the file system is case-insensitive.
     */
    protected int caseInsensitive;
    /**
     * Indicates if the file system case-sensitivity is set
     */
    protected boolean caseInsensitivityComputed;

    /**
     * Indicates whether to keep output files in memory.
     */
    protected final boolean inMemory;

    protected static List<Location> default_locations;

    public ExtFileManager(ExtensionInfo extInfo) {
        super(javaCompiler.getStandardFileManager(null, null, null));
        this.extInfo = extInfo;
        loadedSources = new HashMap<>();
        objectMap = new HashMap<>();
        inMemory = extInfo.getOptions().noOutputToFS;
        List<Location> defaultLocations = extInfo.defaultLocations();
        if (!defaultLocations.equals(default_locations)) {
            default_locations = defaultLocations;
            clearCache();
        }
    }

    protected void clearCache() {
        packageCache.clear();
        nocache.clear();
        zipCache.clear();
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName,
            String relativeName) throws IOException {
        if (inMemory) {
            Map<String, JavaFileObject> locMap = objectMap.get(location);
            if (locMap != null) {
                String key = fileKey(packageName, relativeName);
                JavaFileObject jfo = locMap.get(key);
                if (jfo != null) return jfo;
            }
        }
        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location,
            String className, Kind kind) throws IOException {
        String pkg = StringUtil.getPackageComponent(className);
        String name = StringUtil.getShortNameComponent(className);
        String relativeName = name + kind.extension;
        return (JavaFileObject) getFileForInput(location, pkg, relativeName);
    }

    protected Kind kindFromExtension(String name) {
        Kind k;
        if (name.endsWith(".java"))
            k = Kind.SOURCE;
        else if (name.endsWith(".class"))
            k = Kind.CLASS;
        else if (name.endsWith(".html") || name.endsWith(".htm"))
            k = Kind.HTML;
        else k = Kind.OTHER;
        return k;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName,
            String relativeName, FileObject sibling) throws IOException {
        if (inMemory) {
            String key = fileKey(packageName, relativeName);
            URI src = URI.create("file:///" + key);
            JavaFileObject jfo =
                    new ExtFileObject(src, kindFromExtension(relativeName));
            Map<String, JavaFileObject> locMap = objectMap.get(location);
            if (locMap == null) {
                locMap = new HashMap<>();
                objectMap.put(location, locMap);
            }
            locMap.put(key, jfo);
            return jfo;
        }
        return super.getFileForOutput(location,
                                      packageName,
                                      relativeName,
                                      sibling);
    }

    protected String fileKey(String packageName, String relativeName) {
        if (!packageName.isEmpty())
            packageName =
                    packageName.replace('.', separatorChar) + separatorChar;
        StringBuilder sb = new StringBuilder(packageName);
        sb.append(relativeName);
        return sb.toString();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
            String className, Kind kind, FileObject sibling) throws IOException {

        String pkg = StringUtil.getPackageComponent(className);
        String name = StringUtil.getShortNameComponent(className);
        String relativeName = name + kind.extension;
        return (JavaFileObject) getFileForOutput(location,
                                                 pkg,
                                                 relativeName,
                                                 sibling);
    }

    @Override
    public boolean hasLocation(Location location) {
        return objectMap.get(location) != null || super.hasLocation(location);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof ExtFileObject) {
            String className = ((ExtFileObject) file).getName();
            return className.substring(className.lastIndexOf('.') + 1);
        }
        return super.inferBinaryName(location, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName,
            Set<Kind> kinds, boolean recurse) throws IOException {
        Map<String, JavaFileObject> locMap = objectMap.get(location);
        if (locMap != null)
            return locMap.values();
        else return super.list(location, packageName, kinds, recurse);
    }

    // Use this method for obtaining JavaFileObjects representing files on the
    // local file system
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        return fileManager.getJavaFileObjects(files);
    }

    // Use this method for obtaining JavaFileObjects representing files on the
    // local file system
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(
            String... names) {
        return fileManager.getJavaFileObjects(names);
    }

    // Use this method for obtaining JavaFileObjects representing files on the
    // local file system
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
            Iterable<? extends File> files) {
        return fileManager.getJavaFileObjectsFromFiles(files);
    }

    // Use this method for obtaining JavaFileObjects representing files on the
    // local file system
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
            Iterable<String> names) {
        return fileManager.getJavaFileObjectsFromStrings(names);
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        return fileManager.getLocation(location);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.toUri().equals(b.toUri());
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> path)
            throws IOException {
        fileManager.setLocation(location, path);
    }

    @Override
    public boolean packageExists(String name) {
        if (packageCache.containsKey(name)) return packageCache.get(name);
        boolean exists = false;
        for (int i = default_locations.size() - 1; !exists && i >= 0; i--)
            exists = packageExists(default_locations.get(i), name);
        if (!exists)
            exists = packageExists(extInfo.getOptions().source_path, name);
        packageCache.put(name, exists);
        return exists;
    }

    protected static ZipFile loadZip(File dir) throws IOException {
        Object o = zipCache.get(dir);
        if (o != not_found) {
            ZipFile zip = (ZipFile) o;
            if (zip != null) {
                return zip;
            }
            else {
                // the zip is not in the cache.
                // try to get it.
                if (!dir.exists()) {
                    // record that the file does not exist,
                    zipCache.put(dir, not_found);
                }
                else {
                    // get the zip and put it in the cache.
                    if (Report.should_report(verbose, 2))
                        Report.report(2, "Opening zip " + dir);
                    if (dir.getName().endsWith(".jar")) {
                        zip = new JarFile(dir);
                    }
                    else {
                        zip = new ZipFile(dir);
                    }
                    zipCache.put(dir, zip);

                    // Load the package cache
                    Enumeration<? extends ZipEntry> i = zip.entries();
                    while (i.hasMoreElements()) {
                        ZipEntry ei = i.nextElement();
                        String n = ei.getName();
                        int index = n.indexOf('/');
                        while (index >= 0) {
                            packageCache.put(n.substring(0, index), true);
                            index = n.indexOf('/', index + 1);
                        }
                    }
                    return zip;
                }
            }
        }
        return null;
    }

    @Override
    public boolean packageExists(Location location, String name) {
        Iterable<? extends File> files = getLocation(location);
        if (files == null) return false;
        for (File f : files) {
            String fileName = f.getName();
            if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
                String entryName = name.replace('.', '/');
                try {
                    loadZip(f);
                }
                catch (IOException e) {
                    throw new InternalCompilerError(e);
                }
                Boolean contains = packageCache.get(entryName);
                if (contains != null && contains) return true;
            }
            else {
                String filePath = name.replace('.', File.separatorChar);
                File newFile = new File(f, filePath);
                try {
                    if (newFile.exists() && newFile.isDirectory()
                            && newFile.getCanonicalPath().endsWith(filePath))
                        return true;
                }
                catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public ClassFile loadFile(String name) {
        if (nocache.contains(name)) return null;
        ClassFile clazz = null;
        for (int i = default_locations.size() - 1; i >= 0; i--) {
            clazz = loadFile(default_locations.get(i), name);
            if (clazz != null) break;
        }
        if (clazz == null) nocache.add(name);
        return clazz;
    }

    @Override
    public ClassFile loadFile(Location location, String name) {
        if (Report.should_report(report_topics, 3)) {
            Report.report(3, "looking in " + location + " for " + name);
        }
        if (Report.should_report(report_topics, 4)) {
            Report.report(4, "Location " + location + " has "
                    + getLocation(location));
        }

        try {
            JavaFileObject jfo = null;
            try {
                jfo = getJavaFileForInput(location, name, Kind.CLASS);
            }
            catch (IOException e) {
                throw new InternalCompilerError("Error while checking for class file "
                                                        + name,
                                                e);
            }
            if (jfo != null) {
                if (Report.should_report(report_topics, 4)) {
                    Report.report(4, "Class " + name + " found in " + location
                            + " at " + jfo.toUri());
                }
            }
            else {
                if (Report.should_report(report_topics, 4)) {
                    Report.report(4, "Class " + name + " not found in "
                            + location);
                }
            }

            if (jfo != null) {
                return extInfo.createClassFile(jfo, getBytes(jfo));
            }
        }
        catch (ClassFormatError e) {
            if (Report.should_report(report_topics, 4))
                Report.report(4, "Class " + name + " format error");
        }
        catch (IOException e) {
            if (Report.should_report(report_topics, 4))
                Report.report(4, "Error loading class " + name);
        }
        return null;
    }

    @Override
    public FileSource fileSource(String fileName) throws IOException {
        return fileSource(extInfo.getOptions().source_path,
                          fileName,
                          Source.Kind.DEPENDENCY);
    }

    @Deprecated
    @Override
    public FileSource fileSource(String fileName, boolean userSpecified)
            throws IOException {
        return fileSource(fileName, userSpecified
                ? Source.Kind.USER_SPECIFIED : Source.Kind.DEPENDENCY);
    }

    @Override
    public FileSource fileSource(String fileName,
            polyglot.frontend.Source.Kind kind) throws IOException {
        return fileSource(extInfo.getOptions().source_path, fileName, kind);
    }

    @Override
    public FileSource fileSource(Location location, String fileName)
            throws IOException {
        return fileSource(location, fileName, Source.Kind.DEPENDENCY);
    }

    @Deprecated
    @Override
    public FileSource fileSource(Location location, String fileName,
            boolean userSpecified) throws IOException {
        return fileSource(location, fileName, userSpecified
                ? Source.Kind.USER_SPECIFIED : Source.Kind.DEPENDENCY);
    }

    @Override
    public FileSource fileSource(Location location, String fileName,
            polyglot.frontend.Source.Kind kind) throws IOException {
        File f = new File(fileName);
        FileSource sourceFile;
        FileObject fo = null;
        String key;
        if (kind == Source.Kind.USER_SPECIFIED) {
            key = fileName;
            f = f.getAbsoluteFile();
            sourceFile = loadedSources.get(key);
            if (sourceFile != null) return sourceFile;
            for (FileObject jfo : fileManager.getJavaFileObjects(f)) {
                if (fo != null)
                    throw new InternalCompilerError("Two files exist of the same name");
                fo = jfo;
            }
        }
        else {
            if (f.isAbsolute())
                throw new InternalCompilerError("Expected relative filename");

            key = fileKey(location, "", fileName);
            sourceFile = loadedSources.get(key);
            if (sourceFile != null) return sourceFile;
            fo = getFileForInput(location, "", fileName);
            if (fo == null)
                throw new FileNotFoundException("File: " + fileName
                        + " not found.");
        }
        sourceFile = extInfo.createFileSource(fo, kind);
        String[] exts = extInfo.fileExtensions();
        boolean ok = false;

        for (String ext : exts) {
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
                }
                else if (exts.length != 1 && i == exts.length - 1) {
                    extString += ", or ";
                }
                else if (i != 0) {
                    extString += ", ";
                }
                extString = extString + "\"." + exts[i] + "\"";
            }

            if (exts.length == 1) {
                throw new IOException("Source \"" + fileName
                        + "\" does not have the extension " + extString + ".");
            }
            else {
                throw new IOException("Source \"" + fileName
                        + "\" does not have any of the extensions " + extString
                        + ".");
            }
        }

        if (Report.should_report(Report.loader, 2))
            Report.report(2, "Loading class from " + sourceFile);

        if (sourceFile != null) {
            sourceFile.setKind(kind);
            loadedSources.put(key, sourceFile);
        }

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
            if (source != null) return source;
            int dot = name.lastIndexOf('.');
            if (dot == -1)
                done = true;
            else name = name.substring(0, dot);
        }
        return null;
    }

    /** Load the source file for the given class name using the source path. */
    protected FileSource checkForSource(Location location, String className) {
        /* Search the source path. */
        String[] exts = extInfo.fileExtensions();
        for (String ext : exts) {
            String pkgName = StringUtil.getPackageComponent(className);
            String shortName = StringUtil.getShortNameComponent(className);
            String fileName = shortName + "." + ext;

            String key = fileKey(location, pkgName, fileName);
            FileSource source = loadedSources.get(key);
            // Skip it if already loaded
            if (source != null) {
                return source;
            }

            FileObject fo;
            try {
                fo = getFileForInput(location, pkgName, fileName);
            }
            catch (IOException e1) {
                return null;
            }
            if (fo == null) continue;

            try {
                source = extInfo.createFileSource(fo, Source.Kind.DEPENDENCY);
                if (Report.should_report(Report.loader, 2))
                    Report.report(2, "Loading " + className + " from " + source);

                loadedSources.put(key, source);
                return source;
            }
            catch (IOException e) {
            }
        }
        return null;
    }

    protected String fileKey(Location location, String packageName,
            String fileName) {
        if (caseInsensitive())
            return location + "/" + packageName.toLowerCase() + "/"
                    + fileName.toLowerCase();
        return location + "/" + packageName + "/" + fileName;
    }

    @Override
    public boolean caseInsensitive() {
        if (!caseInsensitivityComputed) {
            setCaseInsensitive(System.getProperty("user.dir"));
            if (caseInsensitive == 0)
                throw new InternalCompilerError("unknown case sensitivity");
            caseInsensitivityComputed = true;
        }
        return caseInsensitive == 1;
    }

    private void setCaseInsensitive(String fileName) {
        if (caseInsensitive != 0) {
            return;
        }

        // File.equals doesn't work correctly on the Mac.
        // So, get the list of files in the same directory
        // as sourceFile. Check if the sourceFile with two
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
                for (File element : ls) {
                    if (f1.equals(element)) {
                        f1Exists = true;
                    }
                    if (f2.equals(element)) {
                        f2Exists = true;
                    }
                }
            }
            else {
                // dir not found
            }

            if (!f1Exists || !f2Exists) {
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

    /**
     * Convenience method for extracting bytes from a FileObject
     */
    public static byte[] getBytes(FileObject fo) throws IOException {
        InputStream is = fo.openInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[2048];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    protected static Collection<String> verbose;

    static {
        verbose = new HashSet<>();
        verbose.add("filemanager");
    }
}
