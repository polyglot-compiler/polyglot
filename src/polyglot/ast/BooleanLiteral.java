package jltools.ast;

import jltools.types.*;
import jltools.util.*;

/**
 * A <code>BooleanLiteral</code> represents the two Java keywords
 * <code>true</code> and <code>false</code>.
 */
public class BooleanLiteral extends Literal
{
  protected final boolean value;

  /**
   * Creates a new <code>BooleanLiteral</code>.
   */
  public BooleanLiteral( boolean value)
  {
    this.value = value;
  }

  public BooleanLiteral reconstruct( boolean value)
  {
    if( this.value == value) {
      return this;
    }
    else {
      return new BooleanLiteral( value);
    }
  }

  public boolean getBooleanValue()
  {
    return value;
  }

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    setCheckedType( c.getTypeSystem().getBoolean());
    
    return this;
  }

  public void translate( LocalContext c, CodeWriter w)
  {
    w.write( "" + value);
  }

  public void dump( CodeWriter w) 
  {
    w.write( "( BOOLEAN LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
