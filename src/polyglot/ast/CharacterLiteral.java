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
  public CharacterLiteral( Node ext, char value) 
  {
    this( ext, value, String.valueOf( value));
  }

    public CharacterLiteral( char value) {
	this(null, value);
    }

  /**
   * Creates a new <code>CharacterLiteral</code> storing a character with the
   * value <code>c</code> and escaped value <code>escaped</code>.
   */ 
  public CharacterLiteral( Node ext, char value, String escaped) 
  {
    this.ext = ext;
    this.value = value;
    this.escaped = escaped;
  }
  
    public CharacterLiteral( char value, String escaped) {
	this(null, value, escaped);
    }

  /**
   * Lazily reconstruct this node.
   */
  public CharacterLiteral reconstruct( Node ext, char value, String escaped)
  {
    if( this.value == value && this.ext == ext && this.escaped.equals( escaped)) {
      return this;
    }
    else {
      CharacterLiteral n = new CharacterLiteral( ext, value, escaped);
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

    public CharacterLiteral reconstruct( char value, String escaped) {
	return reconstruct(this.ext, value, escaped);
    }

  public long getValue()
  {
    return value;
  }

  public String getEscapedCharValue()
  {
    return escaped;
  }

  public Node visitChildren( NodeVisitor v) 
  {
    return reconstruct(Node.condVisit(ext, v), value, escaped);
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    setCheckedType( c.getTypeSystem().getChar());
    return this;
  }  

  public void translate_no_override( LocalContext c, CodeWriter w)
  {
    w.write( "'" + escaped + "'");
  }

  public void dump( CodeWriter w)
  {
    w.write( "CHAR LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
  }
}
