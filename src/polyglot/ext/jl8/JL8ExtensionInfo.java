package polyglot.ext.jl8;

import java.io.Reader;
import java.util.Set;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.JL5ExtFactory_c;
import polyglot.ext.jl7.JL7ExtensionInfo;
import polyglot.ext.jl7.ast.JL7ExtFactory_c;
import polyglot.ext.jl8.ast.JL8ExtFactory_c;
import polyglot.ext.jl8.ast.JL8Lang_c;
import polyglot.ext.jl8.ast.JL8NodeFactory_c;
import polyglot.ext.jl8.parse.Grm;
import polyglot.ext.jl8.parse.Lexer_c;
import polyglot.ext.jl8.types.JL8TypeSystem_c;
import polyglot.frontend.CupParser;
import polyglot.frontend.Parser;
import polyglot.frontend.Source;
import polyglot.lex.Lexer;
import polyglot.main.Version;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;

/**
 * Extension information for jl8 extension.
 */
public class JL8ExtensionInfo extends JL7ExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "jl8";
    }

    @Override
    public String compilerName() {
        return "jl8c";
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new JL8NodeFactory_c(JL8Lang_c.instance, new JL8ExtFactory_c(new JL7ExtFactory_c(new JL5ExtFactory_c())));
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new JL8TypeSystem_c();
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        reader = new polyglot.lex.EscapedUnicodeReader(reader);

        polyglot.lex.Lexer lexer = new Lexer_c(reader, source, eq);
        polyglot.parse.BaseParser grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    @Override
    public Set<String> keywords() {
        return new Lexer_c(null).keywords();
    }

    @Override
    public Version version() {
        return new JL8Version();
    }
}
