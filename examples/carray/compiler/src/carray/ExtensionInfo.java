package polyglot.ext.carray;

import polyglot.ext.carray.parse.Lexer_c;
import polyglot.ext.carray.parse.Grm;
import polyglot.ext.carray.ast.*;
import polyglot.ext.carray.types.*;
import polyglot.lex.Lexer;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import polyglot.frontend.*;

import java.util.*;
import java.io.*;

/**
 * Extension information for carray extension.
 */
public class ExtensionInfo extends polyglot.ext.jl.ExtensionInfo {
    public String defaultFileExtension() {
        return "jl";
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.name(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);
        return new CupParser(grm, source, eq);
    }

    protected NodeFactory createNodeFactory() {
        return new CarrayNodeFactory_c();
    }
    protected TypeSystem createTypeSystem() {
        return new CarrayTypeSystem();
    }

}
