package jltools.lex;

import java_cup.runtime.Symbol;

class IntegerLiteral extends NumericLiteral {
  IntegerLiteral(int line, int i) { super(line); this.val = new Integer(i); }

  Symbol symbol() { return new Symbol(Sym.INTEGER_LITERAL, val); }
}
