package jltools.lex;

import java_cup.runtime.Symbol;

public class CharacterLiteral extends Literal {
  String val;
  CharacterLiteral(int line, String s) { super(line); this.val = s; }
  
  public String getValue() { return val; }

  public Symbol symbol() { return new Symbol(Sym.CHARACTER_LITERAL, this); }

  public String toString() {
    return "CharacterLiteral <"+Token.escape(val.toString())+">";
  }
}
