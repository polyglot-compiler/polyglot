/*
 * Author : Stephen Chong
 * Created: Nov 24, 2003
 */
package polyglot.pth;

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 
 */
public class ScriptTestSuite extends TestSuite {
    protected File scriptFile;
    public ScriptTestSuite(String scriptFilename) {
        super(scriptFilename);
        this.scriptFile = new File(scriptFilename);
        this.loadResults();            
    }
    public ScriptTestSuite(File scriptFile) {
        super(scriptFile.getName());
        this.scriptFile = scriptFile;
        this.loadResults();            
    }
    
    protected boolean runTest() {
        if (!this.scriptFile.exists()) {
            this.setFailureMessage("File " + scriptFile.getName() + " not found.");
            return false;
        }
        else {
            if (!parseScript()) {
                return false;
            }

            this.setOutputController(output);
            return super.runTest();            
        }
    }
        
    protected void postRun() {
        super.postRun();
        this.saveResults();
    }

    protected boolean parseScript() {        
        Grm grm = new Grm(this.scriptFile);
        try {
            this.tests = (List)grm.parse().value;
        }
        catch (Exception e) {
            this.setFailureMessage("Parsing error: " + e.getMessage());
            return false;
        }
        return true;
    }
    
    protected void loadResults() {
        try {        
            ObjectInputStream ois = 
                 new ObjectInputStream(
                          new FileInputStream(TestSuiteResult.getResultsFileName(this.scriptFile)));
            TestResult tr = (TestResult)ois.readObject();
            this.setTestResult(tr);
        }
        catch (FileNotFoundException e) { 
            // ignore, and fail silently
        }
        catch (ClassNotFoundException e) { 
            // ignore, and fail silently
        }
        catch (IOException e) {
            System.err.println("Unable to load results for test suite " + this.getName() + ": " + e.getMessage());
        }
    }
    protected void saveResults() {
        try {        
            ObjectOutputStream oos = 
                 new ObjectOutputStream(
                          new FileOutputStream(TestSuiteResult.getResultsFileName(this.scriptFile)));
            oos.writeObject(this.getTestSuiteResult());            
        }
        catch (IOException e) {
            System.err.println("Unable to save results for test suite " + this.getName());
        }
    }
}
