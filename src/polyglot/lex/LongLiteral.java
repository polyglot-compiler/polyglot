package jltools.lex;

import java_cup.runtime.Symbol;

class LongLiteral extends NumericLiteral {
  LongLiteral(int line, long l) { super(line); this.val = new Long(l); }

  Symbol symbol() { return new Symbol(Sym.INTEGER_LITERAL, val); }
}
