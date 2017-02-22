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
package polyglot.pth;

import polyglot.pth.polyglot.PolyglotTestFactory;

/**
 *  Main program for the Polyglot Test Harness
 */
public class Main {
    public static void main(String[] args) {
        boolean okay = new Main().start(args);
        if (options.nonzeroExitCodeOnFailedTests && !okay) System.exit(1);
    }

    public static Options options;

    public boolean start(String[] args) {
        options = new Options();
        try {
            options.parseCommandLine(args);
        }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (options.inputFilenames.isEmpty()) {
            System.err.println("Need at least one script file.");
            System.exit(1);
        }

        OutputController outCtrl = createOutputController(options);

        // TODO refactor
        TestFactory tf = TestFactory();
        PDFReporter pdfReporter = null;
        if (options.pdffilename != null)
            pdfReporter = new PDFReporter(options.pdffilename);
        boolean okay = true;
        for (String filename : options.inputFilenames) {
            ScriptTestSuite t = new ScriptTestSuite(tf, filename);
            t.setOutputController(outCtrl);
            t.setPDFReporter(pdfReporter);
            if (options.showResultsOnly)
                t.displayTestResult(outCtrl);
            else okay = okay && t.run();
        }
        if (pdfReporter != null) pdfReporter.flush();
        return okay;
    }

    /**
     * Factory method for creating a test factory.
     */
    protected TestFactory TestFactory() {
        return new PolyglotTestFactory();
    }

    protected OutputController createOutputController(Options options) {
        switch (options.verbosity) {
        // More output controllers should be written, for varying degrees
        // of detail.
        case 0:
            return new SilentOutputController(System.out);
        case 1:
            return new QuietOutputController(System.out,
                                             false,
                                             true,
                                             false,
                                             false,
                                             false);
        case 2:
            return new QuietOutputController(System.out,
                                             false,
                                             true,
                                             false,
                                             false,
                                             true);
        case 3:
            return new QuietOutputController(System.out,
                                             true,
                                             true,
                                             false,
                                             false,
                                             true);
//        case 8:
//            return new VerboseOutputController(System.out, false);
//        case 9:
//            return new VerboseOutputController(System.out, true);

        default:
            return new StdOutputController(System.out);
        }
    }

}
