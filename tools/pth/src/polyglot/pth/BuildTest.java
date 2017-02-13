package polyglot.pth;

import java.io.File;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class BuildTest extends AbstractTest {

    public BuildTest(String name) {
        super(name);
    }

    @Override
    public String getUniqueId() {
        return getName();
    }

    @Override
    protected boolean matchFilter() {
        if (!Main.options.testCollectionFilters.isEmpty()) {
            for (String testCollectionFilter : Main.options.testCollectionFilters)
                if (Pattern.matches(testCollectionFilter, getName()))
                    return true;
            return false;
        }
        return super.matchFilter();
    }

    @Override
    public boolean haltOnFailure() {
        return true;
    }

    @Override
    protected boolean runTest() {
        // Check existence of compiler directory.
        String dirname = getCompilerDir();
        File dir = new File(dirname);
        if (!dir.isDirectory()) {
            appendFailureMessage("Compiler directory not found: " + dirname);
            return false;
        }

        int ret = invokeCompilerBuilder(dir);
        if (ret != 0) {
            if (ret > 0) appendFailureMessage("Failed to build: " + name
                    + " exit code " + ret);
            return false;
        }
        return true;
    }

    @Override
    protected void postRun() {
        output.finishTest(this);
        if (pdfReporter != null) pdfReporter.finishTest(this);
    }

    protected abstract int invokeCompilerBuilder(File dir);

    protected String getCompilerDir() {
        return Main.options.compilerpath == null
                ? "." : Main.options.compilerpath;
    }

    protected String prependCompilerPath(String filename) {
        String compilerpath = getCompilerDir();
        if (compilerpath != null) {
            if (filename != null) return compilerpath + filename;
            return compilerpath;
        }
        return filename;
    }
}
