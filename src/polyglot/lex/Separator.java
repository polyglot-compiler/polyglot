package jltools.lex;

import java_cup.runtime.Symbol;

class Separator extends Token {
  char which;
  Separator(char which) { this.which = which; }

  Symbol token() {
    switch(which) {
    case '(': return new Symbol(Sym.LPAREN);
    case ')': return new Symbol(Sym.RPAREN);
    case '{': return new Symbol(Sym.LBRACE);
    case '}': return new Symbol(Sym.RBRACE);
    case '[': return new Symbol(Sym.LBRACK);
    case ']': return new Symbol(Sym.RBRACK);
    case ';': return new Symbol(Sym.SEMICOLON);
    case ',': return new Symbol(Sym.COMMA);
    case '.': return new Symbol(Sym.DOT);
    default:
      throw new Error("Invalid separator.");
    }
  }

  public String toString() {
    return "Separator <"+which+">";
  }
}
