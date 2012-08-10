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

package polyglot.util.typedump;

import polyglot.frontend.ExtensionInfo;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.OptimalCodeWriter;

public class Main {
    public static void main(String args[]) {
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
            System.err.println("Extension " + extension
                    + " not found: could not find class " + extClassName + ".");
            System.exit(1);
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
            TypeSystem ts = extInfo.typeSystem();
            TypeDumper t = TypeDumper.load(className, ts);

            CodeWriter cw = new OptimalCodeWriter(System.out, 72);

            t.dump(cw);
            cw.newline(0);

            try {
                cw.flush();
            }
            catch (java.io.IOException exn) {
                System.err.println(exn.getMessage());
            }
        }
        catch (java.io.IOException exn) {
            System.err.println("IO errors.");
            System.err.println(exn.getMessage());
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
}
