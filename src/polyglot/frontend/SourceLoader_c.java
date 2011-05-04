/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
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

package polyglot.frontend;

import java.io.*;
import java.util.*;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileManager.Location;

import polyglot.main.Report;
import polyglot.util.InternalCompilerError;
import polyglot.util.StringUtil;

/** A <code>SourceLoader</code> is responsible for loading source files. */
public class SourceLoader_c implements SourceLoader
{
    protected ExtensionInfo sourceExt;
    protected JavaFileManager.Location sourcePath;

    /** Set of sources already loaded.  An attempt to load a source
      * already loaded will cause an IOException. */
    protected Map<Object,FileSource> loadedSources;

    public SourceLoader_c(ExtensionInfo sourceExt, JavaFileManager.Location sourcePath) {
		this.sourcePath = sourcePath;
		this.sourceExt = sourceExt;
        this.loadedSources = new HashMap<Object,FileSource>();
    }

    public FileSource fileSource(String fileName) throws IOException {
        return fileSource(fileName, false);
    }

    public FileSource fileSource(String fileName, boolean userSpecified) throws IOException {
        StandardJavaFileManager jf_mgr = sourceExt.fileManager();
		Iterable<? extends JavaFileObject> jfos = jf_mgr
				.getJavaFileObjects(fileName);
		JavaFileObject jfo = null;
		for(JavaFileObject f : jfos) {
			if(jfo != null)
				throw new InternalCompilerError(
						"File manager returned multiple file objects for "
								+ fileName);
			jfo = f;
		}
		
		FileSource sourceFile = loadedSources.get(fileKey(jfo));
		if(sourceFile != null)
			return sourceFile;

		sourceFile = sourceExt.createFileSource(jfo, userSpecified);
		
        String[] exts = sourceExt.fileExtensions();
        boolean ok = false;
        
        for (int i = 0; i < exts.length; i++) {
            String ext = exts[i];
            
            if (fileName.endsWith("." + ext)) {
                ok = true;
                break;
            }
        }
        
        if (! ok) {
            String extString = "";
            
            for (int i = 0; i < exts.length; i++) {
                if (exts.length == 2 && i == exts.length-1) {
                    extString += " or ";
                }
                else if (exts.length != 1 && i == exts.length-1) {
                    extString += ", or ";
                }
                else if (i != 0) {
                    extString += ", ";
                }
                extString = extString + "\"." + exts[i] + "\"";
            }
            
            if (exts.length == 1) {
                throw new IOException("Source \"" + fileName +
                                      "\" does not have the extension "
                                      + extString + ".");
            }
            else {
                throw new IOException("Source \"" + fileName +
                                      "\" does not have any of the extensions "
                                      + extString + ".");
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
        
		loadedSources.put(fileKey(jfo), sourceFile);
        return sourceFile;
    }

    public FileSource classSource(String className) {
    	String name = className;
    	boolean done = false;
    	while (!done) {
    		FileSource source = checkForSource(name);
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
    protected FileSource checkForSource(String className) {
    	/* Search the source path. */
        String[] exts = sourceExt.fileExtensions();
        JavaFileManager file_mgr = sourceExt.fileManager();
        for (int k = 0; k < exts.length; k++) {
        	String pkgName = StringUtil.getPackageComponent(className);
            String shortName = StringUtil.getShortNameComponent(className);
            String fileName = shortName + "." + exts[k];

            FileObject fo;
			try {
				fo = file_mgr.getFileForInput(sourcePath, pkgName, fileName);
			} catch (IOException e1) {
				return null;
			}
            if(fo == null)
            	continue;
            
            FileSource source = (FileSource) loadedSources.get(fileKey(fo));
            // Skip it if already loaded
            if (source != null) {
                return source;
            }

            try {
            	source = sourceExt.createFileSource(fo, false);
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
    	return fo;
	}
}
