package jltools.lex;

import java_cup.runtime.Symbol;

public class NullLiteral extends Literal {
  NullLiteral(int line) { super(line); }

  public Symbol symbol() { return new Symbol(jltools.parse.sym.NULL_LITERAL, this); }

  public String toString() { return "NullLiteral <null>"; }
}
