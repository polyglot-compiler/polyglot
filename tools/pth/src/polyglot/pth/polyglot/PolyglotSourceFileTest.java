package polyglot.pth.polyglot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import polyglot.pth.AnyExpectedFailure;
import polyglot.pth.ExpectedFailure;
import polyglot.pth.SourceFileTest;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.SilentErrorQueue;

/**
 *
 */
public class PolyglotSourceFileTest extends SourceFileTest {

    protected static final int TIMEOUT = 30;
    protected static final TimeUnit TIMEUNIT = TimeUnit.SECONDS;

    protected final SilentErrorQueue eq;

    public PolyglotSourceFileTest(List<List<String>> compilationUnits,
            List<ExpectedFailure> expectedFailures) {
        super(compilationUnits, expectedFailures);
        eq = new SilentErrorQueue(100, getName());
    }

    public ErrorQueue errorQueue() {
        return eq;
    }

    @Override
    protected boolean matchFilter() {
        return td.shouldExecute() && super.matchFilter();
    }

    @Override
    public PolyglotTestDriver getTestDriver() {
        return (PolyglotTestDriver) td;
    }

    @Override
    public int invokeCompiler(String compilerDirname, List<String> cmdLineHdr,
            List<String> sourceFileNames) {
        PolyglotTestDriver td = getTestDriver();
        try {
            List<String> cmdLine = new LinkedList<>(cmdLineHdr);
            cmdLine.addAll(sourceFileNames);

            // TODO
//            if (addDestDirToCmdLine) {
//                cmdLine.add("-d");
//                cmdLine.add(prependTestPath(destDir.getName()));
//            }
//
//            // To get separate compilation, add the output directory to the
//            // class path only if we are compiling subsequent sets of files.
//            if (addClassPath) {
//                cmdLine.add("-cp");
//                cmdLine.add(prependTestPath(destDir.getName()));
//            }
//            else addClassPath = true;
            return td.invokeCompiler(this, cmdLine);
        }
        catch (RuntimeException e) {
            if (e.getMessage() != null) {
                appendFailureMessage(e.getMessage());
                e.printStackTrace();
                return 1;
            }
            else {
                appendFailureMessage("Uncaught " + e.getClass().getName());
                e.printStackTrace();
                return 1;
            }
        }
    }

    @Override
    public boolean postTest() {
        List<ErrorInfo> errors = new ArrayList<>(eq.getErrors());

        boolean swallowRemainingFailures = false;
        for (ExpectedFailure f : expectedFailures) {
            if (f instanceof AnyExpectedFailure) {
                swallowRemainingFailures = true;
                continue;
            }

            boolean found = false;
            for (Iterator<ErrorInfo> j = errors.iterator(); j.hasNext();) {
                ErrorInfo e = j.next();
                if (f.matches(e)) {
                    // this error info has been matched. remove it.
                    found = true;
                    j.remove();
                    break;
                }
            }
            if (!found) {
                appendFailureMessage("Expected to see " + f.toString());
                return false;
            }
        }

        // are there any unaccounted for errors?
        if (!errors.isEmpty() && !swallowRemainingFailures) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<ErrorInfo> iter =
                    errors.iterator(); iter.hasNext();) {
                ErrorInfo err = iter.next();
                sb.append(err.getMessage());
                if (err.getPosition() != null) {
                    sb.append(" (");
                    sb.append(err.getPosition());
                    sb.append(")");
                }
                if (iter.hasNext()) sb.append("; ");
            }
            appendFailureMessage(sb.toString());
        }
        return errors.isEmpty() || swallowRemainingFailures;
    }

    @Override
    public String compilerName() {
        return getTestDriver().commandName();
    }
}
