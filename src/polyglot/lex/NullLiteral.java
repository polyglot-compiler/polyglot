package jltools.lex;

import java_cup.runtime.Symbol;

class NullLiteral extends Literal {
  NullLiteral(int line) { super(line); }

  Symbol symbol() { return new Symbol(Sym.NULL_LITERAL); }

  public String toString() { return "NullLiteral <null>"; }
}
