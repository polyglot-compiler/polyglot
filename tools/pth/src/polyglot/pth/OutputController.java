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

/**
 * 
 */
public class OutputController {
    protected final PrintStream out;
    protected final int verbosity;
    
    public OutputController(PrintStream out, int verbosity) {
        this.out = out;
        this.verbosity = verbosity;
    }
     
    public void startTest(Test t) {
        if (t instanceof ScriptTestSuite) {
            ScriptTestSuite sts = (ScriptTestSuite)t;
            out.println("Test script " + sts.getName());
        }
        else if (t instanceof SourceFileTest) {
            SourceFileTest sft = (SourceFileTest)t;
            out.print("  " + sft.getName() + ": ");
        }
    }
    
    public void finishTest(Test t) {
        if (t instanceof ScriptTestSuite) {
            ScriptTestSuite sts = (ScriptTestSuite)t;
            out.println(sts.getName() + ": " + 
                sts.getSuccesfulTestCount() + " out of " + sts.getTotalTestCount() + 
                " tests suceeded.");
        }
        else if (t instanceof SourceFileTest) {
            SourceFileTest sft = (SourceFileTest)t;
            if (sft.success()) {
                out.println("OK");
            }
            else {
                out.println("Failed with message \"" + sft.getFailureMessage() + "\"");
            }
        }
    }
    
    public void displayTestSuiteResults(TestSuiteResult tsr) {
        if (tsr == null || tsr.testResults.isEmpty()) {
            out.println("No test results for " + tsr.testName);
            return;
        }
        
        out.println("Test script \"" + tsr.testName + "\"");
        out.println("    Last run: " + getDateDisplay(tsr.dateTestRun));
        out.println("  Tests:  (name, lastRun, lastSucceeded)");
        for (Iterator iter = tsr.testResults.keySet().iterator(); iter.hasNext(); ) {
            String testName = (String)iter.next();
            if (Main.options.testFilter == null || Pattern.matches(Main.options.testFilter, testName)) {
                TestResult tr = (TestResult)tsr.testResults.get(testName);
                out.print("    " + testName);
                out.println(", " + getDateDisplay(tr.dateTestRun) + ", " + getDateDisplay(tr.dateLastSuccess));
            }
        }
    }

    public void displayTestResults(TestResult tr) {
    }

    public String getDateDisplay(Date d) {
        if (d == null) return "never";        
        DateFormat df;
        Date now = new Date();
        if (now.getYear() == d.getYear() && now.getMonth() == d.getMonth() 
            && now.getDate() == d.getDate()) {
          df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        }
        else {
            df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        }
        return df.format(d);
    }    
}
