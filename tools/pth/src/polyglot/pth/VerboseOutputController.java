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

import java.io.PrintStream;

import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.SilentErrorQueue;
import polyglot.util.StdErrorQueue;

/**
 * 
 */
public class VerboseOutputController extends OutputController {
    private boolean alwaysShowErrorQ;

    public VerboseOutputController(PrintStream out, boolean alwaysShowErrorQ) {
        super(out);
        this.alwaysShowErrorQ = alwaysShowErrorQ;
    }

    @Override
    protected void startScriptTestSuite(ScriptTestSuite sts) {
        out.println("Test script " + sts.getName());
        if (sts.getDescription() != null && sts.getDescription().length() > 0) {
            out.println("  Description : " + sts.getDescription());
        }
        if (sts.getTestResult() != null) {
            out.println("  Last run    : "
                    + getDateDisplay(sts.getTestResult().dateTestRun));
            out.println("  Last success: "
                    + getDateDisplay(sts.getTestResult().dateLastSuccess));
        }
        out.println("==============================");
    }

    @Override
    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        out.println("Test script " + sts.getName() + " finished");
        if (!sts.success() && sts.failureMessage != null) {
            out.println(sts.failureMessage);
        }

        out.println("  " + sts.getSuccesfulTestCount() + " out of "
                + sts.getTotalTestCount() + " tests succeeded.");
    }

    @Override
    protected void startSourceFileTest(SourceFileTest sft) {
        out.println(sft.getName() + ": ");
        if (sft.getDescription() != null && sft.getDescription().length() > 0) {
            out.println("    Description : " + sft.getDescription());
        }
        if (sft.getTestResult() != null) {
            out.println("    Last run    : "
                    + getDateDisplay(sft.getTestResult().dateTestRun));
            out.println("    Last success: "
                    + getDateDisplay(sft.getTestResult().dateLastSuccess));
        }
    }

    @Override
    protected void finishSourceFileTest(SourceFileTest sft, ErrorQueue eq) {
        if (sft.success()) {
            out.println("    Test completed OK");
        }
        else {
            out.print("    Test failed");
            if (sft.getFailureMessage() != null) {
                out.println(": " + sft.getFailureMessage());
            }
            else {
                out.println();
            }
        }
        if (alwaysShowErrorQ || !sft.success()) {
            StdErrorQueue stdeq =
                    new StdErrorQueue(out, eq.errorCount() + 1, sft.getName());

            SilentErrorQueue seq = (SilentErrorQueue) eq;
            for (ErrorInfo ei : seq.getErrors()) {
                stdeq.enqueue(ei);
            }
            stdeq.flush();
            if (seq.errorCount() > 0) {
                out.println("-----------------------------");
            }
        }
    }

    @Override
    public void displayTestSuiteResults(String suiteName, TestSuite ts) {
        TestSuiteResult tsr = ts.getTestSuiteResult();
        if (tsr == null || tsr.testResults.isEmpty()) {
            out.println("No test results for " + suiteName);
            return;
        }

        out.println("Test script " + tsr.testName);
        out.println("  Last run    : " + getDateDisplay(tsr.dateTestRun));
        out.println("  Last success: " + getDateDisplay(tsr.dateLastSuccess));

        int total = 0;
        int lastSuccess = 0;
        int neverRun = 0;
        int neverSuccess = 0;
        for (Test t : ts.getTests()) {
            String testName = t.getName();
            TestResult tr = tsr.testResults.get(t.getUniqueId());
            if (TestSuite.executeTest(testName, tr)) {
                displayTestResults(tr, testName);
            }
            total++;
            if (tr != null && tr.dateLastSuccess != null
                    && tr.dateLastSuccess.equals(tr.dateTestRun)) {
                lastSuccess++;
            }
            if (tr == null || tr.dateTestRun == null) {
                neverRun++;
            }
            if (tr == null || tr.dateLastSuccess == null) {
                neverSuccess++;
            }
        }
        out.println("Total tests: " + total);
        out.println("   Succeeded last run: " + lastSuccess);
        out.println("   Never run         : " + neverRun);
        out.println("   Never succeeded   : " + neverSuccess);
    }

    @Override
    public void displayTestResults(TestResult tr, String testName) {
        out.println("    " + testName);
        out.println("      Last run    : "
                + getDateDisplay(tr == null ? null : tr.dateTestRun));
        out.println("      Last success: "
                + getDateDisplay(tr == null ? null : tr.dateLastSuccess));
    }

    @Override
    public void warning(String w) {
        out.println("Warning: " + w);
    }
}
