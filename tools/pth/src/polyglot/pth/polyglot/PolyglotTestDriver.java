package polyglot.pth.polyglot;

import java.io.File;
import java.util.List;

import polyglot.pth.AbstractTestDriver;
import polyglot.pth.PDFReporter;
import polyglot.pth.SourceFileTest;
import polyglot.pth.SourceFileTestCollection;
import polyglot.util.ErrorQueue;

public class PolyglotTestDriver extends AbstractTestDriver {

    protected File testDir;
    protected File destDir;

    public PolyglotTestDriver(SourceFileTestCollection sftc) {
        super(sftc);
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    public String commandName() {
        return "polyglot";
    }

    protected File getTestDir() {
        if (testDir == null) {
            String testDirname = getPathFromFlagMap("testpath");
            testDir = new File(testDirname);
        }
        return testDir;
    }

    protected File getDestDir() {
        if (destDir == null) {
            String destDirname = getPathFromFlagMap("workpath", "D");
            destDir = new File(destDirname);
        }
        return destDir;
    }

    public int invokeCompiler(PolyglotSourceFileTest sft,
            List<String> cmdLine) {
        ErrorQueue eq = sft.errorQueue();
        try {
            polyglot.main.Main polyglotMain = new polyglot.main.Main();
            polyglotMain.start(cmdLine.toArray(new String[cmdLine.size()]), eq);
        }
        catch (polyglot.main.Main.TerminationException e) {
            if (e.getMessage() != null) {
                sft.appendFailureMessage(e.getMessage());
                return 1;
            }
            else {
                if (!eq.hasErrors()) {
                    sft.appendFailureMessage("Failed to compile for unknown reasons: "
                            + e.toString());
                    return 1;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean preTest(SourceFileTestCollection sftc) {
        return true;
    }

    @Override
    public boolean preTest(SourceFileTest t) {
        return true;
    }

    @Override
    public boolean postTest(SourceFileTest t) {
        boolean okay = true;
//        File destDir = getDestDir();
//        for (PolyglotTester tester : testers) {
//            // Normalize generated files.
//            if (!tester.normalizeGeneratedFiles(t, destDir))
//                okay = false;
//            else {
//                // Compare generated files with solution and report results.
//                okay = okay && tester.checkResult(t, destDir);
//            }
//        }
        return okay;
    }

    @Override
    public boolean cleanup(SourceFileTest t, File saveDir) {
        boolean okay = true;
//        File destDir = getDestDir();
//        for (PolyglotTester tester : testers) {
//            if (shouldCleanupReferenceFiles()) {
//                // Clean up reference files.
//                okay = tester.cleanupReferenceFiles(t, destDir, saveDir)
//                        && okay;
//            }
//            // Clean up generated files.
//            okay = tester.cleanupGeneratedFiles(t, destDir, saveDir) && okay;
//        }
        return okay;
    }

    protected boolean shouldCleanupReferenceFiles() {
        File testDir = getTestDir();
        File destDir = getDestDir();
        // Clean up unless testDir and destDir are the same.
        return !isSameDirectory(testDir, destDir);
    }

    @Override
    public void printTestResult(SourceFileTest t, PDFReporter pr) {
//        File destDir = getDestDir();
//        for (PolyglotTester tester : testers)
//            tester.printTestResult(t, destDir, pr);
    }

//    @Override
//    public void getSummary(StringBuffer sb) {
//        for (PolyglotTester tester : testers)
//            tester.getSummary(sb);
//    }
}
