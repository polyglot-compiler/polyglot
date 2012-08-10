/*
 * Author : Stephen Chong
 * Created: Feb 5, 2004
 */
package polyglot.pth;

import java.io.PrintStream;

import polyglot.util.ErrorQueue;

/**
 * 
 */
public class StdOutputController extends OutputController {
    public StdOutputController(PrintStream out) {
        super(out);
    }

    @Override
    protected void startScriptTestSuite(ScriptTestSuite sts) {
        out.println("Test script " + sts.getName());
    }

    @Override
    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        if (!sts.success() && sts.failureMessage != null) {
            out.println(sts.failureMessage);
        }

        out.println(sts.getName() + ": " + sts.getSuccesfulTestCount()
                + " out of " + sts.getTotalTestCount() + " tests succeeded.");
    }

    @Override
    protected void startSourceFileTest(SourceFileTest sft) {
        out.print("  " + sft.getName() + ": ");
    }

    @Override
    protected void finishSourceFileTest(SourceFileTest sft, ErrorQueue eq) {
        if (sft.success()) {
            out.println("OK");
        }
        else if (sft.getFailureMessage() != null) {
            out.println(sft.getFailureMessage());
        }
        else {
            out.println("Failed (no message)");
        }
    }

    @Override
    public void displayTestSuiteResults(String suiteName, TestSuite ts) {
        TestSuiteResult tsr = ts.getTestSuiteResult();

        if (tsr == null || tsr.testResults.isEmpty()) {
            out.println("No test results for " + suiteName);
            return;
        }

        out.print("Test script \"" + tsr.testName + "\"");
        out.println("    Last run: " + getDateDisplay(tsr.dateTestRun));
        out.println("  Contains tests:");

        int total = 0;
        int grandTotal = 0;
        int lastSuccess = 0;
        int neverRun = 0;
        int neverSuccess = 0;
        for (Test t : ts.getTests()) {
            String testId = t.getUniqueId();
            TestResult tr = tsr.testResults.get(testId);
            if (TestSuite.executeTest(t.getName(), tr)) {
                displayTestResults(tr, t.getName());

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
            grandTotal++;
        }
        out.print("Total tests displayed: " + total);
        if (total != grandTotal) {
            out.print(" (out of " + grandTotal + " in script)");
        }
        out.println();
        out.println("   Succeeded last run: " + lastSuccess);
        out.println("   Never run         : " + neverRun);
        out.println("   Never succeeded   : " + neverSuccess);
    }

    private static final int TEST_NAME_COLUMN_WIDTH = 30;

    @Override
    public void displayTestResults(TestResult tr, String testName) {
        StringBuffer sb = new StringBuffer();
        sb.append("    ");
        sb.append(testName);
        while (sb.length() < TEST_NAME_COLUMN_WIDTH) {
            sb.append(' ');
        }
        sb.append(" run ");
        sb.append(getDateDisplay(tr == null ? null : tr.dateTestRun));
        sb.append("; success ");
        sb.append(getDateDisplay(tr == null ? null : tr.dateLastSuccess));
        out.println(sb.toString());
    }

    @Override
    public void warning(String w) {
        out.println("Warning: " + w);
    }
}
