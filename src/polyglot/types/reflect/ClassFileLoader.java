package polyglot.types.reflect;

import polyglot.main.Report;
import polyglot.types.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

/**
 * We implement our own class loader.  All this pain is so
 * we can define the classpath on the command line.
 */
public class ClassFileLoader
{
    Map cache;
    final static Object not_found = new Object();

    public ClassFileLoader() {
        this.cache = new HashMap();
    }

    /**
     * Load a class from the classpath.
     */
    public ClassFile loadClass(File dir, String name)
        throws ClassNotFoundException
    {
        if (Report.should_report(verbose, 2))
	    Report.report(2, "attempting to load class " + name +
                      " from " + dir);

        String key = dir.toString() + "/" + name;

        Object o = cache.get(key);

        if (o != not_found) {
            ClassFile c = (ClassFile) o;

            if (c != null) {
                if (Report.should_report(verbose, 3))
		    Report.report(3, "already loaded " + c.name());
                return c;
            }

            c = findClass(dir, name);

            // We cache here since more than one type system may attempt
            // to load the same class file.  But, we use a weak hash map
            // to allow garbage collection of ClassFiles when we need it.
            if (c != null) {
                if (Report.should_report(verbose, 2))
		    Report.report(2, "loaded class " + c.name());
                cache.put(key, c);
                return c;
            }

            cache.put(key, not_found);
        }

        throw new ClassNotFoundException(name);
    }

    protected ClassFile findClass(File dir, String name) throws ClassNotFoundException {
	String fileName = name.replace('.', File.separatorChar) + ".class";
	String entryName = name.replace('.', '/') + ".class";

        if (Report.should_report(verbose, 3))
	    Report.report(3, "looking in " + dir + " for " + fileName);

        try {
            if (dir.isDirectory()) {
                File file = new File(dir, fileName);

                if (file.exists()) {
                    if (Report.should_report(verbose, 3))
			Report.report(3, "found " + file);
                    FileInputStream in = new FileInputStream(file);
                    ClassFile c = loadFromStream(in, name);
                    in.close();
                    return c;
                }
            }
            else if (dir.getName().endsWith(".jar")) {
                JarFile jar = new JarFile(dir);
                JarEntry entry = jar.getJarEntry(entryName);
                if (entry != null) {
                    if (Report.should_report(verbose, 3))
			Report.report(3, "found jar entry " + entry);
                    InputStream in = jar.getInputStream(entry);
                    ClassFile c = loadFromStream(in, name);
                    in.close();
                    return c;
                }
                jar.close();
            }
            else if (dir.getName().endsWith(".zip")) {
                ZipFile zip = new ZipFile(dir);
                ZipEntry entry = zip.getEntry(entryName);
                if (entry != null) {
                    if (Report.should_report(verbose, 3))
			Report.report(3, "found zip entry " + entry);
                    InputStream in = zip.getInputStream(entry);
                    ClassFile c = loadFromStream(in, name);
                    in.close();
                    return c;
                }
                zip.close();
            }
        }
        catch (IOException e) {
        }

        return null;
    }

    /**
     * Load a class from an input stream.
     */
    ClassFile loadFromStream(InputStream in, String name) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[4096];
        int n = 0;

        do {
            n = in.read(buf);
            if (n >= 0) out.write(buf, 0, n);
        } while (n >= 0);

        byte[] bytecode = out.toByteArray();

        try {
            if (Report.should_report(verbose, 3))
		Report.report(3, "defining class " + name);
            return new ClassFile(bytecode);
        }
        catch (ClassFormatError e) {
            throw new IOException(e.getMessage());
        }
    }

    static Collection verbose;

    static {
        verbose = new HashSet();
        verbose.add("loader");
    }
}
