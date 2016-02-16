package java_cup;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;
import java.lang.Error;
import java.io.InputStreamReader;

@SuppressWarnings("all")
%%

%class Lexer
%public
%unicode
%line
%column
%cup
%{
    public Lexer(ComplexSymbolFactory sf){
	this(new InputStreamReader(System.in));
        symbolFactory = sf;
    }
    private StringBuffer sb;
    private ComplexSymbolFactory symbolFactory;
    private int csline,cscolumn;
    public Symbol symbol(String name, int code){
	return symbolFactory.newSymbol(name, code,new Location(yyline+1,yycolumn+1-yylength()),new Location(yyline+1,yycolumn+1));
    }
    public Symbol symbol(String name, int code, String lexem){
	return symbolFactory.newSymbol(name, code, new Location(yyline+1, yycolumn +1), new Location(yyline+1,yycolumn+yylength()), lexem);
    }
    protected void emit_warning(String message){
	ErrorManager.getManager().emit_warning("Scanner at " + (yyline+1) + "(" + (yycolumn+1) + "): " + message);
    }
    protected void emit_error(String message){
	ErrorManager.getManager().emit_error("Scanner at " + (yyline+1) + "(" + (yycolumn+1) +  "): " + message);
    }
%}

Newline = \r | \n | \r\n
Whitespace = [ \t\f] | {Newline}

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment = "/*" {CommentContent} \*+ "/"
EndOfLineComment = "//" [^\r\n]* {Newline}
CommentContent = ( [^*] | \*+[^*/] )*

ident = ([:jletter:] | "_" ) ([:jletterdigit:] | [:jletter:] | "_" )*


%eofval{
    return symbolFactory.newSymbol("EOF",sym.EOF);
%eofval}

%state CODESEG

%%  

<YYINITIAL> {

  {Whitespace}  {                                              }
  "?"           { return symbol("QUESTION", sym.QUESTION);          }
  ";"           { return symbol("SEMI", sym.SEMI);                  }
  ","           { return symbol("COMMA", sym.COMMA);                }
  "*"           { return symbol("STAR", sym.STAR);                  }
  "."           { return symbol("DOT", sym.DOT);                    }
  "|"           { return symbol("BAR", sym.BAR);                    }
  "["           { return symbol("LBRACK", sym.LBRACK);              }
  "]"           { return symbol("RBRACK", sym.RBRACK);              }
  ":"           { return symbol("COLON", sym.COLON);                }
  "::="         { return symbol("COLON_COLON_EQUALS", sym.COLON_COLON_EQUALS);   }
  "%prec"       { return symbol("PERCENT_PREC", sym.PERCENT_PREC);  }
  ">"           { return symbol("GT", sym.GT);                      }
  "<"           { return symbol("LT", sym.LT);                      }
  {Comment}     {                                              }
  "{:"          { sb = new StringBuffer(); csline=yyline+1; cscolumn=yycolumn+1; yybegin(CODESEG);    }
  "package"     { return symbol("PACKAGE", sym.PACKAGE);            } 
  "import"      { return symbol("IMPORT", sym.IMPORT);	       }
  "class"       { return symbol("CLASS", sym.CLASS); 	       }
  "code"        { return symbol("CODE", sym.CODE);		       }
  "action"      { return symbol("ACTION", sym.ACTION);	       }
  "parser"      { return symbol("PARSER", sym.PARSER);	       }
  "terminal"    { return symbol("PARSER", sym.TERMINAL);	       }
  "non"         { return symbol("NON", sym.NON);		       }
  "nonterminal" { return symbol("NONTERMINAL", sym.NONTERMINAL);    }
  "init"        { return symbol("INIT", sym.INIT);		       }
  "scan"        { return symbol("SCAN", sym.SCAN);		       }
  "with"        { return symbol("WITH", sym.WITH);		       }
  "start"       { return symbol("START", sym.START);		       }
  "precedence"  { return symbol("PRECEDENCE", sym.PRECEDENCE);      }
  "left"        { return symbol("LEFT", sym.LEFT);		       }
  "right"       { return symbol("RIGHT", sym.RIGHT);		       }
  "nonassoc"    { return symbol("NONASSOC", sym.NONASSOC);          }
  "extends"     { return symbol("EXTENDS", sym.EXTENDS);            }
  "implements"  { return symbol("IMPLEMENTS", sym.IMPLEMENTS);      }
  "super"       { return symbol("SUPER", sym.SUPER);                }
  {ident}       { return symbol("ID", sym.ID, yytext());            }
  
}

<CODESEG> {
  ":}"         { yybegin(YYINITIAL); return symbolFactory.newSymbol("CODE_STRING", sym.CODE_STRING, new Location(csline, cscolumn), new Location(yyline+1, yycolumn+1+yylength()), sb.toString()); }
  [^]            { sb.append(yytext()); }
}

// error fallback
[^]          { emit_warning("Unrecognized character '" +yytext()+"' -- ignored"); }
