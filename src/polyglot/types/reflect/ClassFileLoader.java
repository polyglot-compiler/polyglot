package jltools.types.reflect;

import jltools.main.Report;
import jltools.types.*;

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

    public ClassFileLoader() {
        this.cache = new HashMap();
    }

    /**
     * Load a class from the classpath.
     */
    public ClassFile loadClass(File dir, String name)
        throws ClassNotFoundException
    {
        Report.report(verbose, 2, "attempting to load class " + name +
                      " from " + dir);

        String key = dir.toString() + "/" + name;

        ClassFile c = (ClassFile) cache.get(key);

        if (c == null) {
            c = findClass(dir, name);

            // We cache here since more than one type system may attempt
            // to load the same class file.  But, we use a weak hash map
            // to allow garbage collection of ClassFiles when we need it.

            cache.put(key, c);
        }
        else {
            Report.report(verbose, 3, "already loaded " + c.name());
        }

        Report.report(verbose, 1, "loaded class " + c.name());

        return c;
    }

    protected ClassFile findClass(File dir, String name) throws ClassNotFoundException {
	String fileName = name.replace('.', File.separatorChar) + ".class";
	String entryName = name.replace('.', '/') + ".class";

        Report.report(verbose, 3, "looking in " + dir + " for " + fileName);

        try {
            if (dir.isDirectory()) {
                File file = new File(dir, fileName);

                if (file.exists()) {
                    Report.report(verbose, 3, "found " + file);
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
                    Report.report(verbose, 3, "found jar entry " + entry);
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
                    Report.report(verbose, 3, "found zip entry " + entry);
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

        throw new ClassNotFoundException(name);
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
            Report.report(verbose, 3, "defining class " + name);
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
