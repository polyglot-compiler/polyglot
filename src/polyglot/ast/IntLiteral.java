/*
 * IntegerLiteral.java
 */ 

package jltools.ast;
import jltools.types.Context;
import jltools.util.CodeWriter;

/** 
 * IntegerLiteral
 * 
 * Overview: An IntLiteral represents a literal in java of an integer
 * type, character type or boolean type.
 */
public class IntLiteral extends Literal {

  public static final int BOOLEAN = 0;
  public static final int CHAR    = 1;
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
   * Creates a new IntLiteral storing a character with the value <c>
   */ 
  public IntLiteral (char c) {
    type = CHAR;
    value = c;
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
   * Effects: returns the character value of this IntLiteral.
   */
  public char getCharValue() {
    return (char) value;
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


  public void translate(Context c, CodeWriter w)
  {
    if (type == BOOLEAN)
    {
      w.write( (value != 0 ? "true" : "false")  );
    }
    else
    {
      w.write( Long.toString( value ) );
    }
  }

  public void dump(Context c, CodeWriter w)
  {
    w.write( " ( INTLITERAL " );
    dumpNodeInfo(c, w);
    w.write( " ( " + value + " ) )");
  }

  public Node typeCheck(Context c)
  {
    // FIXME; implement
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
