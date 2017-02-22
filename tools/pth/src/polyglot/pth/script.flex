package polyglot.pth;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;

@SuppressWarnings({"unused", "fallthrough", "all"})
%%
%public
%class Lexer_c
%implements Lexer
%cup

%unicode

%line
%column

%{
  private static ComplexSymbolFactory csf = new ComplexSymbolFactory();
  StringBuffer string = new StringBuffer();

  private Symbol sym(String name, int id) {
    return csf.newSymbol(name, id, beginPos(), endPos());
  }

  private Symbol sym(String name, int id, Object o) {
    return csf.newSymbol(name, id, beginPos(), endPos(), o);
  }

  private Position beginPos() {
    return new Position(yyline+1, yycolumn+1);
  }

  private Position endPos() {
    int len = yytext().length();
    return new Position(yyline+1, yycolumn+1+len);
  }

  private static class Position extends Location {
    public Position(int line, int column) {
      super(line, column);
    }

    @Override
    public String toString() {
      return getLine() + ":" + getColumn();
    }
  }
%}

%eofval{
return (sym("EOF", sym.EOF));
%eofval}

LINE_TERMINATOR = \R
WHITE_SPACE     = {LINE_TERMINATOR} | [ \t\f]

/* comments */
COMMENT = {C_COMMENT} | {LINE_COMMENT} | {END_OF_LINE_COMMENT}

C_COMMENT            = "/*" ~"*/"
END_OF_LINE_COMMENT  = "//" ~{LINE_TERMINATOR}
LINE_COMMENT         = "#" ~{LINE_TERMINATOR}

IDENT                = [a-zA-Z0-9_\:\.\$\/\\\-]+

%state STRING_LIT

%%
<YYINITIAL> {
   /* identifiers */
   "build"                        { return sym("build", sym.BUILD); }
   {IDENT}                        { return sym("ID", sym.IDENT, yytext()); }

   /* literals */
   \"                             { string.setLength(0); yybegin(STRING_LIT); }

   /* comments */
   {COMMENT}                      { /* ignore */ }

   /* whitespace */
   {WHITE_SPACE}                  { /* ignore white space. */ }

   /* symbols */
   "+"                            { return sym("+", sym.PLUS); }
   ";"                            { return sym(";", sym.SEMICOLON); }
   ","                            { return sym(",", sym.COMMA); }
   "("                            { return sym("(", sym.LPAREN); }
   ")"                            { return sym(")", sym.RPAREN); }
   "["                            { return sym("[", sym.LBRACK); }
   "]"                            { return sym("]", sym.RBRACK); }
   "{"                            { return sym("{", sym.LBRACE); }
   "}"                            { return sym("}", sym.RBRACE); }
}

<STRING_LIT> {
   \"                             { yybegin(YYINITIAL);
                                    return sym("STRING", sym.STRING_LITERAL,
                                    string.toString()); }
   [^\n\r\"\\]+                   { string.append( yytext() ); }
   \\t                            { string.append('\t'); }
   \\n                            { string.append('\n'); }

   \\r                            { string.append('\r'); }
   \\\"                           { string.append('\"'); }
   \\\\                           { string.append('\\'); }
}

\^Cd   { return sym("EOF", sym.EOF); }

/* error fallback */
[^]                               { throw new Error(beginPos() +
                                                    ": Illegal character <"+
                                                    yytext()+">"); }


