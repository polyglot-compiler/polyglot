package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/** 
 * An <code>IntLiteral</code> represents a literal in Java of an integer
 * type.
 */
public class IntLiteral extends Literal 
{
  public static final int BYTE    = 0;
  public static final int SHORT   = 1;
  public static final int INT     = 2;
  public static final int LONG    = 3;
  
  protected final int kind;
  protected final long value;
 
  /**
   * Creates a new IntLiteral storing a byte with the value <code>b</code>.
   */ 
  public IntLiteral( byte b) 
  {
    this( BYTE, b);
  }

  /**
   * Creates a new IntLiteral storing a short with the value <code>s</code>.
   */ 
  public IntLiteral( short s) 
  {
    this( SHORT, s);
  }
    
  /**
   * Creates a new IntLiteral storing an integer with the value <code>i</code>.
   */ 
  public IntLiteral( int i) 
  {
    this( INT, i);
  }

  /**
   * Creates a new IntLiteral storing a long with the value <code>l</code>.
   */ 
  public IntLiteral( long l) 
  {
    this( LONG, l);
  }

  public IntLiteral( int kind, long value)
  {
    this.kind = kind;
    this.value = value;
  }

  public IntLiteral reconstruct( int kind, long value) 
  {
    if( this.kind == kind && this.value == value) {
      return this;
    }
    else {
      IntLiteral n = new IntLiteral( kind, value);
      n.copyAnnotationsFrom( this);
      return n;
    }      
  }

  /** 
   * Returns the kind of this <code>IntLiteral</code> as specified by the
   * <code>public static</code> constants in this class.  
   */ 
  public int getKind() 
  {
    return kind;
  }

  /**
   * Returns the <code>byte</code> value of this literal.
   */
  public byte getByteValue() 
  {
    return (byte)value;
  }
  
  /**
   * Returns the <code>short</code> value of this literal.
   */
  public short getShortValue() 
  {
    return (short)value;
  }

  /** 
   * Returns the <code>int</code> value of this literal.
   */
  public int getIntValue() 
  {
    return (int)value;
  }
  
  /**
   * Returns the <code>long</code> value of this literal.
   */
  public long getLongValue() 
  {
    return value;   
  } 

  public Node typeCheck( LocalContext c) throws SemanticException
  {
    Type t = null;

    switch( kind) {
    case BYTE:
      t = c.getTypeSystem().getByte();
      break;

    case SHORT:
      t = c.getTypeSystem().getShort();
      break;

    case INT:
      t = c.getTypeSystem().getInt();
      break;

    case LONG:
      t = c.getTypeSystem().getLong();
      break;
    }     
    setCheckedType( t);
    
    return this;
  }  

  public void translate( LocalContext c, CodeWriter w)
  {
    if( kind == LONG) {
      w.write( Long.toString( value ) /* + 'L' */);
    }
    else {
      w.write( Long.toString( value ));
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "( INTEGER LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
    w.write( ")");
  }
}
