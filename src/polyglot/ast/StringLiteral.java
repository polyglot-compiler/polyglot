package jltools.ast;

import jltools.util.*;
import jltools.types.*;


/** 
 * A <code>StringLiteral</code> represents an immutable instance of a 
 * <code>String</code> which corresponds to a literal string in Java code.
 */
public class StringLiteral extends Literal 
{
  protected final String value;

  /**
   * Creates a new <code>StringLiteral</code>.
   */ 
  public StringLiteral( String value) 
  {
    this.value = value;
  }
  
  /**
   * Returns the string value of this node.
   */ 
  public String getString() 
  {
    return value;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    setCheckedType( c.getTypeSystem().getString());
    return this;
  }
  
  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "\"" + value + "\"");
  }

  public void dump( CodeWriter w)
  {
    w.write( "( STRING LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
