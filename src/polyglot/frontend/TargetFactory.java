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

import polyglot.main.Options;
import polyglot.main.Report;
import polyglot.types.*;
import polyglot.util.*;

import java.io.*;
import java.util.*;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/** A <code>TargetFactory</code> is responsible for opening output files. */
public class TargetFactory
{
    protected File outputDirectory;
    protected String outputExtension;
    protected boolean outputStdout;
    protected JavaFileManager fm;

    public TargetFactory(File outDir, String outExt, boolean so) {
	outputDirectory = outDir;
	outputExtension = outExt;
	outputStdout = so;
    }
    
    public TargetFactory(JavaFileManager fm, File outDir, String outExt, boolean so) {
    	this(outDir, outExt, so);
    	this.fm = fm;
    }

    /** Open a writer to the output file for the class in the given package. */
    public Writer outputWriter(String packageName, String className,
	    Source source) throws IOException 
    {
	return outputWriter(outputFile(packageName, className, source));
    }

    public CodeWriter outputCodeWriter(File f, int width) throws IOException {
    	Writer w = outputWriter(f);
        return Compiler.createCodeWriter(w, width);
    }

    public CodeWriter outputCodeWriter(JavaFileObject f, int width) throws IOException {
    	return Compiler.createCodeWriter(f.openWriter(), width);
    }
    
    /** Open a writer to the output file. */
    public Writer outputWriter(File outputFile) throws IOException {
	if (Report.should_report(Report.frontend, 2))
	    Report.report(2, "Opening " + outputFile + " for output.");

	if (outputStdout) {
	    return new UnicodeWriter(new PrintWriter(System.out));
	}

	if (! outputFile.getParentFile().exists()) {
	    File parent = outputFile.getParentFile();
	    parent.mkdirs();
	}

	return new UnicodeWriter(new FileWriter(outputFile));
    }

    /** Return a file object for the output of the source file in the given package. */
    public File outputFile(String packageName, Source source) {
	String name;
	name = new File(source.name()).getName();
	name = name.substring(0, name.lastIndexOf('.'));
	return outputFile(packageName, name, source);
    }
    
    /** Return a file object for the output of the class in the given package. */
    public File outputFile(String packageName, String className, Source source)
    {
	if (outputDirectory == null) {
	      throw new InternalCompilerError("Output directory not set.");
	}

	if (packageName == null) {
	    packageName = "";
	}

	File outputFile = new File(outputDirectory,
				   packageName.replace('.', File.separatorChar)
				   + File.separatorChar
				   + className
				   + "." + outputExtension);

        if (source != null && outputFile.getPath().equals(source.path())) {
	    throw new InternalCompilerError("The output file is the same as the source file");
	}
	
	return outputFile;
    }
    
    public JavaFileObject outputFileObject(String packageName, Source source) {
    	String name = source.name;
		name = name.substring(0, name.lastIndexOf('.'));
		return outputFileObject(packageName, name, source);
    }
    
    public JavaFileObject outputFileObject(String packageName, String className, Source source) {
    	if(packageName == null)
    		packageName = "";
    	try {
			return fm.getJavaFileForOutput(null, packageName + "." + className, Kind.SOURCE, null);
		} catch (IOException e) {
			throw new InternalCompilerError("Error creating output file object for " + source, e);
		}
    }
    
}
