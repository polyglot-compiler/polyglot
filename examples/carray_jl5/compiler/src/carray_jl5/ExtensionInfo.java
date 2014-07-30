package carray_jl5;

import java.io.Reader;
import java.util.Set;

import polyglot.ast.NodeFactory;
import polyglot.frontend.CupParser;
import polyglot.frontend.Parser;
import polyglot.frontend.Source;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import carray_jl5.ast.CarrayJL5NodeFactory_c;
import carray_jl5.parse.Grm;
import carray_jl5.parse.Lexer_c;
import carray_jl5.types.CarrayJL5TypeSystem_c;

/**
 * Extension information for carray extension.
 */
public class ExtensionInfo extends polyglot.ext.jl5.JL5ExtensionInfo {
    @Override
    public String[] defaultFileExtensions() {
        String ext = defaultFileExtension();
        return new String[] { ext, "jl" };
    }

    @Override
    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source, eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    @Override
    public Set<String> keywords() {
	return new Lexer_c(null).keywords();
    }

    @Override
    protected NodeFactory createNodeFactory() {
        return new CarrayJL5NodeFactory_c();
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new CarrayJL5TypeSystem_c();
    }

}
