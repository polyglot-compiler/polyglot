package jltools.lex;

import java_cup.runtime.Symbol;

class Separator extends Token {
  char which;
  Separator(int line, char which) { super(line); this.which = which; }

  public Symbol symbol() {
    switch(which) {
    case '(': return new Symbol(jltools.parse.sym.LPAREN, this);
    case ')': return new Symbol(jltools.parse.sym.RPAREN, this);
    case '{': return new Symbol(jltools.parse.sym.LBRACE, this);
    case '}': return new Symbol(jltools.parse.sym.RBRACE, this);
    case '[': return new Symbol(jltools.parse.sym.LBRACK, this);
    case ']': return new Symbol(jltools.parse.sym.RBRACK, this);
    case ';': return new Symbol(jltools.parse.sym.SEMICOLON, this);
    case ',': return new Symbol(jltools.parse.sym.COMMA, this);
    case '.': return new Symbol(jltools.parse.sym.DOT, this);
    default:
      throw new Error("Invalid separator.");
    }
  }

  public String toString() {
    return "Separator <"+which+">";
  }
}
