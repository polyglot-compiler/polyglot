package jltools.lex;

import java_cup.runtime.Symbol;

class Separator extends Token {
  char which;
  Separator(int line, char which) { super(line); this.which = which; }

  public Symbol symbol() {
    switch(which) {
    case '(': return new Symbol(Sym.LPAREN, this);
    case ')': return new Symbol(Sym.RPAREN, this);
    case '{': return new Symbol(Sym.LBRACE, this);
    case '}': return new Symbol(Sym.RBRACE, this);
    case '[': return new Symbol(Sym.LBRACK, this);
    case ']': return new Symbol(Sym.RBRACK, this);
    case ';': return new Symbol(Sym.SEMICOLON, this);
    case ',': return new Symbol(Sym.COMMA, this);
    case '.': return new Symbol(Sym.DOT, this);
    default:
      throw new Error("Invalid separator.");
    }
  }

  public String toString() {
    return "Separator <"+which+">";
  }
}
