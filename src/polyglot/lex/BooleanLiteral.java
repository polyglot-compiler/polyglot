package Lex;

import java_cup.runtime.Symbol;

class BooleanLiteral extends Literal {
  Boolean val;
  BooleanLiteral(boolean b) { this.val = new Boolean(b); }

  Symbol token() { return new Symbol(Sym.BOOLEAN_LITERAL, val); }

  public String toString() { return "BooleanLiteral <"+val.toString()+">"; }
}
