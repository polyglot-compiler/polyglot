package jltools.lex;

import java_cup.runtime.Symbol;

public class BooleanLiteral extends Literal {
  Boolean val;
  BooleanLiteral(int line, boolean b) { super(line); this.val = new Boolean(b); }
  
  public Boolean getValue() { return val; }

  public Symbol symbol() { return new Symbol(jltools.parse.sym.BOOLEAN_LITERAL, this); }

  public String toString() { return "BooleanLiteral <"+val.toString()+">"; }
}
