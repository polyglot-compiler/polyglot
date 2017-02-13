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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TestSuite<T extends Test> extends AbstractTest {
    protected List<T> tests;
    protected boolean haltOnFirstFailure = false;
    protected int totalTests = -1;
    protected int executedTests = 0;
    protected int successfulTests = 0;

    public TestSuite(String name) {
        this(name, new ArrayList<T>(), false);
    }

    public TestSuite(String name, List<T> tests) {
        this(name, tests, false);
    }

    public TestSuite(String name, List<T> tests, boolean haltOnFirstFailure) {
        super(name);
        this.tests = tests;
        this.haltOnFirstFailure = haltOnFirstFailure;
    }

    public boolean getHaltOnFirstFailure() {
        return haltOnFirstFailure;
    }

    @Override
    public void setOutputController(OutputController output) {
        super.setOutputController(output);
        for (Test t : tests)
            t.setOutputController(output);
    }

    @Override
    public void setPDFReporter(PDFReporter pdfReporter) {
        super.setPDFReporter(pdfReporter);
        for (Test t : tests)
            t.setPDFReporter(pdfReporter);
    }

    @Override
    protected boolean runTest() {
        boolean okay = true;

        if (getTestSuiteResult() == null) setTestResult(createTestResult(null));

        Map<String, TestResult> testResults = getTestSuiteResult().testResults;
        Map<String, TestResult> oldTestResults = new HashMap<>(testResults);
        Map<String, TestResult> newResults = new HashMap<>();

        boolean shouldExecute = true;
        for (Test t : tests) {
            TestResult tr = oldTestResults.get(t.getUniqueId());
            if (shouldExecute && t.shouldExecute(tr)) {
                if (tr != null) t.setTestResult(tr);
                boolean result = t.run();
                okay = okay && result;

                tr = t.getTestResult();

                executedTests += t.getExecutedTestCount();
                successfulTests += t.getSuccessfulTestCount();
                postIndividualTest();
                if (!result && (t.haltOnFailure() || haltOnFirstFailure))
                    shouldExecute = false;
            }
            newResults.put(t.getUniqueId(), tr);
        }
        testResults.clear();
        testResults.putAll(newResults);
        return okay;
    }

    @Override
    protected boolean matchFilter() {
        return true;
    }

    protected void postIndividualTest() {
    }

    @Override
    public int getTotalTestCount() {
        if (totalTests == -1) {
            totalTests = 0;
            for (Test t : tests)
                totalTests += t.getTotalTestCount();
        }
        return totalTests;
    }

    @Override
    public int getExecutedTestCount() {
        return executedTests;
    }

    @Override
    public int getSuccessfulTestCount() {
        return successfulTests;
    }

    public int getFailedTestCount() {
        return totalTests - successfulTests;
    }

    protected TestSuiteResult getTestSuiteResult() {
        return (TestSuiteResult) getTestResult();
    }

    @Override
    public int[] displayTestResult(OutputController outCtrl) {
        String suiteName = getName();
        TestSuiteResult tsr = getTestSuiteResult();

        if (tsr == null || tsr.testResults.isEmpty()) {
            outCtrl.printNoTestResults(suiteName);
            return new int[] { 0, 0, 0, 0 };
        }

        outCtrl.printTestSuiteHeader(tsr);

        int total = 0;
        int grandTotal = 0;
        int lastSuccess = 0;
        int neverRun = 0;
        int neverSuccess = 0;
        for (Test t : getTests()) {
            String testId = t.getUniqueId();
            TestResult tr = tsr.testResults.get(testId);
            if (t.shouldExecute(tr)) {
                t.setTestResult(tr);
                int[] counts = t.displayTestResult(outCtrl);
                total += counts[0];
                lastSuccess += counts[1];
                neverRun += counts[2];
                neverSuccess += counts[3];
            }
            grandTotal += t.getTotalTestCount();
        }

        outCtrl.printTestSuiteFooter(total,
                                     grandTotal,
                                     lastSuccess,
                                     neverRun,
                                     neverSuccess);
        return new int[] { total, lastSuccess, neverRun, neverSuccess };
    }

    public List<T> getTests() {
        return Collections.unmodifiableList(tests);
    }

    @Override
    protected TestResult createTestResult(Date lastSuccess) {
        Map<String, TestResult> testResults;
        {
            TestSuiteResult testSuiteResult = getTestSuiteResult();
            if (testSuiteResult != null)
                testResults = testSuiteResult.testResults;
            else testResults = new LinkedHashMap<>();
        }
        Date lastRun = new Date();
        if (success()) lastSuccess = lastRun;
        return new TestSuiteResult(this, lastRun, testResults, lastSuccess);
    }

    @Override
    public String getUniqueId() {
        return getName();
    }
}
