package jltools.lex;

import java_cup.runtime.Symbol;

public class StringLiteral extends Literal {
  String val;
  StringLiteral(int line, String s) { super(line); this.val = s; }

  public Symbol symbol() { return new Symbol(Sym.STRING_LITERAL, val); }

  public String getValue() { return val; }

  public String toString() {
    return "StringLiteral <"+Token.escape(val)+">";
  }
}
