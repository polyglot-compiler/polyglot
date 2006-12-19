/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.lex;

import java_cup.runtime.Symbol;
import polyglot.util.Position;

/** Token class for char literals. */
public class CharacterLiteral extends Literal {
    protected Character val;

  public CharacterLiteral(Position position, char c, int sym)
  {
    super(position, sym); 
    this.val = new Character(c); 
  }
  
  public Character getValue() 
  {
    return val;
  }

  public String getEscapedValue()
  {
    return Token.escape( String.valueOf( val));
  }

  public String toString() 
  {
    return "char literal '" + getEscapedValue() + "'";
  }
}
