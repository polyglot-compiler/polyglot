/*
 * Author : Stephen Chong
 * Created: Nov 24, 2003
 */
package polyglot.pth;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 
 */
public class TestSuite extends AbstractTest {
    protected List tests;
    protected boolean haltOnFirstFailure = false;
    protected int totalTests = 0;
    protected int successfulTests = 0;

    public TestSuite(String name) {
        this(name, new ArrayList(), false);
    }

    public TestSuite(String name, List tests) {
        this(name, tests, false);
    }

    public TestSuite(String name, List tests, boolean haltOnFirstFailure) {
        super(name);
        this.tests = tests;
        this.haltOnFirstFailure = haltOnFirstFailure;        
    }
    
    public boolean getHaltOnFirstFailure() {
        return haltOnFirstFailure;
    }
    
    public void setOutputController(OutputController output) {
        super.setOutputController(output);
        for (Iterator iter = tests.iterator(); iter.hasNext(); ) {
            Test t = (Test)iter.next();
            t.setOutputController(output);
        }
    }

    protected boolean runTest() {
        boolean okay = true;

        if (this.getTestSuiteResult() == null) {
            this.setTestResult(this.createTestResult(null));
        }        
        
        for (Iterator i = tests.iterator(); i.hasNext(); ) {
            Test t = (Test)i.next();
            
            if (executeTest(t)) {
                totalTests++;
                TestResult tr = (TestResult)this.getTestSuiteResult().testResults.get(t.getName());
                if (tr != null) {
                    t.setTestResult(tr);
                }
                boolean result = t.run();            
                okay = okay && result;
                
                this.getTestSuiteResult().testResults.put(t.getName(), t.getTestResult());
                
                if (!result && haltOnFirstFailure) {
                    break;
                }
                else if (result) {
                    successfulTests++;
                }
            }
        }        
        return okay;
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

    protected boolean executeTest(Test t) {
        return executeTest(t.getName(), 
                          (TestResult)getTestSuiteResult().testResults.get(t.getName()));
    }
    
    protected static boolean executeTest(String testName, TestResult tr) {
        if (Main.options.testFilter != null &&
            !Pattern.matches(Main.options.testFilter, testName)) {
            return false;
        }
        
        if (Main.options.testPreviouslyFailedOnly &&
            tr != null && tr.dateLastSuccess != null &&
            tr.dateLastSuccess.equals(tr.dateTestRun)) {
                return false;
        }
        return true;
    }

    protected TestSuiteResult getTestSuiteResult() {
        return (TestSuiteResult)this.getTestResult();
    }
    
    protected TestResult createTestResult(Date lastSuccess) {
        Map testResults;
        if (this.getTestSuiteResult() != null) {
            testResults = getTestSuiteResult().testResults;
        }
        else {
            testResults = new LinkedHashMap();
        }
        Date lastRun = new Date();
        if (this.success()) {
            lastSuccess = lastRun;
        }
        return new TestSuiteResult(this, lastRun, testResults, lastSuccess);
    }    
}
