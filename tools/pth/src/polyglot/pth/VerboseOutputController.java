/*
 * Author : Stephen Chong
 * Created: Feb 5, 2004
 */
package polyglot.pth;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import polyglot.util.*;
import polyglot.util.ErrorQueue;
import polyglot.util.SilentErrorQueue;
import polyglot.util.StdErrorQueue;

/**
 * 
 */
public class VerboseOutputController extends OutputController{
    public VerboseOutputController(PrintStream out) {
        super(out);
    }
     
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
    protected void finishScriptTestSuite(ScriptTestSuite sts) {
        out.println("Test script " + sts.getName() + " finished");
        if (!sts.success() && sts.failureMessage != null) {
            out.println(sts.failureMessage); 
        }

        out.println("  " + 
            sts.getSuccesfulTestCount() + " out of " + sts.getTotalTestCount() + 
            " tests suceeded.");
    }

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
            StdErrorQueue stdeq = new StdErrorQueue(out, 
                         eq.errorCount() + 1, 
                         sft.getName());
                         
            SilentErrorQueue seq = (SilentErrorQueue)eq;
            for (Iterator i = seq.getErrors().iterator(); i.hasNext(); ) {
                ErrorInfo ei = (ErrorInfo)i.next();
                stdeq.enqueue(ei);
            }
            stdeq.flush();
            if (seq.errorCount() > 0) {
                out.println("-----------------------------");
            }
        }
    }
    

    public void displayTestSuiteResults(String suiteName, TestSuiteResult tsr) {
        if (tsr == null || tsr.testResults.isEmpty()) {
            out.println("No test results for " + suiteName);
            return;
        }
    
        out.println("Test script " + tsr.testName);
        out.println("  Last run    : " + getDateDisplay(tsr.dateTestRun));
        out.println("  Last success: " + getDateDisplay(tsr.dateLastSuccess));
        for (Iterator iter = tsr.testResults.keySet().iterator(); iter.hasNext(); ) {
            String testName = (String)iter.next();
            TestResult tr = (TestResult)tsr.testResults.get(testName);
            if (TestSuite.executeTest(testName, tr)) {
                displayTestResults(tr);
            }
        }
    }

    public void displayTestResults(TestResult tr) {
        out.println("    " + tr.testName);
        out.println("      Last run    : " + getDateDisplay(tr.dateTestRun));
        out.println("      Last success: " + getDateDisplay(tr.dateLastSuccess));
    }
}
