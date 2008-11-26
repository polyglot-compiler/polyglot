/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 1997-2001 Purdue Research Foundation
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types.reflect;

import polyglot.main.Report;

import java.io.*;
import java.util.*;

/**
 * Polyglot has its own class loader just so
 * the classpath can be specified on the command line.
 */
public class ClassPathLoader
{
    protected List classpath;
    protected ClassFileLoader loader;

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

    public boolean packageExists(String name) {
	for (Iterator i = classpath.iterator(); i.hasNext(); ) {
	    File dir = (File) i.next();
            if (loader.packageExists(dir, name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Load a class from the classpath. If the class is not found, then
     * <code>null</code> is returned.
     */
    public ClassFile loadClass(String name) {
        if (Report.should_report(verbose, 2)) {
	    Report.report(2, "attempting to load class " + name);
	    Report.report(2, "classpath = " + classpath);
	}

	for (Iterator i = classpath.iterator(); i.hasNext(); ) {
	    File dir = (File) i.next();
            ClassFile cf = loader.loadClass(dir, name); 
            if (cf != null) {
                return cf;
            }
        }

        return null;
    }

    protected static Collection verbose;

    static {
        verbose = new HashSet();
        verbose.add("loader");
    }
}
