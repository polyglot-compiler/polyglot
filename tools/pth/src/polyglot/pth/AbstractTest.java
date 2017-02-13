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

import java.util.Date;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class AbstractTest implements Test {
    protected String name;
    protected String description;
    protected boolean success = false;
    protected boolean hasRun = false;
    protected TestResult testResult;

    protected StringBuffer noticeBuilder = null;
    protected StringBuffer failureMessageBuilder = null;

    protected OutputController output;
    protected PDFReporter pdfReporter;

    public AbstractTest(String name) {
        this.name = name;
    }

    @Override
    public void setOutputController(OutputController output) {
        this.output = output;
    }

    @Override
    public void setPDFReporter(PDFReporter pdfReporter) {
        this.pdfReporter = pdfReporter;
    }

    @Override
    public final boolean run() {
        preRun();
        success = runTest();

        hasRun = true;
        Date lastSuccess = null;
        if (getTestResult() != null)
            lastSuccess = getTestResult().dateLastSuccess;
        setTestResult(createTestResult(lastSuccess));
        postRun();
        return success();
    }

    @Override
    public boolean shouldExecute(TestResult tr) {
        if (!matchFilter()) return false;

        if (Main.options.testPreviouslyFailedOnly && tr != null
                && tr.dateLastSuccess != null
                && tr.dateLastSuccess.equals(tr.dateTestRun))
            return false;
        return true;
    }

    protected boolean matchFilter() {
        if (Main.options.testFilters.isEmpty()) return true;
        for (String testFilter : Main.options.testFilters)
            if (Pattern.matches(testFilter, getName())) return true;
        return false;
    }

    @Override
    public boolean haltOnFailure() {
        return false;
    }

    protected abstract boolean runTest();

    protected void preRun() {
        output.startTest(this);
        if (pdfReporter != null) pdfReporter.startTest(this);
    }

    protected void postRun() {
        output.finishTest(this);
        if (pdfReporter != null) pdfReporter.finishTest(this);
    }

    protected TestResult createTestResult(Date lastSuccess) {
        Date lastRun = new Date();
        if (success()) lastSuccess = lastRun;
        return new TestResult(this, lastRun, lastSuccess);
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public int getTotalTestCount() {
        return 1;
    }

    @Override
    public int getExecutedTestCount() {
        return 1;
    }

    @Override
    public int getSuccessfulTestCount() {
        return success() ? 1 : 0;
    }

    @Override
    public String getNotice() {
        return noticeBuilder == null ? null : noticeBuilder.toString();
    }

    @Override
    public String getFailureMessage() {
        return failureMessageBuilder == null
                ? null : failureMessageBuilder.toString();
    }

    public void appendNotice(String notice) {
        if (noticeBuilder == null)
            noticeBuilder = new StringBuffer(notice);
        else {
            noticeBuilder.append("\n");
            noticeBuilder.append(notice);
        }
    }

    public void appendFailureMessage(String failureMessage) {
        if (failureMessageBuilder == null)
            failureMessageBuilder = new StringBuffer(failureMessage);
        else {
            failureMessageBuilder.append("\n");
            failureMessageBuilder.append(failureMessage);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setDescription(String string) {
        description = string;
    }

    public void setName(String string) {
        name = string;
    }

    @Override
    public TestResult getTestResult() {
        return testResult;
    }

    @Override
    public void setTestResult(TestResult tr) {
        testResult = tr;
    }

    @Override
    public int[] displayTestResult(OutputController outCtrl) {
        int total = 0;
        int lastSuccess = 0;
        int neverRun = 0;
        int neverSuccess = 0;
        TestResult tr = getTestResult();

        total++;
        if (tr != null && tr.dateLastSuccess != null
                && tr.dateLastSuccess.equals(tr.dateTestRun))
            lastSuccess++;
        if (tr == null || tr.dateTestRun == null) neverRun++;
        if (tr == null || tr.dateLastSuccess == null) neverSuccess++;

        outCtrl.displayTestResults(tr, getName());

        return new int[] { total, lastSuccess, neverRun, neverSuccess };
    }
}
