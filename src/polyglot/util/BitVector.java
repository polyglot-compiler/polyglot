package jltools.util;

public class BitVector
{
  private int size;
  private boolean[] bits;

  public BitVector()
  {
    this( 32);
  }
  
  public BitVector( int initialSize)
  {
    size = initialSize;
    bits = new boolean[ size];
  }

  public final void setBit( int which, boolean value)
  {
    if( which >= size) {
      boolean[] newBits = new boolean[ which + 1];
    }
    
    bits[ which] = value;
  }

  public final boolean getBit( int which)
  {
    if( which > size) {
      return false;
    }
    else {
      return bits[ which];
    }
  }
}

