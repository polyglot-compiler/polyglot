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
  public BooleanLiteral( Node ext, boolean value)
  {
    this.ext = ext;
    this.value = value;
  }

    public BooleanLiteral( boolean value) {
	this(null, value);
    }

  public BooleanLiteral reconstruct( Node ext, boolean value)
  {
    if( this.value == value && this.ext == ext) {
      return this;
    }
    else {
      return new BooleanLiteral( ext, value);
    }
  }

    public BooleanLiteral reconstruct( boolean value) {
	return reconstruct(this.ext, value);
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
