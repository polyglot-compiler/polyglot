package jltools.lex;

import java_cup.runtime.Symbol;

class EOF extends Token {
  EOF(int line) { super(line); }
  Symbol symbol() { return new Symbol(Sym.EOF); }
  public String toString() { return "EOF"; }
}
