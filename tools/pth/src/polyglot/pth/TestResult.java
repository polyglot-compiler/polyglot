/*
 * Author : Stephen Chong
 * Created: Feb 6, 2004
 */
package polyglot.pth;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 */
public class TestResult implements Serializable {
    public final String testName; 
    public final String testUniqueID;
    public final Date dateTestRun;
    public final Date dateLastSuccess;
    public final boolean testRunSucessful;
    
    public TestResult(Test t, Date dateTestRun, Date dateLastSuccess) {
        this.dateTestRun = dateTestRun;
        this.dateLastSuccess = dateLastSuccess;
        this.testName = t.getName();
        this.testUniqueID = t.getUniqueId();
        this.testRunSucessful = t.success();
    } 
}
