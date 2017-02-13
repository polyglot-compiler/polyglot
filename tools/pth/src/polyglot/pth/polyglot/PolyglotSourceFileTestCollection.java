package polyglot.pth.polyglot;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import polyglot.pth.SourceFileTest;
import polyglot.pth.SourceFileTestCollection;

public class PolyglotSourceFileTestCollection extends SourceFileTestCollection {

    private static final String JAVAC = "javac";

    public PolyglotSourceFileTestCollection(String testCommand, String name,
            String testDir, String args, List<SourceFileTest> tests) {
        super(testCommand, name, testDir, args, tests);
    }

    @Override
    protected void populatePathFlags() {
        pathFlags.put("-sourcepath", "sourcepath");
        pathFlags.put("-D", "D");
    }

    @Override
    protected List<String> buildCmdLine() {
        List<String> args = super.buildCmdLine();
        String s;

        if ((s = testCommand) != null && !s.equals(JAVAC)) {
            args.add("-extclass");
            args.add(testCommand);
        }

        if ((s = getAdditionalClasspath()) != null) {
            args.add("-cp");
            args.add(prependTestPath(s));
        }

        List<String> newArgs = new ArrayList<>(args.size());
        for (String arg : args) {
            if (arg.contains("$(testpath")) {
                String workpath = getPathFromFlagMap("workpath");
                String testpath = getPathFromFlagMap("testpath");
                arg = arg.replace("$(testpath)", testpath);
                // Relativize testpath against workpath.
                File testpathFile = new File(arg);
                Path testPath = testpathFile.toPath().normalize();
                if (!testPath.isAbsolute()) {
                    File workpathFile = new File(workpath);
                    Path workPath = workpathFile.toPath().normalize();
                    if (workPath.isAbsolute()) {
                        arg = workpathFile.toURI()
                                          .relativize(testpathFile.toURI())
                                          .getPath();
                    }
                    else arg = workPath.relativize(testPath).toString();
                }
            }
            newArgs.add(arg);
        }
        return newArgs;
    }

    protected String prependTestPath(String filename) {
        String testpath = getPathFromFlagMap("testpath");
        if (testpath != null) {
            if (filename != null) return testpath + filename;
            return testpath;
        }
        return filename;
    }

    @Override
    protected PolyglotTestDriver createTestDriver() {
        PolyglotTestDriver ptd;
        switch (testCommand) {
        case "javac":
            ptd = new JavaTestDriver(this);
            break;
        default:
            ptd = new PolyglotTestDriver(this);
        }
        return ptd;
    }

}
