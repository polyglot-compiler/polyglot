package jltools.lex;

import java_cup.runtime.Symbol;

public class IntegerLiteral extends NumericLiteral {
  IntegerLiteral(int line, int i) { super(line); this.val = new Integer(i); }

  public Symbol symbol() { return new Symbol(jltools.parse.sym.INTEGER_LITERAL, this); }
}
