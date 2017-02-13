package polyglot.pth;

import java.io.File;

public interface TestDriver {

    boolean shouldExecute();

    String getPathFromFlagMap(String... keys);

    boolean haltOnFailure();

    boolean preTest(SourceFileTestCollection sftc);

    boolean preTest(SourceFileTest t);

    boolean postTest(SourceFileTest t);

    boolean cleanup(SourceFileTest t, File saveDir);

    void printTestResult(SourceFileTest t, PDFReporter pr);

    void getSummary(StringBuffer sb);
}
