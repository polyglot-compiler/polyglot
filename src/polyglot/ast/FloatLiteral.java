package jltools.ast;

import jltools.types.*;
import jltools.util.*;

/** 
 * A <code>FloatLiteral</code> represents a literal in java of type
 * <code>float</code> or <code>double</code>.
 */
public class FloatLiteral extends Literal 
{  
  public static final int FLOAT   = 0;
  public static final int DOUBLE  = 1;

  protected final int kind;
  protected final double value;  

  /**
   * Creates a new <code>FloatLiteral</code> storing a float with the value 
   * <code>f</code>.
   */
  public FloatLiteral( float f) 
  {
    kind = FLOAT;
    value = f;
  }

  /**
   * Creates a new <code>FloatLiteral</code> storing a double with the value
   * <code>d</code>.
   */
  public FloatLiteral( double d) 
  {
    kind = DOUBLE;
    value = d;
  }

  /* Lazily reconstruct this node. */
  public FloatLiteral reconstruct( int kind, double value)
  {
    if( this.kind == kind && (double)this.value == value) {
      return this;
    }
    else {
      FloatLiteral n = (kind == FLOAT ? new FloatLiteral( (float)value) :
                                  new FloatLiteral( (double)value));
      n.copyAnnotationsFrom( this);
      return n;
    }
  }

  /**
   * Returns the kind of this <code>FloatLiteral</code> as specified by the
   * <code>public static</code> constants in this class.
   */ 
  public int getKind() 
  {
    return kind;
  }

  /**
   * Returns the float value of this <code>FloatLiteral</code>.
   */
  public float getFloatValue() 
  {
    return (float)value;
  }
  
  /**
   * Returns the double value of this <code>FloatLiteral</code>.
   */
  public double getDoubleValue() 
  {
    return (double)value;
  }

  /**
   * Visit this children of this node. */
  Node visitChildren(NodeVisitor v) 
  {
    return this;
  }

  public Node typeCheck( LocalContext c)
  {
    setCheckedType( c.getTypeSystem().getFloat());
    return this;
  }  

  public void translate(LocalContext c, CodeWriter w)
  {
    w.write( kind == FLOAT ? Float.toString( (float)value) /* + 'F' */ : 
                          Double.toString( value));
  }

  public void dump( CodeWriter w)
  {
    w.write( "( " + (kind == FLOAT ? " FLOAT LITERAL < " 
                                        + Float.toString((float)value) :
                               " DOUBLE LITERAL < " 
                                        + Double.toString( value ) ) + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}

