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
   * Creates a new CharacterLiteral storing a character with the value <c>
   */ 
  public CharacterLiteral (char c) {
    value = String.valueOf(c);
  }

  /**
   * Creates a new CharacterLiteral storing the String s. This is useful
   * when storing escaped character values.
   */
  public CharacterLiteral (String s) {
    value = s;
  }

  public String getCharValue() {
    return value;
  }

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write( "'" + value + "'");
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
    CharacterLiteral cl = new CharacterLiteral(value);
    cl.copyAnnotationsFrom(this);
    return cl;
  }

  public Node deepCopy() {
    CharacterLiteral cl = new CharacterLiteral(new String(value));
    cl.copyAnnotationsFrom(this);
    return cl;
  }
  
  private String value;
}
