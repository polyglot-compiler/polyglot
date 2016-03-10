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
package ppg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import ppg.lex.Lexer;
import ppg.parse.Parser;
import ppg.spec.CUPSpec;
import ppg.spec.Spec;
import ppg.util.CodeWriter;

public class PPG {
    public static final String HEADER = "ppg: ";
    public static final String DEBUG_HEADER = "ppg [debug]: ";
    public static boolean debug = false;
    public static String SYMBOL_CLASS_NAME = "sym";
    public static String OUTPUT_FILE = null;

    public static void DEBUG(String s) {
        if (debug) System.err.println(DEBUG_HEADER + s);
    }

    public static void main(String args[]) {
        String filename = null;

        try {
            for (int i = 0; i < args.length; i++) {
                // assume all switches begin with a dash '-'
                if (args[i].charAt(0) == '-') {
                    if (args[i].equals("-symbols")) {
                        if (args.length > i)
                            SYMBOL_CLASS_NAME = args[++i];
                        else throw new Exception("No filename specified after -symbols");
                    }
                    else if (args[i].equals("-o")) {
                        if (args.length > i)
                            OUTPUT_FILE = args[++i];
                        else throw new Exception("No filename specified after -o");
                    }
                    else // invalid switch
                        throw new Exception("Invalid switch: " + args[i]);
                }
                else {
                    // not a switch: this must be a filename
                    // but only do the 1st filename on the command line
                    if (filename == null)
                        filename = args[i];
                    else throw new Exception("Error: multiple source files specified.");
                }
            }
        }
        catch (Exception e) {
            System.err.println(HEADER + e.getMessage());
            usage();
        }

        if (filename == null) {
            System.err.println("Error: no filename specified.");
            usage();
        }

        try (FileReader fileReader = new FileReader(filename)) {
            Lexer lex = new Lexer(fileReader, filename);

            Parser parser = new Parser(filename, lex);
            try {
                parser.parse();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                // Used by CUP to indicate an unrecoverable error.
                System.err.println(HEADER + "Exception: " + e.getMessage());
                System.exit(1);
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("Error: " + filename + " is not found.");
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println(HEADER + "Exception: " + e.getMessage());
            System.exit(1);
        }

        Spec spec = (Spec) Parser.getProgramNode();

        File file = new File(filename);
        String parent = file.getParent();
        spec.parseChain(parent == null ? "" : parent);

        PrintStream out = System.out;

        /* now we have a linked list of inheritance, namely
         * PPG_1, PPG_2, ..., PPG_n, CUP
         * We combine two at a time, starting from the end with the CUP spec
         */
        try {
            if (OUTPUT_FILE != null) {
                out = new PrintStream(new FileOutputStream(OUTPUT_FILE));
            }

            CUPSpec combined = spec.coalesce();
            CodeWriter cw = new CodeWriter(out, 80);
            combined.unparse(cw);
            cw.flush();
        }
        catch (PPGError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println(HEADER + "Exception: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void usage() {
        System.err.println("Usage: ppg [-symbols ConstClass] <input file>\nwhere:\n"
                + "\t-c <Class>\tclass prepended to token names to pass to <func>\n"
                + "\t<input>\ta PPG or CUP source file\n");
        System.exit(1);
    }
}
