/*
 * Author : Stephen Chong
 * Created: Feb 5, 2004
 */
package polyglot.pth;

import java.io.PrintStream;
import java.util.Iterator;

import polyglot.util.ErrorQueue;

/**
 * 
 */
public class StdOutputController extends OutputController{
    public StdOutputController(PrintStream out) {
        super(out);
    }
     
    protected void startScriptTestSuite(ScriptTestSuite sts) {
        out.println("Test script " + sts.getName());
    }
    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        if (!sts.success() && sts.failureMessage != null) {
            out.println(sts.failureMessage); 
        }

        out.println(sts.getName() + ": " + 
            sts.getSuccesfulTestCount() + " out of " + sts.getTotalTestCount() + 
            " tests suceeded.");
    }

    protected void startSourceFileTest(SourceFileTest sft) {
        out.print("  " + sft.getName() + ": ");
    }
    
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
    

    public void displayTestSuiteResults(String suiteName, TestSuiteResult tsr) {
        if (tsr == null || tsr.testResults.isEmpty()) {
            out.println("No test results for " + suiteName);
            return;
        }
    
        out.print("Test script \"" + tsr.testName + "\"");
        out.println("    Last run: " + getDateDisplay(tsr.dateTestRun));
        out.println("  Contains tests:");
        for (Iterator iter = tsr.testResults.keySet().iterator(); iter.hasNext(); ) {
            String testName = (String)iter.next();
            TestResult tr = (TestResult)tsr.testResults.get(testName);
            if (TestSuite.executeTest(testName, tr)) {
                displayTestResults(tr);
            }
        }
    }

    public void displayTestResults(TestResult tr) {
        out.print("    " + tr.testName);
        out.println("\t\t run " + getDateDisplay(tr.dateTestRun) + "; success " + getDateDisplay(tr.dateLastSuccess));
    }
}
