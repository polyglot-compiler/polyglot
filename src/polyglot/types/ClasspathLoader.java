package jltools.types;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

/**
 * We implement our own class loader.  All this pain is so (1) we can define
 * the classpath on the command line and (2) we can avoid
 * having to re-compile source files if the type system can 
 * construct a type directly from a Class object.  But, we need to check
 * if the source is here. If the .class file
 * is f

 */
public class ClasspathLoader extends ClassLoader
{
    List classpath;

    public ClasspathLoader(List classpath) {
        this.classpath = classpath;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
	String fileName = name.replace('.', File.separatorChar) + ".class";

	for (Iterator i = classpath.iterator(); i.hasNext(); ) {
	    File directory = (File) i.next();

	    if (directory.isDirectory()) {
		File file = new File(directory, fileName);

		if (file.exists()) {
		}
	    }
	    else if (directory.getName().endsWith(".jar")) {
	    }
	    else if (directory.getName().endsWith(".zip")) {
	    }
	}

	// If all else fails, load from the system class loader.
	return Class.forName(name);
    }
}
