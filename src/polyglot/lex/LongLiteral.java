package jltools.lex;

import java_cup.runtime.Symbol;

public class LongLiteral extends NumericLiteral {
  LongLiteral(int line, long l) { super(line); this.val = new Long(l); }

  public Symbol symbol() { return new Symbol(jltools.parse.sym.LONG_LITERAL, this); }
}
