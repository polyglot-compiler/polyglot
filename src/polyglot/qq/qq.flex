/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 *
 */

/* Java 1.4 scanner for JFlex.
 * Based on JLS, 2ed, Chapter 3.
 */

package polyglot.qq;

import polyglot.lex.Lexer;
import polyglot.lex.*;
import polyglot.ast.*;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import java.util.*;
import java.io.StringReader;
import java.math.BigInteger;

@SuppressWarnings({"unused", "fallthrough", "all"})
%%

%public
%class Lexer_c
%implements Lexer
%type Token
%function nextToken

%unicode
%pack

%line
%column

%state STRING, CHARACTER, TRADITIONAL_COMMENT, END_OF_LINE_COMMENT

%{
    StringBuffer sb = new StringBuffer();
    String file;
    String path;
    HashMap<String, Integer> keywords;
    LinkedList<Object> subst;

    public Lexer_c(String s, Position pos, Object... subst) {
        this(new EscapedUnicodeReader(new StringReader(s)));
        if (pos != null) {
            this.file = pos.toString() + ": quasiquote(" + stringSubst(s ,subst) + ")";
            this.path = pos.path();
        }
        else {
            this.file = "quasiquote(" + stringSubst(s ,subst) + ")";
            this.path = null;
        }

        this.subst = new LinkedList<Object>();
        Collections.addAll(this.subst, subst);
        this.keywords = new HashMap<String, Integer>();
        init_keywords();
    }

    private String stringSubst(String s, Object... subst) {
        StringBuffer sb = new StringBuffer();
        int substIdx = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '%' && substIdx < subst.length) {
                i++;
                sb.append(subst[substIdx++]);
            }
            else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    private void error(String msg, Position pos) {
       throw new InternalCompilerError(msg, pos);
    }

    protected String substKind(char kind) { return substKind(kind, false); }

    protected String substKind(char kind, boolean list) {
        switch (kind) {
            case 's': return "String";
            case 'E': return "Expr";
            case 'S': return "Stmt";
            case 'T': return "TypeNode";
            case 'D': return "ClassDecl";
            case 'M': return "ClassMember";
            case 'F': return "Formal";
            default:
                error("Bad quasiquoting substitution type: \"" + kind + "\".",
                      pos());
                return null;
        }
    }

    public Token substList(char kind) {
        if (subst.isEmpty()) {
            error("Not enough arguments to quasiquoter.", pos());
        }

        Object o = subst.removeFirst();
        String expected = substKind(kind, true);

        if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<? extends Term> l = (List<? extends Term>) o;

            for (Term p : l) {
                switch (kind) {
                    case 'E':
                        if (p instanceof Expr) continue;
                        break;
                    case 'S':
                        if (p instanceof Stmt) continue;
                        break;
                    case 'T':
                        if (p instanceof TypeNode) continue;
                        break;
                    case 'D':
                        if (p instanceof ClassDecl) continue;
                        break;
                    case 'M':
                        if (p instanceof ClassMember) continue;
                        break;
                    case 'F':
                        if (p instanceof Formal) continue;
                        break;
                    default:
                        break;
                }

                error("Bad quasiquoting substitution: expected List of " + expected + ".", pos());
            }

            return new QQListToken(pos(), l, sym.COMMA_LIST);
        }
        else {
            error("Bad quasiquoting substitution: expected List of " + expected + ".", pos());
            return null;
        }
    }

    public Token subst(char kind) {
        if (subst.isEmpty()) {
            error("Not enough arguments to quasiquoter.", pos());
        }

        Object o = subst.removeFirst();
        String expected = substKind(kind);

        switch (kind) {
            case 's': {
                if (o instanceof String) {
                    String s = (String) o;
                    return new Identifier(pos(), s, sym.IDENTIFIER);
                }
                break;
            }
            case 'E': {
                if (o instanceof Expr) {
                    Expr e = (Expr) o;
                    return new QQNodeToken(pos(), e, sym.COMMA_EXPR);
                }
                break;
            }
            case 'S': {                if (o instanceof Stmt) {
                    Stmt s = (Stmt) o;
                    return new QQNodeToken(pos(), s, sym.COMMA_STMT);
                }
                break;
            }
            case 'T': {                if (o instanceof TypeNode) {
                    TypeNode t = (TypeNode) o;
                    return new QQNodeToken(pos(), t, sym.COMMA_TYPE);
                }
                break;
            }
            case 'D': {
                if (o instanceof ClassDecl) {
                    ClassDecl d = (ClassDecl) o;
                    return new QQNodeToken(pos(), d, sym.COMMA_DECL);
                }
                break;
            }
            case 'M': {
                if (o instanceof ClassMember) {
                    ClassMember m = (ClassMember) o;
                    return new QQNodeToken(pos(), m, sym.COMMA_MEMB);
                }
                break;
            }
            case 'F': {
                if (o instanceof Formal) {
                    Formal f = (Formal) o;
                    return new QQNodeToken(pos(), f, sym.COMMA_FORM);
                }
                break;
            }
            default:
                // error: should be caught in substKind
                return null;
        }

        error("Bad quasiquoting substitution: expected " + expected + ".", pos());
        return null;
    }

    public Set<String> keywords() {
        if (keywords == null) {
	    keywords = new HashMap<>();
	    init_keywords();
	}

	return Collections.unmodifiableSet(keywords.keySet());
    }

    protected void init_keywords() {
        keywords.put("abstract",      sym.ABSTRACT);
        keywords.put("assert",        sym.ASSERT);
        keywords.put("boolean",       sym.BOOLEAN);
        keywords.put("break",         sym.BREAK);
        keywords.put("byte",          sym.BYTE);
        keywords.put("case",          sym.CASE);
        keywords.put("catch",         sym.CATCH);
        keywords.put("char",          sym.CHAR);
        keywords.put("class",         sym.CLASS);
        keywords.put("const",         sym.CONST);
        keywords.put("continue",      sym.CONTINUE);
        keywords.put("default",       sym.DEFAULT);
        keywords.put("do",            sym.DO);
        keywords.put("double",        sym.DOUBLE);
        keywords.put("else",          sym.ELSE);
        keywords.put("extends",       sym.EXTENDS);
        keywords.put("final",         sym.FINAL);
        keywords.put("finally",       sym.FINALLY);
        keywords.put("float",         sym.FLOAT);
        keywords.put("for",           sym.FOR);
        keywords.put("goto",          sym.GOTO);
        keywords.put("if",            sym.IF);
        keywords.put("implements",    sym.IMPLEMENTS);
        keywords.put("import",        sym.IMPORT);
        keywords.put("instanceof",    sym.INSTANCEOF);
        keywords.put("int",           sym.INT);
        keywords.put("interface",     sym.INTERFACE);
        keywords.put("long",          sym.LONG);
        keywords.put("native",        sym.NATIVE);
        keywords.put("new",           sym.NEW);
        keywords.put("package",       sym.PACKAGE);
        keywords.put("private",       sym.PRIVATE);
        keywords.put("protected",     sym.PROTECTED);
        keywords.put("public",        sym.PUBLIC);
        keywords.put("return",        sym.RETURN);
        keywords.put("short",         sym.SHORT);
        keywords.put("static",        sym.STATIC);
        keywords.put("strictfp",      sym.STRICTFP);
        keywords.put("super",         sym.SUPER);
        keywords.put("switch",        sym.SWITCH);
        keywords.put("synchronized",  sym.SYNCHRONIZED);
        keywords.put("this",          sym.THIS);
        keywords.put("throw",         sym.THROW);
        keywords.put("throws",        sym.THROWS);
        keywords.put("transient",     sym.TRANSIENT);
        keywords.put("try",           sym.TRY);
        keywords.put("void",          sym.VOID);
        keywords.put("volatile",      sym.VOLATILE);
        keywords.put("while",         sym.WHILE);
    }

    @Override
    public String file() {
        return file;
    }

    @Override
    public String path() {
        return path;
    }

    private Position pos() {
        return new Position(path, file, yyline+1, yycolumn, yyline+1,
                            yycolumn + yytext().length());
    }

    private Position pos(int len) {
        return new Position(path, file, yyline+1, yycolumn-len-1, yyline+1,
                            yycolumn+1);
    }

    private Token key(int symbol) {
        return new Keyword(pos(), yytext(), symbol);
    }

    private Token op(int symbol) {
        return new Operator(pos(), yytext(), symbol);
    }

    private Token id() {
        return new Identifier(pos(), yytext(), sym.IDENTIFIER);
    }

    private Token int_lit(String s, int radix) {
        BigInteger x = new BigInteger(s, radix);
        boolean boundary = (radix == 10 && s.equals("2147483648"));
        int bits = radix == 10 ? 31 : 32;
        if (x.bitLength() > bits && ! boundary) {
            error("Integer literal \"" + yytext() + "\" out of range.", pos());
            return null;
        }
        return new IntegerLiteral(pos(), x.intValue(),
                boundary ? sym.INTEGER_LITERAL_BD : sym.INTEGER_LITERAL);
    }

    private Token long_lit(String s, int radix) {
        BigInteger x = new BigInteger(s, radix);
        boolean boundary = (radix == 10 && s.equals("9223372036854775808"));
        int bits = radix == 10 ? 63 : 64;
        if (x.bitLength() > bits && ! boundary) {
            error("Long literal \"" + yytext() + "\" out of range.", pos());
            return null;
        }
        return new LongLiteral(pos(), x.longValue(),
                boundary ? sym.LONG_LITERAL_BD : sym.LONG_LITERAL);
    }

    private Token float_lit(String s) {
        try {
            Float x = Float.valueOf(s);
	    boolean zero = true;
	    for (int i = 0; i < s.length(); i++) {
		if ('1' <= s.charAt(i) && s.charAt(i) <= '9') {
		    zero = false;
		    break;
		}
	    }
	    if (x.isInfinite() || x.isNaN() || (x.floatValue() == 0 && ! zero)) {
		error("Illegal float literal \"" + yytext() + "\"", pos());
		return null;
	    }
            return new FloatLiteral(pos(), x.floatValue(), sym.FLOAT_LITERAL);
        }
        catch (NumberFormatException e) {
            error("Illegal float literal \"" + yytext() + "\"", pos());
            return null;
        }
    }

    private Token double_lit(String s) {
        try {
            Double x = Double.valueOf(s);
	    boolean zero = true;
	    for (int i = 0; i < s.length(); i++) {
		if ('1' <= s.charAt(i) && s.charAt(i) <= '9') {
		    zero = false;
		    break;
		}
	    }
	    if (x.isInfinite() || x.isNaN() || (x.floatValue() == 0 && ! zero)) {
		error("Illegal double literal \"" + yytext() + "\"", pos());
		return null;
	    }
            return new DoubleLiteral(pos(), x.doubleValue(), sym.DOUBLE_LITERAL);
        }
        catch (NumberFormatException e) {
            error("Illegal double literal \"" + yytext() + "\"", pos());
            return null;
        }
    }

    private Token char_lit(String s) {
        if (s.length() == 1) {
            char x = s.charAt(0);
            return new CharacterLiteral(pos(), x, sym.CHARACTER_LITERAL);
        }
        else {
            error("Illegal character literal \'" + s + "\'", pos(s.length()));
            return null;
        }
    }

    private Token boolean_lit(boolean x) {
        return new BooleanLiteral(pos(), x, sym.BOOLEAN_LITERAL);
    }

    private Token null_lit() {
        return new NullLiteral(pos(), sym.NULL_LITERAL);
    }

    private Token string_lit() {
        return new StringLiteral(pos(sb.length()), sb.toString(),
                                 sym.STRING_LITERAL);
    }

    private String chop(int i, int j) {
        return yytext().substring(i,yylength()-j);
    }

    private String chop(int j) {
        return chop(0, j);
    }

    private String chop() {
        return chop(0, 1);
    }
%}

%eofval{
    return new EOF(pos(), sym.EOF);
%eofval}

/* From Chapter 3 of the JLS: */

/* 3.4 Line Terminators */
/* LineTerminator:
      the ASCII LF character, also known as "newline"
      the ASCII CR character, also known as "return"
      the ASCII CR character followed by the ASCII LF character
*/
LineTerminator = \n|\r|\r\n

/* 3.6 White Space */
/*
WhiteSpace:
    the ASCII SP character, also known as "space"
    the ASCII HT character, also known as "horizontal tab"
    the ASCII FF character, also known as "form feed"
    LineTerminator
*/
WhiteSpace = [ \t\f] | {LineTerminator}

/* 3.8 Identifiers */
Identifier = [:jletter:] [:jletterdigit:]*

/* 3.10.1 Integer Literals */
DecimalNumeral = 0 | [1-9][0-9]*
HexNumeral = 0 [xX] [0-9a-fA-F]+
OctalNumeral = 0 [0-7]+

/* 3.10.2 Floating-Point Literals */
FloatingPointLiteral = [0-9]+ "." [0-9]* {ExponentPart}?
                     | "." [0-9]+ {ExponentPart}?
                     | [0-9]+ {ExponentPart}

ExponentPart = [eE] {SignedInteger}
SignedInteger = [-+]? [0-9]+

/* 3.10.4 Character Literals */
OctalEscape = \\ [0-7]
            | \\ [0-7][0-7]
            | \\ [0-3][0-7][0-7]

%%

<YYINITIAL> {
    /* quasiquoting operators */
    "%s"                           { return subst('s'); }
    "%E"                           { return subst('E'); }
    "%S"                           { return subst('S'); }
    "%T"                           { return subst('T'); }
    "%D"                           { return subst('D'); }
    "%M"                           { return subst('M'); }
    "%F"                           { return subst('F'); }
    "%LE"                          { return substList('E'); }
    "%LS"                          { return substList('S'); }
    "%LT"                          { return substList('T'); }
    "%LD"                          { return substList('D'); }
    "%LM"                          { return substList('M'); }
    "%LF"                          { return substList('F'); }

    /* 3.7 Comments */
    "/*"    { yybegin(TRADITIONAL_COMMENT); }
    "//"    { yybegin(END_OF_LINE_COMMENT); }

    /* 3.10.4 Character Literals */
    \'      { yybegin(CHARACTER); sb.setLength(0); }

    /* 3.10.5 String Literals */
    \"      { yybegin(STRING); sb.setLength(0); }

    /* 3.10.3 Boolean Literals */
    "true"  { return boolean_lit(true);  }
    "false" { return boolean_lit(false); }

    /* 3.10.6 Null Literal */
    "null"  { return null_lit(); }

    /* 3.9 Keywords */
    /* 3.8 Identifiers */
    {Identifier}   { Integer i = keywords.get(yytext());
                    if (i == null) return id();
                    else return key(i.intValue()); }

    /* 3.11 Separators */
    "("    { return op(sym.LPAREN);    }
    ")"    { return op(sym.RPAREN);    }
    "{"    { return op(sym.LBRACE);    }
    "}"    { return op(sym.RBRACE);    }
    "["    { return op(sym.LBRACK);    }
    "]"    { return op(sym.RBRACK);    }
    ";"    { return op(sym.SEMICOLON); }
    ","    { return op(sym.COMMA);     }
    "."    { return op(sym.DOT);       }

    /* 3.12 Operators */
    "="    { return op(sym.EQ);         }
    ">"    { return op(sym.GT);         }
    "<"    { return op(sym.LT);         }
    "!"    { return op(sym.NOT);        }
    "~"    { return op(sym.COMP);       }
    "?"    { return op(sym.QUESTION);   }
    ":"    { return op(sym.COLON);      }
    "=="   { return op(sym.EQEQ);       }
    "<="   { return op(sym.LTEQ);       }
    ">="   { return op(sym.GTEQ);       }
    "!="   { return op(sym.NOTEQ);      }
    "&&"   { return op(sym.ANDAND);     }
    "||"   { return op(sym.OROR);       }
    "++"   { return op(sym.PLUSPLUS);   }
    "--"   { return op(sym.MINUSMINUS); }
    "+"    { return op(sym.PLUS);       }
    "-"    { return op(sym.MINUS);      }
    "*"    { return op(sym.MULT);       }
    "/"    { return op(sym.DIV);        }
    "&"    { return op(sym.AND);        }
    "|"    { return op(sym.OR);         }
    "^"    { return op(sym.XOR);        }
    "%"    { return op(sym.MOD);        }
    "<<"   { return op(sym.LSHIFT);     }
    ">>"   { return op(sym.RSHIFT);     }
    ">>>"  { return op(sym.URSHIFT);    }
    "+="   { return op(sym.PLUSEQ);     }
    "-="   { return op(sym.MINUSEQ);    }
    "*="   { return op(sym.MULTEQ);     }
    "/="   { return op(sym.DIVEQ);      }
    "&="   { return op(sym.ANDEQ);      }
    "|="   { return op(sym.OREQ);       }
    "^="   { return op(sym.XOREQ);      }
    "%="   { return op(sym.MODEQ);      }
    "<<="  { return op(sym.LSHIFTEQ);   }
    ">>="  { return op(sym.RSHIFTEQ);   }
    ">>>=" { return op(sym.URSHIFTEQ);  }

    /* 3.10.1 Integer Literals */
    {OctalNumeral} [lL]          { Token t = long_lit(chop(), 8);
                                   if (t != null) return t; }
    {HexNumeral} [lL]            { Token t = long_lit(chop(2,1), 16);
                                   if (t != null) return t; }
    {DecimalNumeral} [lL]        { Token t = long_lit(chop(), 10);
                                   if (t != null) return t; }
    {OctalNumeral}               { Token t = int_lit(yytext(), 8);
                                   if (t != null) return t; }
    {HexNumeral}                 { Token t = int_lit(chop(2,0), 16);
                                   if (t != null) return t; }
    {DecimalNumeral}             { Token t = int_lit(yytext(), 10);
                                   if (t != null) return t; }

    /* 3.10.2 Floating-Point Literals */
    {FloatingPointLiteral} [fF]  { Token t = float_lit(chop());
                                   if (t != null) return t; }
    {DecimalNumeral} [fF]        { Token t = float_lit(chop());
                                   if (t != null) return t; }
    {FloatingPointLiteral} [dD]  { Token t = double_lit(chop());
                                   if (t != null) return t; }
    {DecimalNumeral} [dD]        { Token t = double_lit(chop());
                                   if (t != null) return t; }
    {FloatingPointLiteral}       { Token t = double_lit(yytext());
                                   if (t != null) return t; }

    /* 3.6 White Space */
    {WhiteSpace}                 { /* ignore */ }
}

<TRADITIONAL_COMMENT> {
    "*/"                         { yybegin(YYINITIAL); }
    [^]                          { /* ignore */ }
}

<END_OF_LINE_COMMENT> {
    {LineTerminator}             { yybegin(YYINITIAL); }
    .                            { /* ignore */ }
}

<CHARACTER> {
    /* End of the character literal */
    \'                           { yybegin(YYINITIAL);
                                   Token t = char_lit(sb.toString());
                                   if (t != null) return t; }

    /* 3.10.6 Escape Sequences for Character and String Literals */
    "\\b"                        { sb.append('\b'); }
    "\\t"                        { sb.append('\t'); }
    "\\n"                        { sb.append('\n'); }
    "\\f"                        { sb.append('\f'); }
    "\\r"                        { sb.append('\r'); }
    "\\\""                       { sb.append('\"'); }
    "\\'"                        { sb.append('\''); }
    "\\\\"                       { sb.append('\\'); }
    {OctalEscape}                { try {
                                       int x = Integer.parseInt(chop(1,0), 8);
                                       sb.append((char) x);
                                   }
                                   catch (NumberFormatException e) {
                                       error("Illegal octal escape \""
                                                  + yytext() + "\"", pos());
                                   }
                                 }

    /* Illegal escape character */
    \\.                          { error("Illegal escape character \"" +
                                              yytext() + "\"", pos()); }

    /* Unclosed character literal */
    {LineTerminator}             { yybegin(YYINITIAL);
                                  error("Unclosed character literal",
                                             pos(sb.length())); }

    /* Anything else is okay */
    [^\r\n\'\\]+                 { sb.append( yytext() ); }
}

<STRING> {
    /* End of string */
    \"                           { yybegin(YYINITIAL);
                                   return string_lit(); }

    /* 3.10.6 Escape Sequences for Character and String Literals */
    "\\b"                        { sb.append( '\b' ); }
    "\\t"                        { sb.append( '\t' ); }
    "\\n"                        { sb.append( '\n' ); }
    "\\f"                        { sb.append( '\f' ); }
    "\\r"                        { sb.append( '\r' ); }
    "\\\""                       { sb.append( '\"' ); }
    "\\'"                        { sb.append( '\'' ); }
    "\\\\"                       { sb.append( '\\' ); }
    {OctalEscape}                { try {
                                       int x = Integer.parseInt(chop(1,0), 8);
                                       sb.append((char) x);
                                   }
                                   catch (NumberFormatException e) {
                                       error("Illegal octal escape \""
                                                  + yytext() + "\"", pos());
                                   }
                                 }

    /* Illegal escape character */
    \\.                          { error("Illegal escape character \"" +
                                              yytext() + "\"", pos()); }

    /* Unclosed string literal */
    {LineTerminator}             { yybegin(YYINITIAL);
                                   error("Unclosed string literal",
                                              pos(sb.length())); }

    /* Anything else is okay */
    [^\r\n\"\\]+                 { sb.append( yytext() ); }
}

/* Fallthrough case: anything not matched above is an error */
[^]                              { error("Illegal character \"" +
                                              yytext() + "\"", pos()); }
