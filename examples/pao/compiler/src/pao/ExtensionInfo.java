package polyglot.ext.pao;

import polyglot.ext.pao.parse.Lexer_c;
import polyglot.ext.pao.parse.Grm;
import polyglot.ext.pao.ast.*;
import polyglot.ext.pao.types.*;
import polyglot.ext.pao.visit.*;

import polyglot.lex.Lexer;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;
import polyglot.main.*;

import java.util.*;
import java.io.*;

/**
 * Extension information for PAO extension.
 */
public class ExtensionInfo extends polyglot.ext.jl.ExtensionInfo {
    public String defaultFileExtension() {
        return "pao";
    }

    public String compilerName() {
        return "paoc";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new PaoNodeFactory_c();
    }
    protected TypeSystem createTypeSystem() {
        return new PaoTypeSystem_c();
    }

    public static final Pass.ID CAST_REWRITE = new Pass.ID("cast-rewrite");

    public List passes(Job job) {
        List passes = super.passes(job);
        beforePass(passes, Pass.PRE_OUTPUT_ALL,
                  new VisitorPass(CAST_REWRITE,
                                  job, new PaoBoxer(job, ts, nf)));
        return passes;
    }

    static { Topics t = new Topics(); }
}
