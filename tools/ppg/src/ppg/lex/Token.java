/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/
package ppg.lex;

import java.io.IOException;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;
import ppg.parse.Constant;

public class Token /* extends Symbol */implements LexerResult {

    private static ComplexSymbolFactory csf = new ComplexSymbolFactory();
    private Symbol symbol;
    private String filename;
    private int lineno;
    private Object value; // String, Boolean, Integer, Vector, null
    //private Position position;

    static int lastID;

    public Token(String filename, int lineno, Object value/*, Position pos*/) {
        this(-1, filename, lineno, -1, -1, value/*, pos*/);
    }

    public Token(int id, String filename, int lineno, int left, int right,
            Object value/*, Position pos*/) {
        // super(id, left, right, value);

        symbol =
                csf.newSymbol(value.toString(),
                              id,
                              new Location(filename, lineno, left),
                              new Location(filename, lineno, right),
                              this);
        lastID = id;
        this.filename = filename;
        this.lineno = lineno;
        this.value = value;
        //position = pos;
    }

    public int getCode() {
        return symbol.sym;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Object getValue() {
        return value;
    }

    public String getID() {
        return toString(symbol.sym);
        /*
        switch (symbol.sym) {

        	// tokens
        	default: break;
        }
        throw new IllegalStateException ("Unknown symbol code: " + symbol.sym);
        */
    }

    public static String toString(int type) {
        switch (type) {
        case Constant.INCLUDE:
            return "INCLUDE";
        case Constant.EXTEND:
            return "EXTEND";
        case Constant.DROP:
            return "DROP";
        case Constant.OVERRIDE:
            return "OVERRIDE";
        case Constant.TRANSFER:
            return "TRANSFER";
        case Constant.IMPORT:
            return "IMPORT";

        case Constant.COLON_COLON_EQUALS:
            return "CCEQ";
        case Constant.SEMI:
            return "SEMI";
        case Constant.COMMA:
            return "COMMA";
        case Constant.DOT:
            return "DOT";
        case Constant.COLON:
            return "COLON";

        case Constant.LBRACE:
            return "LBRACE";
        case Constant.RBRACE:
            return "RBRACE";
        case Constant.LBRACK:
            return "LBRACK";
        case Constant.RBRACK:
            return "RBRACK";
        case Constant.LT:
            return "LT";
        case Constant.GT:
            return "GT";

        case Constant.ID:
            return "ID";
        case Constant.CODE_STR:
            return "CODE_STR";
        case Constant.STRING_CONST:
            return "STRING_CONST";

        case Constant.WITH:
            return "WITH";
        case Constant.PARSER:
            return "PARSER";
        case Constant.INIT:
            return "INIT";
        case Constant.STAR:
            return "STAR";
        case Constant.BAR:
            return "BAR";
        case Constant.SCAN:
            return "SCAN";
        case Constant.NON:
            return "NON";
        case Constant.CODE:
            return "CODE";
        case Constant.LEFT:
            return "LEFT";
        case Constant.START:
            return "START";
        case Constant.NONTERMINAL:
            return "NONTERMINAL";
        case Constant.ACTION:
            return "ACTION";
        case Constant.TO:
            return "TO";
        case Constant.PACKAGE:
            return "PACKAGE";
        case Constant.NONASSOC:
            return "NONASSOC";
        case Constant.PRECEDENCE:
            return "PRECEDENCE";
        case Constant.PERCENT_PREC:
            return "PRECEDENCE";
        case Constant.TERMINAL:
            return "TERMINAL";
        case Constant.RIGHT:
            return "RIGHT";

        case Constant.EOF:
            return "EOF";
        case Constant.error:
            return "ERROR";

        default: {
            return "<undefined>";
        }
        }
    }

    @Override
    public String toString() {
        return (String) value;
        //return filename + ":" + lineno + ": \"" + value + "\"";
    }

    @Override
    public void unparse(java.io.OutputStream o) {

        if (value != null) {
            try {
                o.write((filename + ":" + lineno + ": " + getID() + ": \""
                        + value + "\"").getBytes());
                //o.write (value.toString ().getBytes ());
            }
            catch (IOException e) {
            }
        }
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public int lineNumber() {
        return lineno;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int i) {
        lineno = i;
    }
}
