package jltools.lex;

import java_cup.runtime.Symbol;

public class DoubleLiteral extends NumericLiteral {
  DoubleLiteral(int line, double d) { super(line); this.val = new Double(d); }

  public Symbol symbol()
  {
    return new Symbol(jltools.parse.sym.FLOATING_POINT_LITERAL, this); 
  }
}
