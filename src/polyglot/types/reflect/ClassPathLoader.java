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
public class ClassPathLoader
{
    List classpath;
    ClassFileLoader loader;

    public ClassPathLoader(List classpath, ClassFileLoader loader) {
        this.classpath = new ArrayList(classpath);
        this.loader = loader;
    }

    public ClassPathLoader(String classpath, ClassFileLoader loader) {
        this.classpath = new ArrayList();

        StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);

        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            this.classpath.add(new File(s));
        }

        this.loader = loader;
    }

    public String classpath() {
        return classpath.toString();
    }

    /**
     * Load a class from the classpath.
     */
    public ClassFile loadClass(String name) throws ClassNotFoundException {
        Report.report(verbose, 1, "attempting to load class " + name);
        Report.report(verbose, 2, "classpath = " + classpath);

	for (Iterator i = classpath.iterator(); i.hasNext(); ) {
	    File dir = (File) i.next();

            try {
                return loader.loadClass(dir, name);
            }
            catch (ClassNotFoundException e) {
            }
        }

        throw new ClassNotFoundException(name);
    }

    static Collection verbose;

    static {
        verbose = new HashSet();
        verbose.add("loader");
    }
}
