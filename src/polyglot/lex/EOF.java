package jltools.lex;

import java_cup.runtime.Symbol;

class EOF extends Token {
  EOF(int line) { super(line); }
  public Symbol symbol() { return new Symbol(jltools.parse.sym.EOF); }
  public String toString() { return "EOF"; }
}
