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
import java.util.regex.Pattern;

/**
 * 
 */
public class TestSuite extends AbstractTest {
    protected List<Test> tests;
    protected boolean haltOnFirstFailure = false;
    protected int totalTests = 0;
    protected int successfulTests = 0;

    public TestSuite(String name) {
        this(name, new ArrayList<Test>(), false);
    }

    public TestSuite(String name, List<Test> tests) {
        this(name, tests, false);
    }

    public TestSuite(String name, List<Test> tests, boolean haltOnFirstFailure) {
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
        for (Test t : tests) {
            t.setOutputController(output);
        }
    }

    @Override
    protected boolean runTest() {
        boolean okay = true;

        if (this.getTestSuiteResult() == null) {
            this.setTestResult(this.createTestResult(null));
        }

        Map<String, TestResult> oldTestResults =
                new HashMap<>(this.getTestSuiteResult().testResults);
        Map<String, TestResult> newResults = new HashMap<>();

        for (Test t : tests) {
            TestResult tr = oldTestResults.get(t.getUniqueId());
            if (executeTest(t.getName(), tr)) {
                totalTests++;
                if (tr != null) {
                    t.setTestResult(tr);
                }
                boolean result = t.run();
                okay = okay && result;

                tr = t.getTestResult();

                if (!result && haltOnFirstFailure) {
                    break;
                }
                else if (result) {
                    successfulTests++;
                }
                this.getTestSuiteResult().testResults.put(t.getUniqueId(), tr);
                this.postIndividualTest();
            }
            newResults.put(t.getUniqueId(), tr);
        }
        this.getTestSuiteResult().testResults.clear();
        this.getTestSuiteResult().testResults.putAll(newResults);
        return okay;
    }

    protected void postIndividualTest() {
    }

    public int getTotalTestCount() {
        return totalTests;
    }

    public int getSuccesfulTestCount() {
        return successfulTests;
    }

    public int getFailedTestCount() {
        return totalTests - successfulTests;
    }

    protected static boolean executeTest(String testName, TestResult tr) {
        if (Main.options.testFilter != null
                && !Pattern.matches(Main.options.testFilter, testName)) {
            return false;
        }

        if (Main.options.testPreviouslyFailedOnly && tr != null
                && tr.dateLastSuccess != null
                && tr.dateLastSuccess.equals(tr.dateTestRun)) {
            return false;
        }
        return true;
    }

    protected TestSuiteResult getTestSuiteResult() {
        return (TestSuiteResult) this.getTestResult();
    }

    public List<Test> getTests() {
        return Collections.unmodifiableList(this.tests);
    }

    @Override
    protected TestResult createTestResult(Date lastSuccess) {
        Map<String, TestResult> testResults;
        if (this.getTestSuiteResult() != null) {
            testResults = getTestSuiteResult().testResults;
        }
        else {
            testResults = new LinkedHashMap<>();
        }
        Date lastRun = new Date();
        if (this.success()) {
            lastSuccess = lastRun;
        }
        return new TestSuiteResult(this, lastRun, testResults, lastSuccess);
    }

    @Override
    public String getUniqueId() {
        return this.getName();
    }
}
