package jltools.ast;

import jltools.types.*;
import jltools.util.*;


/** 
 * An <code>IntLiteral</code> represents a literal in Java of an integer
 * type.
 */
public class IntLiteral extends NumericalLiteral 
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
  public IntLiteral( Node ext, byte b) 
  {
    this( ext, BYTE, b);
  }

    public IntLiteral( byte b) {
	this(null, BYTE, b);
    }

  /**
   * Creates a new IntLiteral storing a short with the value <code>s</code>.
   */ 
  public IntLiteral( Node ext, short s) 
  {
    this.ext = ext;
    value = s;
    if ( Math.abs(s) < Byte.MAX_VALUE)
      kind = BYTE;
    else 
      kind = SHORT;
  }
    
    public IntLiteral( short s) {
	this(null, s);
    }

  /**
   * Creates a new IntLiteral storing an integer with the value <code>i</code>.
   */ 
  public IntLiteral( Node ext, int i) 
  {
    this.ext = ext;
    value = i;
    if (Math.abs(i) < Byte.MAX_VALUE)
      kind = BYTE;
    else if (Math.abs(i) < Short.MAX_VALUE)
      kind = SHORT;
    else
      kind = INT;
  }

    public IntLiteral( int i) {
	this(null, i);
    }

  /**
   * Creates a new IntLiteral storing a long with the value <code>l</code>.
   */ 
  public IntLiteral( Node ext, long l) 
  {
    this.ext = ext;
    value = l;
    if (Math.abs( l) < Byte.MAX_VALUE)
      kind = BYTE;
    else if (Math.abs(l) < Short.MAX_VALUE)
      kind = SHORT;
    else if (Math.abs(l) < Integer.MAX_VALUE)
      kind = INT;
    else
      kind = LONG;
  }

    public IntLiteral( long l) {
	this(null, l);
    }

  public IntLiteral( Node ext, int kind, long value)
  {
    this.ext = ext;
    this.kind = kind;
    this.value = value;
  }

    public IntLiteral( int kind, long value) {
	this(null, kind, value);
    }

  public IntLiteral reconstruct( Node ext, int kind, long value) 
  {
    if( this.kind == kind && this.value == value && this.ext == ext) {
      return this;
    }
    else {
      IntLiteral n = new IntLiteral( ext, kind, value);
      n.copyAnnotationsFrom( this);
      return n;
    }      
  }

    public IntLiteral reconstruct( int kind, long value) {
	return reconstruct(this.ext, kind, value);
    }

  public Node visitChildren( NodeVisitor v) 
  {
      return reconstruct(Node.condVisit(ext, v), kind, value);
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
  public long getValue() 
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
      w.write( Long.toString( value ) + 'L');
    }
    else {
      w.write( Long.toString( value ));
    }
  }

  public void dump( CodeWriter w)
  {
    w.write( "INTEGER LITERAL");
    w.write( " < " + value + " > ");
    dumpNodeInfo( w);
  }
  
  public String toString() {
	  String str = "" + value;
	  return str;
  }
}
