package jltools.lex;

import java_cup.runtime.Symbol;

public class CharacterLiteral extends Literal {
  char val;

  CharacterLiteral(int line, char c) 
  {
    super(line); 
    this.val = c; 
  }
  
  public char getValue() 
  {
    return val; 
  }

  public String getEscapedValue()
  {
    return Token.escape( String.valueOf( val));
  }

  public Symbol symbol() 
  {
    return new Symbol(Sym.CHARACTER_LITERAL, this); 
  }

  public String toString() 
  {
    return "CharacterLiteral <" + Token.escape( String.valueOf( val)) + ">";
  }
}
