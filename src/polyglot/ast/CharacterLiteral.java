/*
 * CharacterLiteral.java
 */ 

package jltools.ast;

import jltools.types.*;
import jltools.util.*;

/** 
 * CharacterLiteral
 * 
 * Overview: An CharacterLiteral represents a literal in java of character
 * type.
 */
public class CharacterLiteral extends Literal {


  /**
   * Creates a new CharacterLiteral storing a character with the value <c>.
   */
  public CharacterLiteral( char c) 
  {
    this( c, String.valueOf( c));
  }

  /**
   * Creates a new CharacterLiteral storing a character with the value <c>
   * and escaped value <escaped>.
   */ 
  public CharacterLiteral( char c, String escaped) 
  {
    value = c;
    escapedValue = escaped;
  }

  public char getCharValue() {
    return value;
  }

  public String getEscapedCharValue() {
    return escapedValue;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write( "'" + escapedValue + "'");
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( CHAR LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node typeCheck(  LocalContext c) throws TypeCheckException
  {
    setCheckedType( c.getTypeSystem().getChar());
    return this;
  }  
  
  Object visitChildren(NodeVisitor v) 
  {
    // nothing to do
    return Annotate.getVisitorInfo( this);
  }

  public Node copy() {
    CharacterLiteral cl = new CharacterLiteral(value, escapedValue);
    cl.copyAnnotationsFrom(this);
    return cl;
  }

  public Node deepCopy() {
    CharacterLiteral cl = new CharacterLiteral(value, 
                                               new String(escapedValue));
    cl.copyAnnotationsFrom(this);
    return cl;
  }
  
  protected char value;
  protected String escapedValue;
}
