package jltools.lex;

import java_cup.runtime.Symbol;

public class CharacterLiteral extends Literal {
  Character val;
  CharacterLiteral(int line, char c) { super(line); this.val = new Character(c); }
  
  public Character getValue() { return val; }

  public Symbol symbol() { return new Symbol(Sym.CHARACTER_LITERAL, val); }

  public String toString() {
    return "CharacterLiteral <"+Token.escape(val.toString())+">";
  }
}
