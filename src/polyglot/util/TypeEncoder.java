package jltools.util;

import jltools.types.*;

import java.io.*;
import java.util.zip.*;

/**
 * FIXME docs
 */
public class TypeEncoder
{
  protected TypeSystem ts;

  public TypeEncoder( TypeSystem ts)
  {
    this.ts = ts;
  }

  public String encode( Type t) throws IOException
  {
    ByteArrayOutputStream baos;
    ObjectOutputStream oos;
    byte[] b;
    StringBuffer sb;

    baos = new ByteArrayOutputStream();
    oos = new TypeOutputStream( new GZIPOutputStream( baos), ts, t);

    oos.writeObject( t);
    oos.flush();
    oos.close();
    b = baos.toByteArray();

    sb = new StringBuffer();
    encodeBytes( sb, b);
    return sb.toString();
  }

  public Type decode( String s) throws IOException, ClassNotFoundException
  {
    char[] source;
    byte[] b;
    ObjectInputStream ois;

    source = s.toCharArray();
    b = new byte[ source.length];
    decodeBytes( b, 0, source, 0);
    
    ois = new TypeInputStream( new GZIPInputStream( 
                                 new ByteArrayInputStream( b)), ts);

    return (Type)ois.readObject();
  }

  protected static void encodeChar( StringBuffer sb, char c)
  {
    if( c > 0xFF ) {
      sb.append( c);
    }
    else if( c == 0x22) {
      sb.append( "\\\"");
    }
    else if( c == 0x5C) {
      sb.append( "\\\\");
    }
    else if( c >= 0x20 && c < 0x7F) {
      sb.append( (char)c);
    }
    else {
      sb.append( "\\");
      
      String s = Integer.toOctalString( c);
      switch( s.length()) 
      {
      case 1:
        sb.append( "0");
      case 2:
        sb.append( "0");
      }
      sb.append( s);
    }

  }

  protected static void encodeShort( StringBuffer sb, short s)
  {
    encodeChar( sb, (char)(s >>> 8));
    encodeChar( sb, (char)(s & 0x00FF));
  }
  
  protected static void encodeInt( StringBuffer sb, int i)
  {
    encodeShort( sb, (short)(i >>> 16));
    encodeShort( sb, (short)(i & 0x0000FFFF));
  }

  protected static void encodeBytes( StringBuffer sb, byte[] b) 
  {
    int i, end;

    for( i = 0; i < b.length; i++) {
      encodeChar( sb, (char)b[ i]);
    }
  }

  protected static int decodeByte( byte[] dest, int doff, 
                                   char[] source, int soff)
  {
    char c = source[ soff++];

    dest[ doff] = (byte)c;

    return soff;
  }

  protected static int decodeShort( short[] dest, int doff, 
                                    char[] source, int soff)
  {
    byte[] b = new byte[ 2];
    soff = decodeByte( b, 0, source, soff);
    soff = decodeByte( b, 1, source, soff);

    dest[ doff] = (short)(((b[ 0] << 8) & 0xFF00) | (b[ 1] & 0x00FF));

    return soff;
  }

  protected static int decodeInt( int[] dest, int doff, 
                                  char[] source, int soff)
  {
    short[] s = new short[ 2];
    soff = decodeShort( s, 0, source, soff);
    soff = decodeShort( s, 1, source, soff);

    dest[ doff] = (int)(((s[ 0] << 16) & 0xFFFF0000) | (s[ 1] & 0x0000FFFF));
    System.err.println( "  decodeInt = " + dest[ doff]);
    System.err.println( "  soff = " + soff);
    return soff;
  }

  protected static int decodeBytes( byte[] dest, int doff,
                                    char[] source, int soff)
  {
    for( int i = doff, j = soff; i < dest.length; i++) {
      soff = decodeByte( dest, i, source, soff);
    }
    return soff;
  }

}
