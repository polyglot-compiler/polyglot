package jltools.lex;

import java_cup.runtime.Symbol;

class FloatLiteral extends NumericLiteral {
  FloatLiteral(int line, float f) { super(line); this.val = new Float(f); }

  Symbol symbol() { return new Symbol(Sym.FLOATING_POINT_LITERAL, val); }
}
