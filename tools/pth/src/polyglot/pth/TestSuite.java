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
    protected List<Test> tests;
    protected boolean haltOnFirstFailure = false;
    protected int totalTests = 0;
    protected int successfulTests = 0;

    public TestSuite(String name) {
        this(name, new ArrayList<Test>(), false);
    }

    public TestSuite(String name, List<Test> tests) {
        this(name, tests, false);
    }

    public TestSuite(String name, List<Test> tests, boolean haltOnFirstFailure) {
        super(name);
        this.tests = tests;
        this.haltOnFirstFailure = haltOnFirstFailure;
    }

    public boolean getHaltOnFirstFailure() {
        return haltOnFirstFailure;
    }

    @Override
    public void setOutputController(OutputController output) {
        super.setOutputController(output);
        for (Test t : tests) {
            t.setOutputController(output);
        }
    }

    @Override
    protected boolean runTest() {
        boolean okay = true;

        if (this.getTestSuiteResult() == null) {
            this.setTestResult(this.createTestResult(null));
        }

        Map<String, TestResult> oldTestResults =
                new HashMap<String, TestResult>(this.getTestSuiteResult().testResults);
        Map<String, TestResult> newResults = new HashMap<String, TestResult>();

        for (Test t : tests) {
            TestResult tr = oldTestResults.get(t.getUniqueId());
            if (executeTest(t.getName(), tr)) {
                totalTests++;
                if (tr != null) {
                    t.setTestResult(tr);
                }
                boolean result = t.run();
                okay = okay && result;

                tr = t.getTestResult();

                if (!result && haltOnFirstFailure) {
                    break;
                }
                else if (result) {
                    successfulTests++;
                }
            }
            this.getTestSuiteResult().testResults.put(t.getUniqueId(), tr);
            newResults.put(t.getUniqueId(), tr);
            this.postIndividualTest();
        }
        this.getTestSuiteResult().testResults.clear();
        this.getTestSuiteResult().testResults.putAll(newResults);
        return okay;
    }

    protected void postIndividualTest() {
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

    protected static boolean executeTest(String testName, TestResult tr) {
        if (Main.options.testFilter != null
                && !Pattern.matches(Main.options.testFilter, testName)) {
            return false;
        }

        if (Main.options.testPreviouslyFailedOnly && tr != null
                && tr.dateLastSuccess != null
                && tr.dateLastSuccess.equals(tr.dateTestRun)) {
            return false;
        }
        return true;
    }

    protected TestSuiteResult getTestSuiteResult() {
        return (TestSuiteResult) this.getTestResult();
    }

    public List<Test> getTests() {
        return Collections.unmodifiableList(this.tests);
    }

    @Override
    protected TestResult createTestResult(Date lastSuccess) {
        Map<String, TestResult> testResults;
        if (this.getTestSuiteResult() != null) {
            testResults = getTestSuiteResult().testResults;
        }
        else {
            testResults = new LinkedHashMap<String, TestResult>();
        }
        Date lastRun = new Date();
        if (this.success()) {
            lastSuccess = lastRun;
        }
        return new TestSuiteResult(this, lastRun, testResults, lastSuccess);
    }

    @Override
    public String getUniqueId() {
        return this.getName();
    }
}
