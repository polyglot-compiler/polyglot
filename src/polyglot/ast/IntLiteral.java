/*
 * IntegerLiteral.java
 */ 

package jltools.ast;

import jltools.types.*;
import jltools.util.CodeWriter;

/** 
 * IntegerLiteral
 * 
 * Overview: An IntLiteral represents a literal in java of an integer
 * type, or boolean type.
 */
public class IntLiteral extends Literal {

  public static final int BOOLEAN = 0;
  public static final int BYTE    = 2;
  public static final int SHORT   = 3;
  public static final int INT     = 4;
  public static final int LONG    = 5;
      
  /**
   * Creates a new IntLiteral storing a boolean with the value <b>
   */ 
  public IntLiteral (boolean b) {
    type = BOOLEAN;
    value = b ? 1 : 0;
  }

  /**
   * Creates a new IntLiteral storing a byte with the value <b>
   */ 
  public IntLiteral (byte b) {
    type = BYTE;
    value = b;
  }

  /**
   * Creates a new IntLiteral storing a short with the value <s>
   */ 
  public IntLiteral (short s) {
    type = SHORT;
    value = s;
  }
    
  /**
   * Creates a new IntLiteral storing a integer with the value <i>
   */ 
  public IntLiteral (int i) {
    type = INT;
    value = i;
  }

  /**
   * Creates a new IntLiteral storing a long with the value <l>
   */ 
  public IntLiteral (long l) {
    type = LONG;
    value = l;
  }

  /** 
   * Effects: returns the type of this IntLiteral as specified by the
   *   public static constants in this class.  
   */ 
  public int getIntType() {
    return type;
  }

  /**
   * Effects: returns the boolean value of this IntLiteral.  
   */
  public boolean getBooleanValue() {
    return value == 0 ? false : true;
  }

  /**
   * Effects: returns the byte value of this IntLiteral.
   */
  public byte getByteValue() {
    return (byte) value;
  }
  
  /**
   * Effects: returns the short value of this IntLiteral.
   */
  public short getShortValue() {
    return (short) value;
  }

  /**
   * Effects: returns the int value of this IntLiteral.
   */
  public int getIntValue() {
    return (int) value;
  }
  
  /**
   * Effects: returns the long value of this IntLiteral.
   */
  public long getLongValue() {
    return value;
  }


  public void translate(LocalContext c, CodeWriter w)
  {
    if (type == BOOLEAN)
    {
      w.write( (value != 0 ? "true" : "false")  );
    }
    else if(type == LONG)
    {
      w.write( Long.toString( value ) /* + 'L' */);
    }
    else 
    {
      w.write( Long.toString( value ));
    }
  }

  public Node dump( CodeWriter w)
  {
    w.write( "( INTEGER LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
    return null;
  }

  public Node typeCheck(LocalContext c) throws TypeCheckException
  {
    switch( type) {
    case BOOLEAN:
      setCheckedType( c.getTypeSystem().getBoolean());
      break;
    case BYTE:
      setCheckedType( c.getTypeSystem().getByte());
      break;
    case SHORT:
      setCheckedType( c.getTypeSystem().getShort());
      break;
    case INT:
      setCheckedType( c.getTypeSystem().getInt());
      break;
    case LONG:
      setCheckedType( c.getTypeSystem().getLong());
      break;
    } 
    return this;
  }  

  public void visitChildren(NodeVisitor v) {
    // nothing to do
  }

  public Node copy() {
    IntLiteral il = new IntLiteral(0);
    il.type = type;
    il.value = value;
    il.copyAnnotationsFrom(this);
    return il;
  }

  public Node deepCopy() {
    return copy();
  }
  
  private int  type;
  private long value;
}
