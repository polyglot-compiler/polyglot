package jltools.lex;

import java_cup.runtime.Symbol;

class DoubleLiteral extends NumericLiteral {
  DoubleLiteral(int line, double d) { super(line); this.val = new Double(d); }

  Symbol symbol() { return new Symbol(Sym.FLOATING_POINT_LITERAL, val); }
}
