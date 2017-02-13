package polyglot.pth;

import java.util.List;

public interface TestFactory {

    BuildTest BuildTest();

    SourceFileTestCollection SourceFileTestCollection(String testCommand,
            String name, String testDir, String args,
            List<SourceFileTest> tests);

    SourceFileTest SourceFileTest(List<List<String>> compilationUnits,
            List<ExpectedFailure> expectedFailures);
}
