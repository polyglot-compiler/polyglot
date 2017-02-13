package polyglot.pth;

import java.io.File;
import java.io.IOException;

public abstract class AbstractTestDriver implements TestDriver {

    protected SourceFileTestCollection sftc;

    public AbstractTestDriver(SourceFileTestCollection sftc) {
        this.sftc = sftc;
    }

    @Override
    public String getPathFromFlagMap(String... keys) {
        return sftc.getPathFromFlagMap(keys);
    }

    @Override
    public boolean haltOnFailure() {
        return false;
    }

    @Override
    public void getSummary(StringBuffer sb) {
    }

    protected boolean isSameDirectory(File dir1, File dir2) {
        try {
            return dir1.getCanonicalPath().equals(dir2.getCanonicalPath());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return true;
        }
    }
}
