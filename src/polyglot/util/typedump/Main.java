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

package polyglot.util.typedump;

import java.util.HashSet;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;
import polyglot.main.UsageError;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.InternalCompilerError;
import polyglot.util.OptimalCodeWriter;
import polyglot.util.StdErrorQueue;

public class Main {
    public static void main(String args[]) throws IllegalArgumentException,
            SemanticException {
        String extension = "jl";
        String className;
        if (args.length == 3 && args[0].equals("-ext")) extension = args[1];
        if ((extension == null && args.length != 1)
                || (extension != null && args.length != 3)) {
            System.err.println("Usage: " + "polyglot.util.typedump.Main "
                    + "[-ext <extension>] <classname>");
            System.exit(1);
        }

        if (extension == null)
            className = args[0];
        else className = args[2];

        ExtensionInfo extInfo = null;

        String extClassName = "polyglot.ext." + extension + ".ExtensionInfo";
        Class<?> extClass = null;

        try {
            extClass = Class.forName(extClassName);
        }
        catch (ClassNotFoundException e) {
            try {
                extClass = Class.forName(extension);
            }
            catch (ClassNotFoundException e2) {
                System.err.println("Extension " + extension
                        + " not found: could not find class " + extClassName
                        + ".");
                System.err.println(e2.getMessage());
                System.exit(1);
            }
        }

        try {
            extInfo = (ExtensionInfo) extClass.newInstance();
        }
        catch (Exception e) {
            System.err.println("Extension " + extension
                    + " could not be loaded: " + "could not instantiate "
                    + extClassName + ".");
            System.exit(1);
        }

        try {
            // now try to establish the type system correctly.
            Options options = extInfo.getOptions();

            Options.global = options;

            configureOptions(options);

            StdErrorQueue eq =
                    new StdErrorQueue(System.err, 100, extInfo.compilerName());

            new Compiler(extInfo, eq);

            TypeSystem ts = extInfo.typeSystem();
            TypeDumper t = TypeDumper.load(className, ts, extInfo.version());

            CodeWriter cw = new OptimalCodeWriter(System.out, 100);

            t.dump(cw);
            cw.newline(0);

            try {
                cw.flush();
            }
            catch (java.io.IOException exn) {
                System.err.println(exn.getMessage());
            }
        }
        catch (ClassNotFoundException exn) {
            System.err.println("Could not load .class: " + className);
            System.err.println(exn.getMessage());
        }
        catch (NoSuchFieldException exn) {
            System.err.println("Could not reflect jlc fields");
            System.err.println(exn.getMessage());
        }
        catch (SecurityException exn) {
            System.err.println("Security policy error.");
            System.err.println(exn.getMessage());
        }
    }

    private static void configureOptions(Options options) {
        try {
            options.parseCommandLine(new String[] { "-d", ".", "Dummy" },
                                     new HashSet<String>());
        }
        catch (UsageError e) {
            throw new InternalCompilerError(e);
        }
//        options.setSourceOutput(new File("."));
//        options.setClassOutput(new File("."));

    }
}
