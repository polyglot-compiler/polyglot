package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/** 
 * An <code>CharacterLiteral</code> represents a literal in java of
 * <code>char</code> type.
 */
public class CharacterLiteral extends NumericalLiteral 
{  
  protected final char value;
  protected final String escaped;

  /**
   * Creates a new <code>CharacterLiteral</code> storing a character with the
   * value <code>value</code>.
   */
  public CharacterLiteral( char value) 
  {
    this( value, String.valueOf( value));
  }

  /**
   * Creates a new <code>CharacterLiteral</code> storing a character with the
   * value <code>c</code> and escaped value <code>escaped</code>.
   */ 
  public CharacterLiteral( char value, String escaped) 
  {
    this.value = value;
    this.escaped = escaped;
  }
  
  /**
   * Lazily reconstruct this node.
   */
  public CharacterLiteral reconstruct( char value, String escaped)
  {
    if( this.value == value && this.escaped.equals( escaped)) {
      return this;
    }
    else {
      CharacterLiteral n = new CharacterLiteral( value, escaped);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  public long getValue()
  {
    return value;
  }

  public String getEscapedCharValue()
  {
    return escaped;
  }

  Node visitChildren( NodeVisitor v) 
  {
    /* Nothing to do. */
    return this;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    setCheckedType( c.getTypeSystem().getChar());
    return this;
  }  

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "'" + escaped + "'");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( CHAR LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
