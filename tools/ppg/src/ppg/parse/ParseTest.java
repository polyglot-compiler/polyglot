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
package ppg.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import ppg.lex.Lexer;
import ppg.spec.Spec;
import ppg.util.CodeWriter;

public class ParseTest {
    private static final String HEADER = "ppg [parsetest]: ";

    private ParseTest() {
    }

    public static void main(String args[]) {
        FileReader fileReader;

        String filename = null;
        try {
            filename = args[0];
            fileReader = new FileReader(filename);
        }
        catch (FileNotFoundException e) {
            System.err.println("Error: " + filename + " is not found.");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(HEADER + "Error: No file name given.");
            return;
        }

        File f = new File(filename);
        String simpleName = f.getName();

        Lexer lex = new Lexer(fileReader, simpleName);

        Parser parser = new Parser(filename, lex);
        try {
            parser.parse();
        }
        catch (Exception e) {
            System.err.println(HEADER + "Exception: " + e.getMessage());
            return;
        }
        Spec spec = (Spec) Parser.getProgramNode();

        CodeWriter cw = new CodeWriter(System.out, 72);
        try {
            spec.unparse(cw);
            cw.flush();
            fileReader.close();
        }
        catch (IOException e) {
            System.err.println(HEADER + "exception: " + e.getMessage());
            return;
        }
    }

}
