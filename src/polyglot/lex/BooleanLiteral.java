package jltools.lex;

import java_cup.runtime.Symbol;

class BooleanLiteral extends Literal {
  Boolean val;
  BooleanLiteral(int line, boolean b) { super(line); this.val = new Boolean(b); }

  Symbol symbol() { return new Symbol(Sym.BOOLEAN_LITERAL, val); }

  public String toString() { return "BooleanLiteral <"+val.toString()+">"; }
}
