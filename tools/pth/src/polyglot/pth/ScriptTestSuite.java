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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 *
 */
public class ScriptTestSuite extends TestSuite<Test> {
    protected TestFactory tf;
    protected File scriptFile;
    protected boolean saveProblem = false;
    private boolean scriptLoaded = false;

    public ScriptTestSuite(TestFactory tf, String scriptFilename) {
        this(tf, new File(scriptFilename));
    }

    public ScriptTestSuite(TestFactory tf, File scriptFile) {
        super(scriptFile.getName());
        this.tf = tf;
        this.scriptFile = scriptFile;
        loadResults();
    }

    protected boolean loadScript() {
        if (scriptLoaded) return true;
        scriptLoaded = true;
        if (!scriptFile.exists()) {
            appendFailureMessage("File " + scriptFile.getName()
                    + " not found.");
            return false;
        }
        else if (!parseScript()) return false;
        return true;
    }

    @Override
    protected boolean runTest() {
        saveProblem = false;
        if (!loadScript()) return false;

        setOutputController(output);
        setPDFReporter(pdfReporter);
        return super.runTest();
    }

    @Override
    protected void postIndividualTest() {
        if (!saveProblem) saveProblem = !saveResults();
    }

    @Override
    protected void postRun() {
        super.postRun();
        saveResults();
    }

    protected boolean parseScript() {
        Grm grm = tf.Grm(scriptFile);
        try {
            List<Test> value = grm.parse().<List<Test>> value();
            tests = value;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            // Used by CUP to indicate an unrecoverable error.
            e.printStackTrace(System.out);
            appendFailureMessage("Parsing error: " + e.getMessage());
            return false;
        }
        return true;
    }

    protected void loadResults() {
        try (ObjectInputStream ois =
                new ObjectInputStream(new FileInputStream(TestSuiteResult.getResultsFileName(scriptFile)))) {
            TestResult tr = (TestResult) ois.readObject();
            setTestResult(tr);
        }
        catch (FileNotFoundException e) {
            // ignore, and fail silently
        }
        catch (ClassNotFoundException | IOException e) {
            System.err.println("Unable to load results for test suite "
                    + getName() + ": " + e.getMessage());
        }
    }

    protected boolean saveResults() {
        try (ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(TestSuiteResult.getResultsFileName(scriptFile)))) {
            oos.writeObject(getTestSuiteResult());
        }
        catch (IOException e) {
            System.err.println("Unable to save results for test suite "
                    + getName());
            return false;
        }
        return true;
    }

    @Override
    public List<Test> getTests() {
        loadScript();
        return super.getTests();
    }

}
