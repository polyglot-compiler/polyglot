/*
 * Author : Stephen Chong
 * Created: Feb 6, 2004
 */
package polyglot.pth;

import java.util.Date;
import java.util.Map;

/**
 * 
 */
public final class TestSuiteResult extends TestResult {
    public final Map testResults;
    public TestSuiteResult(Test t, Date dateTestRun, Map testResults, Date dateLastSuccess) {
        super(t, dateTestRun, dateLastSuccess);
        this.testResults = testResults;
    }    
}
