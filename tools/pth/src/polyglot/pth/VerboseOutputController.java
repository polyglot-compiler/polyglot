/*
 * Author : Stephen Chong
 * Created: Feb 5, 2004
 */
package polyglot.pth;

import java.io.PrintStream;

import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.SilentErrorQueue;
import polyglot.util.StdErrorQueue;

/**
 * 
 */
public class VerboseOutputController extends OutputController{
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
            out.println("  Last run    : " + getDateDisplay(sts.getTestResult().dateTestRun));
            out.println("  Last success: " + getDateDisplay(sts.getTestResult().dateLastSuccess));
        }
        out.println("==============================");
    }
    @Override
    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        out.println("Test script " + sts.getName() + " finished");
        if (!sts.success() && sts.failureMessage != null) {
            out.println(sts.failureMessage); 
        }

        out.println("  " + 
            sts.getSuccesfulTestCount() + " out of " + sts.getTotalTestCount() + 
            " tests succeeded.");
    }

    @Override
    protected void startSourceFileTest(SourceFileTest sft) {
        out.println(sft.getName() + ": ");
        if (sft.getDescription() != null && sft.getDescription().length() > 0) {
            out.println("    Description : " + sft.getDescription());
        }
        if (sft.getTestResult() != null) {            
            out.println("    Last run    : " + getDateDisplay(sft.getTestResult().dateTestRun));
            out.println("    Last success: " + getDateDisplay(sft.getTestResult().dateLastSuccess));
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
            StdErrorQueue stdeq = new StdErrorQueue(out, 
                         eq.errorCount() + 1, 
                         sft.getName());
                         
            SilentErrorQueue seq = (SilentErrorQueue)eq;
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
            if (tr != null && tr.dateLastSuccess != null && tr.dateLastSuccess.equals(tr.dateTestRun)) {
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
        out.println("      Last run    : " + getDateDisplay(tr==null?null:tr.dateTestRun));
        out.println("      Last success: " + getDateDisplay(tr==null?null:tr.dateLastSuccess));
    }

    @Override
    public void warning(String w) {
        out.println("Warning: " + w);
    }
}
