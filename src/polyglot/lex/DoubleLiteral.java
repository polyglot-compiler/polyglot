package jltools.lex;

import java_cup.runtime.Symbol;

public class DoubleLiteral extends NumericLiteral {
  DoubleLiteral(int line, double d) { super(line); this.val = new Double(d); }

  public Symbol symbol()
  {
    return new Symbol(Sym.FLOATING_POINT_LITERAL, this); 
  }
}
