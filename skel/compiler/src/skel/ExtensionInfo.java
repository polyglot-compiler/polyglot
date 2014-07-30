package skelpkg;

import polyglot.lex.Lexer;
import skelpkg.parse.Lexer_c;
import skelpkg.parse.Grm;
import skelpkg.ast.*;
import skelpkg.types.*;
import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.main.*;
import polyglot.types.*;
import polyglot.util.*;

import java.io.*;
import java.util.Set;

/**
 * Extension information for skel extension.
 */
public class ExtensionInfo extends polyglot.frontend.JLExtensionInfo {
    static {
        // force Topics to load
        @SuppressWarnings("unused")
        Topics t = new Topics();
    }

    @Override
    public String defaultFileExtension() {
        return "sx";
    }

    @Override
    public String compilerName() {
        return "skelc";
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
        return new SkelNodeFactory_c(SkelLang_c.instance, new SkelExtFactory_c());
    }

    @Override
    protected TypeSystem createTypeSystem() {
        return new SkelTypeSystem_c();
    }

}
