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

package polyglot.frontend;

import static java.io.File.separatorChar;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import polyglot.filemanager.FileManager;
import polyglot.main.Report;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.UnicodeWriter;

/** A {@code TargetFactory} is responsible for opening output files. */
public class TargetFactory {
    protected FileManager fileManager = null;
    protected JavaFileManager.Location outputLocation = null;
    protected String outputExtension;
    protected boolean outputStdout;

    public TargetFactory(
            FileManager fileManager, Location outputLocation, String outExt, boolean so) {
        this.fileManager = fileManager;
        this.outputLocation = outputLocation;
        this.outputExtension = outExt;
        this.outputStdout = so;
    }

    public CodeWriter outputCodeWriter(FileObject f, int width) throws IOException {
        Writer w = outputWriter(f);
        return Compiler.createCodeWriter(w, width);
    }

    /** Open a writer to the output file. */
    public Writer outputWriter(FileObject outputFile) throws IOException {
        if (Report.should_report(Report.frontend, 2))
            Report.report(2, "Opening " + outputFile + " for output.");

        if (outputStdout) {
            return new UnicodeWriter(new PrintWriter(System.out));
        }

        return new UnicodeWriter(outputFile.openWriter());
    }

    /**
     * Return a file object for the output of the source file in the given
     * package.
     */
    public JavaFileObject outputFileObject(String packageName, Source source) {
        String name;
        name = source.name();
        name = name.substring(0, name.lastIndexOf('.'));
        int lastIndex = name.lastIndexOf(separatorChar);
        name = lastIndex >= 0 ? name.substring(lastIndex + 1) : name;
        return outputFileObject(packageName, name, source);
    }

    /** Return a file object for the output of the class in the given package. */
    public JavaFileObject outputFileObject(String packageName, String className, Source source) {
        if (outputLocation == null) {
            throw new InternalCompilerError("Output location not set.");
        }

        try {
            JavaFileObject outputFile;
            if (outputExtension.equals("java")) {
                outputFile =
                        fileManager.getJavaFileForOutput(
                                outputLocation,
                                !"".equals(packageName) ? packageName + "." + className : className,
                                Kind.SOURCE,
                                null);
            } else {
                outputFile =
                        (JavaFileObject)
                                fileManager.getFileForOutput(
                                        outputLocation,
                                        packageName,
                                        className + "." + outputExtension,
                                        null);
            }

            if (source != null
                    && !source.compilerGenerated()
                    && fileManager.isSameFile(source, outputFile)) {
                throw new InternalCompilerError("The output file is the same as the source file");
            }
            return outputFile;
        } catch (IOException e) {
            throw new InternalCompilerError("Error creating output file for " + source, e);
        }
    }
}
