package jltools.lex;

import java_cup.runtime.Symbol;
import jltools.util.Position;

public class CharacterLiteral extends Literal {
  char val;

  public CharacterLiteral(Position position, char c, int sym)
  {
    super(position, sym); 
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

  public String toString() 
  {
    return "CharacterLiteral <" + Token.escape( String.valueOf( val)) + ">";
  }
}
