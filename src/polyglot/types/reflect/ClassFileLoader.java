package polyglot.types.reflect;

import polyglot.main.Report;

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
    /**
     * Keep a cache of the zips and jars so we don't have to keep 
     * opening them from the file system. 
     */
    Map jarCache;
    
    final static Object not_found = new Object();

    public ClassFileLoader() {
        this.jarCache = new HashMap();
    }

    /**
     * Try to find the class <code>name</code> in the directory or jar or zip 
     * file <code>dir</code>.
     * If the class does not exist in the specified file/directory, then 
     * <code>null</code> is returned.
     */
    public ClassFile loadClass(File dir, String name)
    {
        if (Report.should_report(verbose, 3)) {
	    Report.report(3, "looking in " + dir + " for " + 
                             name.replace('.', File.separatorChar) + ".class");
        }

        try {
            if (dir.isDirectory()) {
                StringBuffer filename = new StringBuffer(name.length() + 8);
                filename.append(name);
                // our own replace, to save a suprising amount of memory
                for (int i = 0; i < filename.length(); i++) {
                    if (filename.charAt(i) == '.') 
                        filename.setCharAt(i, File.separatorChar);
                }
                filename.append(".class");
                
                File file = new File(dir, filename.toString());

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
                Object o = jarCache.get(dir);
                if (o != not_found) {
                    JarFile jar = (JarFile)o;
                    if (jar == null) { 
                        // the jar is not in the cache.
                        // try to get it.
                        if (!dir.exists()) {
                            // record that the file does not exist, 
                            jarCache.put(dir, not_found);
                        }
                        else {
                            // get the jar and put it in the cache.
                            if (Report.should_report(verbose, 2))
                                Report.report(2, "Opening jar " + dir);
                            jar = new JarFile(dir);
                            jarCache.put(dir, jar);                            
                        }
                    }
                    if (jar != null) {
                        String entryName = name.replace('.', '/') + ".class";
                        JarEntry entry = jar.getJarEntry(entryName);
                        if (entry != null) {
                            if (Report.should_report(verbose, 3))
        			Report.report(3, "found jar entry " + entry);
                            InputStream in = jar.getInputStream(entry);
                            ClassFile c = loadFromStream(in, name);
                            in.close();
                            return c;
                        }
                    }
                }
            }
            else if (dir.getName().endsWith(".zip")) {
                Object o = jarCache.get(dir);
                if (o != not_found) {
                    ZipFile zip = (ZipFile)o;
                    if (zip == null) { 
                        // the zip is not in the cache.
                        // try to get it.
                        if (!dir.exists()) {
                            // record that the file does not exist, 
                            jarCache.put(dir, not_found);
                        }
                        else {
                            // get the zip and put it in the cache.
                            if (Report.should_report(verbose, 2))
                                Report.report(2, "Opening zip " + dir);
                            zip = new ZipFile(dir);
                            jarCache.put(dir, zip);                            
                        }
                    }
                    if (zip != null) {
                        String entryName = name.replace('.', '/') + ".class";
                        ZipEntry entry = zip.getEntry(entryName);
                        if (entry != null) {
                            if (Report.should_report(verbose, 3))
                                Report.report(3, "found zip entry " + entry);
                            InputStream in = zip.getInputStream(entry);
                            ClassFile c = loadFromStream(in, name);
                            in.close();
                            return c;
                        }
                    }
                }
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
