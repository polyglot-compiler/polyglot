package polyglot.pth.polyglot;

import java.util.List;

import javax.tools.JavaCompiler;

import polyglot.pth.SourceFileTestCollection;

public class JavaTestDriver extends PolyglotTestDriver {

    private static final JavaCompiler javaCompiler =
            polyglot.main.Main.javaCompiler();

    public JavaTestDriver(SourceFileTestCollection sftc) {
        super(sftc);
    }

    @Override
    public String commandName() {
        return "javac";
    }

    @Override
    public int invokeCompiler(PolyglotSourceFileTest sft,
            List<String> cmdLine) {
        return javaCompiler.run(null,
                                null,
                                null,
                                cmdLine.toArray(new String[cmdLine.size()]));
    }
}
