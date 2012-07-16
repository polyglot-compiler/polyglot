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
public class SilentOutputController extends OutputController{
    public SilentOutputController(PrintStream out) {
        super(out);
    }
     
    @Override
    protected void startScriptTestSuite(ScriptTestSuite sts) { }
    @Override
    protected void startSourceFileTest(SourceFileTest sft) { }
    @Override
    protected void finishScriptTestSuite(ScriptTestSuite sts) { }
    @Override
    protected void finishSourceFileTest(SourceFileTest sft, ErrorQueue eq) { }
    @Override
    public void displayTestSuiteResults(String suiteName, TestSuite ts) { }
    @Override
    public void displayTestResults(TestResult tr, String testName) { }

    @Override
    public void warning(String w) { }
}
