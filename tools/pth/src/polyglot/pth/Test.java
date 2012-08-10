/*
 * Author : Stephen Chong
 * Created: Nov 21, 2003
 */
package polyglot.pth;

/**
 * 
 */
public interface Test {
    String getName();

    /**
     * A string that uniquely identifies this test.
     * @return
     */
    String getUniqueId();

    String getDescription();

    boolean success();

    String getFailureMessage();

    boolean run();

    void setOutputController(OutputController output);

    TestResult getTestResult();

    void setTestResult(TestResult tr);
}
