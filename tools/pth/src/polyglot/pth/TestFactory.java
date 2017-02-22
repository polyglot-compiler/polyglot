package polyglot.pth;

import java.io.File;
import java.io.Reader;
import java.util.List;

public interface TestFactory {

    BuildTest BuildTest();

    Grm Grm(File scriptFile);

    Lexer Lexer(Reader scriptFileReader);

    SourceFileTestCollection SourceFileTestCollection(String testCommand,
            String name, String testDir, String args,
            List<SourceFileTest> tests);

    SourceFileTest SourceFileTest(List<List<String>> compilationUnits,
            List<ExpectedFailure> expectedFailures);
}
