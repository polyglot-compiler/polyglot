/*
 * FloatLiteral.java
 */

package jltools.ast;
import jltools.types.LocalContext;
import jltools.util.CodeWriter;

/** 
 * Float Literal
 * 
 * Overview: A FloatLiteral represents a literal in java of type float
 *    or double.
 */
public class FloatLiteral extends Literal {
  
  public static final int FLOAT   = 0;
  public static final int DOUBLE  = 1;

  /**
   * Creates a new FloatLiteral storing a float with the value <f>.
   */
  public FloatLiteral (float f) {
    type = FLOAT;
    value = f;
  }

  /**
   * Creates a new FloatLiteral storing a double with the value <d>.
   */
  public FloatLiteral (double d) {
    type = DOUBLE;
    value = d;
  }

  /**
   * Effects: Returns the type of this FloatLiteral as specified by the
   *   public static constants in this class.
   */ 
  public int getFloatType() {
    return type;
  }

  /**
   * Effects: Returns the float value of this FloatLiteral.
   */
  public float getFloatValue() {
    return (float) value;
  }
  
  /**
   * Effects: Returns the double value of this FloatLiteral.
   */
  public double getDoubleValue() {
    return (double) value;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    w.write ( type == FLOAT ? Float.toString( (float)value) /* + 'F' */: Double.toString( value ));
  }

  public void dump(LocalContext c, CodeWriter w)
  {
    w.write ( " ( " + (type == FLOAT ? " FLOAT " + Float.toString((float)value) :
                                       " DOUBLE " + Double.toString( value ) ) + ")");
  }

  public Node typeCheck( LocalContext c)
  {
    setCheckedType( c.getTypeSystem().getFloat());
    return this;
  }  

  public void visitChildren(NodeVisitor v) {
  }

  public Node copy() {
    switch(type) {
    case FLOAT:
      return new FloatLiteral((float) value);
    case DOUBLE:
      return new FloatLiteral((double) value);
    default:
      throw new Error("Internal error: Float rep broken.");
    }
  }

  public Node deepCopy() {
    return copy();
  }
  
  private int type;
  private double value;  
}

