package jltools.ext.carray;

import jltools.ext.carray.parse.Lexer;
import jltools.ext.carray.parse.Grm;
import jltools.ext.carray.ast.*;
import jltools.ext.carray.types.*;
import jltools.ast.*;
import jltools.types.*;
import jltools.util.*;
import jltools.visit.*;
import jltools.frontend.*;

import java.util.*;
import java.io.*;

/**
 * Extension information for carray extension.
 */
public class ExtensionInfo extends jltools.ext.jl.ExtensionInfo {
    public String defaultFileExtension() {
        return "jl";
    }

    public Parser parser(Reader reader, Source source, ErrorQueue eq) {
        Lexer lexer = new Lexer(reader, source.name(), eq);
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
