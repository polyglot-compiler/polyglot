package jltools.lex;

import java_cup.runtime.Symbol;

public class FloatLiteral extends NumericLiteral {
  FloatLiteral(int line, float f) { super(line); this.val = new Float(f); }

  public Symbol symbol() 
  {
    return new Symbol(jltools.parse.sym.FLOATING_POINT_LITERAL, 
                                             this); }
}
