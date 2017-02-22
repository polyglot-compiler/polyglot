package polyglot.pth.polyglot;

import java.io.File;
import java.io.Reader;
import java.util.List;

import polyglot.pth.BuildTest;
import polyglot.pth.ExpectedFailure;
import polyglot.pth.Grm;
import polyglot.pth.Lexer;
import polyglot.pth.Lexer_c;
import polyglot.pth.SourceFileTest;
import polyglot.pth.SourceFileTestCollection;
import polyglot.pth.TestFactory;

public class PolyglotTestFactory implements TestFactory {

    @Override
    public BuildTest BuildTest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Grm Grm(File scriptFile) {
        return new Grm(this, scriptFile);
    }

    @Override
    public Lexer Lexer(Reader scriptFileReader) {
        return new Lexer_c(scriptFileReader);
    }

    @Override
    public SourceFileTestCollection SourceFileTestCollection(String testCommand,
            String name, String testDir, String args,
            List<SourceFileTest> tests) {
        return new PolyglotSourceFileTestCollection(testCommand,
                                                    name,
                                                    testDir,
                                                    args,
                                                    tests);
    }

    @Override
    public SourceFileTest SourceFileTest(List<List<String>> compilationUnits,
            List<ExpectedFailure> expectedFailures) {
        return new PolyglotSourceFileTest(compilationUnits, expectedFailures);
    }

}
